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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.report.CloseableReportAppenderBuilder;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfigReader;
import org.wso2.carbon.privacy.forgetme.config.ConfigConstants;
import org.wso2.carbon.privacy.forgetme.config.SystemConfig;
import org.wso2.carbon.privacy.forgetme.runtime.ForgetMeExecutionException;
import org.wso2.carbon.privacy.forgetme.runtime.VariableResolver;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * Reads the configuration forn the main config file.
 *
 */
public class ConfigReader {

    private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);

    private ServiceLoader<InstructionReader> readerServiceLoader;
    private ServiceLoader<ProcessorConfigReader> processorConfigReaderServiceLoader;
    private ServiceLoader<CloseableReportAppenderBuilder> reportAppenderBuilderServiceLoader;
    private List<InstructionReader> instructionReaderList;
    private Map<String, InstructionReader> instructionReaderMap;
    private Map<String, ProcessorConfigReader> stringProcessorConfigReaderMap;
    private Map<String, CloseableReportAppenderBuilder> reportAppenderBuilderMap;
    private static ConfigReader configReader = new ConfigReader();

    public static ConfigReader getInstance() {

        return configReader;
    }

    private ConfigReader() {

        this.readerServiceLoader = ServiceLoader.load(InstructionReader.class);
        this.processorConfigReaderServiceLoader = ServiceLoader.load(ProcessorConfigReader.class);
        this.reportAppenderBuilderServiceLoader = ServiceLoader.load(CloseableReportAppenderBuilder.class);
        this.instructionReaderList = new ArrayList<>();
        this.instructionReaderMap = new HashMap<>();
        this.readerServiceLoader.forEach(r -> instructionReaderList.add(r));
        this.readerServiceLoader.forEach(r -> instructionReaderMap.put(r.getType(), r));
        this.stringProcessorConfigReaderMap = new HashMap<>();
        this.processorConfigReaderServiceLoader.forEach(r -> stringProcessorConfigReaderMap.put(r.getName(), r));
        this.reportAppenderBuilderMap = new HashMap<>();
        this.reportAppenderBuilderServiceLoader.forEach(r -> reportAppenderBuilderMap.put(r.getType(), r));
    }

    /**
     * Reads the system configuration from the file given.
     *
     * @param file
     * @return
     * @throws ForgetMeExecutionException
     */
    public SystemConfig readSystemConfig(File file, Environment environment) throws ForgetMeExecutionException {

        SystemConfig systemConfig = new SystemConfig();
        JSONParser jsonParser = new JSONParser();
        Path basePath = file.toPath().getParent();

        try {
            Object parsedObject = jsonParser.parse(new FileReader(file));
            if (parsedObject instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) parsedObject;

                Object processors = jsonObject.get(ConfigConstants.CONFIG_ELEMENT_PROCESSORS);
                if (processors instanceof JSONArray) {
                    loadProcessors((JSONArray) processors, systemConfig);
                }

                Object extensions = jsonObject.get(ConfigConstants.CONFIG_ELEMENT_EXTENSIONS);
                if (extensions instanceof JSONArray) {
                    loadExtensions((JSONArray) extensions, systemConfig, basePath);
                }

                Object directories = jsonObject.get(ConfigConstants.CONFIG_ELEMENT_DIRECTORIES);
                if (directories instanceof JSONArray) {
                    loadDirectories((JSONArray) directories, systemConfig, basePath, environment);
                }

                Object reports = jsonObject.get(ConfigConstants.CONFIG_ELEMENT_REPORTS);
                if (reports instanceof JSONArray) {
                    loadReports((JSONArray) reports, systemConfig, basePath);
                }
            }
        } catch (IOException e) {
            throw new ForgetMeExecutionException(
                    "Could not read the config files related to : " + file.getAbsolutePath(), e);
        } catch (ParseException e) {
            throw new ForgetMeExecutionException("Could not parse config files related to: " + file.getAbsolutePath(),
                    e);
        }
        return systemConfig;
    }

    private void loadDirectories(JSONArray directories, SystemConfig systemConfig, Path basePath,
            Environment environment) throws ForgetMeExecutionException {

        VariableResolver variableResolver = new VariableResolver(environment);
        for (Object e : directories) {
            if (e instanceof JSONObject) {
                JSONObject dirConfig = (JSONObject) e;
                Object type = dirConfig.get(ConfigConstants.CONFIG_ELEMENT_TYPE);
                Object dir = dirConfig.get(ConfigConstants.CONFIG_ELEMENT_DIR);
                Object processor = dirConfig.get(ConfigConstants.CONFIG_ELEMENT_PROCESSOR);
                Properties additionalProperties = getAdditionalProperties(dirConfig, variableResolver);
                if (type instanceof String && dir instanceof String && processor instanceof String) {
                    String processorName = (String) processor;
                    if (systemConfig.getProcessors().contains(processorName)) {
                        InstructionReader instructionReader = instructionReaderMap.get(processor);
                        String pathStr = variableResolver.resolve((String) dir);
                        Path path = Paths.get(pathStr);
                        if (!path.isAbsolute()) {
                            path = basePath.resolve((String) dir);
                        }
                        if (instructionReader != null) {
                            systemConfig.addInstructionReader(path, instructionReader, additionalProperties);
                        } else {
                            throw new ForgetMeExecutionException(
                                    "Could not find an instruction reader for the processor : " + processor);
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("The processor : {} is not enabled for directory : {} ", processor, dir);
                        }
                    }
                }
            }
        }
    }

    private Properties getAdditionalProperties(JSONObject dirConfig, VariableResolver variableResolver) {

        Properties properties = new Properties();
        dirConfig.forEach((key, value) -> properties.setProperty(key.toString(),
                variableResolver.resolve(value.toString())));
        return properties;
    }

    private void loadProcessors(JSONArray processors, SystemConfig systemConfig) {

        processors.forEach(e -> {
            if (e instanceof String) {
                systemConfig.addProcessor((String) e);
            }
        });
    }

    private void loadExtensions(JSONArray extensions, SystemConfig systemConfig, Path basePath)
            throws ForgetMeExecutionException {

        for (Object e : extensions) {
            if (e instanceof JSONObject) {
                JSONObject extension = (JSONObject) e;
                Object processor = extension.get(ConfigConstants.CONFIG_ELEMENT_PROCESSOR);
                Object type = extension.get(ConfigConstants.CONFIG_ELEMENT_TYPE);
                Object dir = extension.get(ConfigConstants.CONFIG_ELEMENT_DIR);
                Object properties = extension.get(ConfigConstants.CONFIG_ELEMENT_PROPERTIES);
                if (processor instanceof String && dir instanceof String && type instanceof String) {
                    ProcessorConfigReader processorConfigReader = stringProcessorConfigReaderMap.get(type);
                    if (processorConfigReader == null) {
                        throw new ForgetMeExecutionException(
                                "No processor configuration extension found for : " + processor + ", dir: " + dir);
                    } else {
                        Path path = basePath.resolve((String) dir);
                        ProcessorConfig processorConfig;
                        try {
                            Map<String, String> propertiesMap;
                            if (properties instanceof JSONArray) {
                                propertiesMap = getPropertiesMap((JSONArray) properties);
                            } else {
                                propertiesMap = new HashMap<>();
                            }
                            processorConfig = processorConfigReader.readProcessorConfig(path, propertiesMap);
                            systemConfig.addProcessorConfig((String) processor, processorConfig);
                            if (log.isDebugEnabled()) {
                                log.debug("Loaded processor config : {} from directory : {}", processorConfig, path);
                            }
                        } catch (ModuleException me) {
                            throw new ForgetMeExecutionException(
                                    "Error in reading config of the processor : " + processor + ", from the path : "
                                            + path, me);
                        }
                    }
                }
            }
        }
    }

    private void loadReports(JSONArray reports, SystemConfig systemConfig, Path basePath) throws
            ForgetMeExecutionException {

        for (Object e : reports) {
            if (e instanceof JSONObject) {
                JSONObject reportConfig = (JSONObject) e;
                Object processor = reportConfig.get(ConfigConstants.CONFIG_ELEMENT_PROCESSOR);
                Object type = reportConfig.get(ConfigConstants.CONFIG_ELEMENT_TYPE);
                Object dir = reportConfig.get(ConfigConstants.CONFIG_ELEMENT_DIR);
                Object properties = reportConfig.get(ConfigConstants.CONFIG_ELEMENT_PROPERTIES);
                if (processor instanceof String && dir instanceof String && type instanceof String) {
                    if (!systemConfig.getProcessors().contains(processor)) {
                        throw new ForgetMeExecutionException(
                                "Could not find a processor: " + processor + " registered to load the report appender");
                    }

                    CloseableReportAppenderBuilder closeableReportAppenderBuilder = reportAppenderBuilderMap.get(type);
                    if (closeableReportAppenderBuilder == null) {
                        throw new ForgetMeExecutionException(
                                "No report appender extension found for type: " + type + " for processor: " + processor);
                    }

                    Path path = basePath.resolve((String) dir);
                    Map<String, String> propertiesMap;
                    if (properties instanceof JSONArray) {
                        propertiesMap = getPropertiesMap((JSONArray) properties);
                    } else {
                        propertiesMap = new HashMap<>();
                    }

                    systemConfig.addReportAppenderConfig((String) processor, path, propertiesMap, closeableReportAppenderBuilder);
                    if (log.isDebugEnabled()) {
                        log.debug("Loaded report appender config of type: {} for processor : {}", type,
                                processor);
                    }
                }
            }
        }
    }

    private Map<String, String> getPropertiesMap(JSONArray jsonArray) {

        Map<String, String> propertiesMap = new HashMap<>();
        for (Object arrayItem : jsonArray) {
            JSONObject jsonObject = (JSONObject) arrayItem;
            for (Object key : jsonObject.keySet()) {
                propertiesMap.put((String) key, (String) jsonObject.get(key));
            }
        }
        return propertiesMap;
    }
}
