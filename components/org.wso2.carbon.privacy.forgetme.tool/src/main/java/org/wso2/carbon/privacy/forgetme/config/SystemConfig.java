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

package org.wso2.carbon.privacy.forgetme.config;

import org.wso2.carbon.privacy.forgetme.api.report.CloseableReportAppenderBuilder;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Represents System Configuration.
 *
 */
public class SystemConfig {

    private Map<Path, InstructionReaderConfig> directoryToInstructionReaderMap = new HashMap<>();
    private Map<String, ProcessorConfig> processorConfigMap = new HashMap<>();
    private List<String> processors = new ArrayList<>();
    private Path workDir;
    private Map<String, ReportAppenderConfig> processorToReportAppenderConfigMap = new HashMap<>();

    /**
     * Adds an instruction reader with the given path.
     *
     * @param dir
     * @param instructionReader
     */
    public void addInstructionReader(Path dir, InstructionReader instructionReader, Properties properties) {
        directoryToInstructionReaderMap.put(dir, new InstructionReaderConfig(instructionReader, properties));
    }

    public Map<Path, InstructionReaderConfig> getDirectoryToInstructionReaderMap() {
        return Collections.unmodifiableMap(directoryToInstructionReaderMap);
    }

    /**
     * Adds a processor configuration agianst the given processor name.
     *
     * @param processorName
     * @param processorConfig
     */
    public void addProcessorConfig(String processorName, ProcessorConfig processorConfig) {
        processorConfigMap.put(processorName, processorConfig);
    }

    public Map<String, ProcessorConfig> getProcessorConfigMap() {
        return Collections.unmodifiableMap(processorConfigMap);
    }

    /**
     * Add a processor name, effectively enables the processor.
     *
     * @param processor
     */
    public void addProcessor(String processor) {
        processors.add(processor);
    }

    /**
     * Returns the active list of processor names.
     * @return
     */
    public List<String> getProcessors() {
        return Collections.unmodifiableList(processors);
    }

    public Path getWorkDir() {
        return workDir;
    }

    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    /**
     * Adds a report appender builder for a given processor with report appender parameters.
     *
     * @param processor                      processor name
     * @param reportDirectoryPath            directory path of the report
     * @param properties                     report properties
     * @param closeableReportAppenderBuilder report appender builder
     */
    public void addReportAppenderConfig(String processor, Path reportDirectoryPath, Map<String, String> properties,
                                        CloseableReportAppenderBuilder
                                                closeableReportAppenderBuilder) {

        processorToReportAppenderConfigMap.put(processor, new ReportAppenderConfig(reportDirectoryPath, properties,
                closeableReportAppenderBuilder));
    }

    /**
     * Returns the processor to report reader configuration mapping.
     *
     * @return a map of processor to report reader configuration
     */
    public Map<String, ReportAppenderConfig> getProcessorToReportAppenderConfigMap() {

        return Collections.unmodifiableMap(processorToReportAppenderConfigMap);
    }
}
