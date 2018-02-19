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

package org.wso2.carbon.privacy.forgetme.logs.instructions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.report.ReportAppender;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResult;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionExecutionException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;
import org.wso2.carbon.privacy.forgetme.logs.beans.Patterns;
import org.wso2.carbon.privacy.forgetme.logs.processor.LogFileProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements forget-me log re-writing instruction.
 *
 */
public class LogFileInstruction implements ForgetMeInstruction {

    private static final Logger log = LoggerFactory.getLogger(LogFileInstruction.class);

    private List<Patterns.Pattern> patterns;
    private File logFile;

    public LogFileInstruction(List<Patterns.Pattern> patterns, File logFile) {

        this.patterns = patterns;
        this.logFile = logFile;
    }

    @Override
    public ForgetMeResult execute(UserIdentifier userIdentifier, ProcessorConfig processorConfig,
                                  Environment environment, ReportAppender reportAppender)
            throws InstructionExecutionException {

        List<File> logFiles = new ArrayList<>();
        logFiles.add(logFile);
        LogFileProcessor logFileProcessor = new LogFileProcessor();

        if (log.isDebugEnabled()) {
            log.debug("File {} is being processed.", logFile.getName());
        }

        logFileProcessor.processFiles(userIdentifier, reportAppender, patterns, logFiles);
        return new ForgetMeResult();
    }
}
