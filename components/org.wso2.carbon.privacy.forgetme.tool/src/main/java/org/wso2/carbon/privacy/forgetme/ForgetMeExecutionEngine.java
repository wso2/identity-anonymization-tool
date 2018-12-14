/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.privacy.forgetme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.report.CloseableReportAppender;
import org.wso2.carbon.privacy.forgetme.api.report.CloseableReportAppenderBuilder;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResult;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionExecutionException;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;
import org.wso2.carbon.privacy.forgetme.config.InstructionReaderConfig;
import org.wso2.carbon.privacy.forgetme.config.ReportAppenderConfig;
import org.wso2.carbon.privacy.forgetme.config.SystemConfig;
import org.wso2.carbon.privacy.forgetme.processor.ForgetMeCompositeResult;
import org.wso2.carbon.privacy.forgetme.report.PlainTextReportAppender;
import org.wso2.carbon.privacy.forgetme.runtime.ForgetMeExecutionException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Executes Forget Me tasks.
 *
 */
public class ForgetMeExecutionEngine {

    private static final Logger log = LoggerFactory.getLogger(ForgetMeExecutionEngine.class);
    private static final String EXECUTOR_THREAD_PREFIX = "ProcessorExec-";
    private SystemConfig systemConfig;
    private Map<String, ExecutorService> executors = new HashMap<>();
    private Set<Future<ForgetMeResult>> submittedJobs = new HashSet<>();
    private UserIdentifier userIdentifier;
    private Environment systemEnv;

    public ForgetMeExecutionEngine(UserIdentifier userIdentifier, Environment systemEnv, SystemConfig systemConfig) {

        this.systemConfig = systemConfig;
        this.userIdentifier = userIdentifier;
        this.systemEnv = systemEnv;
    }

    /**
     * Executes the engine.
     * This will start multiple processors in parallel threads.
     *
     * @return valid ForgetMeResult.
     * @throws ForgetMeExecutionException  upon any error while executing the set of instructions.
     */
    public ForgetMeResult execute() throws ForgetMeExecutionException {

        ForgetMeCompositeResult forgetMeResult = new ForgetMeCompositeResult();
        createExecutors();

        startExecutors();
        waitForCompletion(forgetMeResult);
        return forgetMeResult;
    }

    /**
     * Waits for the completion of all the processors.
     *
     * @param compositeResult Collected results form all the executions.
     */
    private void waitForCompletion(ForgetMeCompositeResult compositeResult) {

        for (ExecutorService executorService : executors.values()) {
            //Causes shutdown when the current job finishes.
            executorService.shutdown();
        }

        for (Future<ForgetMeResult> future : submittedJobs) {
            try {
                ForgetMeResult result = future.get(1, TimeUnit.HOURS);
                compositeResult.addEntry(result);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                log.error("Interrupted while executing the processor thread : " + future, e);
            }
        }
        log.info("All processors have been properly shut-down");
    }

    private void startExecutors() throws ForgetMeExecutionException {

        for (String processorName : executors.keySet()) {
            List<ForgetMeInstruction> instructions = getInstructions(processorName, systemEnv);
            ProcessorConfig processorConfig = systemConfig.getProcessorConfigMap().get(processorName);
            ReportAppenderConfig reportAppenderConfig = systemConfig.getProcessorToReportAppenderConfigMap().get
                    (processorName);
            ProcessorPipeline processorPipeline = new ProcessorPipeline(systemConfig.getWorkDir(), processorName,
                    userIdentifier, processorConfig, instructions, systemEnv, reportAppenderConfig);
            ExecutorService executorService = executors.get(processorName);
            Future<ForgetMeResult> future = executorService.submit(processorPipeline);
            submittedJobs.add(future);
        }
    }

    private List<ForgetMeInstruction> getInstructions(String processorName, Environment environment)
            throws ForgetMeExecutionException {

        List<ForgetMeInstruction> result = new ArrayList<>();
        for (Map.Entry<Path, InstructionReaderConfig> entry : systemConfig.getDirectoryToInstructionReaderMap()
                .entrySet()) {
            InstructionReaderConfig readerConfig = entry.getValue();
            InstructionReader reader = readerConfig.getInstructionReader();
            Path path = entry.getKey();
            if (reader.getType().equals(processorName)) {
                try {
                    List<ForgetMeInstruction> instructions = reader
                            .read(path, readerConfig.getProperties(), environment);
                    result.addAll(instructions);
                } catch (ModuleException e) {
                    throw new ForgetMeExecutionException(
                            "Unable to get instructions for the processor : " + processorName, e);
                }
            }
        }

        return result;
    }

    private void createExecutors() {

        for (String name : systemConfig.getProcessors()) {
            ExecutorService executorService = Executors
                    .newSingleThreadExecutor(new SimpleThreadFactory(EXECUTOR_THREAD_PREFIX + name));
            executors.put(name, executorService);
        }
    }

    private static class SimpleThreadFactory implements ThreadFactory {

        private String threadNamePrefix;

        public SimpleThreadFactory(String threadNamePrefix) {

            this.threadNamePrefix = threadNamePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {

            return new Thread(r, threadNamePrefix);
        }
    }

    /**
     * Class implements instruction execution thread.
     * Each processor has its own instruction pipeline.
     * Pipelines can run in parallel.
     * Instructions in each pipeline executes sequentially.
     *
     */
    private static class ProcessorPipeline implements Callable<ForgetMeResult> {

        private ProcessorConfig processorConfig;
        private List<ForgetMeInstruction> instructionList;
        private UserIdentifier userIdentifier;
        private String name;
        private Path workDir;
        private Environment environment;
        private ReportAppenderConfig reportAppenderConfig;

        public ProcessorPipeline(Path workDir, String name, UserIdentifier userIdentifier,
                                 ProcessorConfig processorConfig, List<ForgetMeInstruction> instructionList,
                                 Environment environment, ReportAppenderConfig reportAppenderConfig) {

            this.workDir = workDir;
            this.name = name;
            this.userIdentifier = userIdentifier;
            this.processorConfig = processorConfig;
            this.instructionList = instructionList;
            this.environment = environment;
            this.reportAppenderConfig = reportAppenderConfig;
        }

        @Override
        public ForgetMeResult call() throws InstructionExecutionException {

            try (CloseableReportAppender reportAppender = getReportAppender(reportAppenderConfig)) {
                reportAppender.open();
                ForgetMeCompositeResult forgetMeResult = new ForgetMeCompositeResult();
                for (ForgetMeInstruction instruction : instructionList) {
                    instruction.execute(userIdentifier, processorConfig, environment, reportAppender);
                }
                log.info("Processor execution completed. Processor : " + name);
                return forgetMeResult;
            }
        }

        private CloseableReportAppender getReportAppender(ReportAppenderConfig reportAppenderConfig) {

            if (reportAppenderConfig == null) {
                return getDefaultAppender();
            }

            CloseableReportAppenderBuilder closeableReportAppenderBuilder = reportAppenderConfig.getReportAppenderBuilder();

            try {
                return closeableReportAppenderBuilder.build(name, reportAppenderConfig.getReportDirectoryPath(),
                        reportAppenderConfig.getProperties(), userIdentifier);
            } catch (ModuleException e) {
                String msg = "Failed to load report appender: " + closeableReportAppenderBuilder.getType() + " for " +
                        "processor: " + name;
                log.warn(msg);
                if (log.isDebugEnabled()) {
                    log.debug(msg, e);
                }

                return getDefaultAppender();
            }
        }

        private CloseableReportAppender getDefaultAppender() {

            Path reportFile = Paths.get(workDir.toString(), getReportFileName());
            return new PlainTextReportAppender(reportFile.toFile(), name);
        }

        private String getReportFileName() {
            return "Report-" + name + "-" + System.currentTimeMillis() + ".txt";
        }
    }
}
