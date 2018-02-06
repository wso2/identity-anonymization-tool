package org.wso2.carbon.privacy.forgetme;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfigReader;
import org.wso2.carbon.privacy.forgetme.config.ConfigConstants;
import org.wso2.carbon.privacy.forgetme.config.SystemConfig;
import org.wso2.carbon.privacy.forgetme.runtime.ForgetMeExecutionException;
import org.wso2.carbon.privacy.forgetme.runtime.SystemEnv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    private List<InstructionReader> instructionReaderList;
    private Map<String, InstructionReader> instructionReaderMap;
    private Map<String, ProcessorConfigReader> stringProcessorConfigReaderMap;
    private static ConfigReader configReader = new ConfigReader();

    public static ConfigReader getInstance() {
        return configReader;
    }

    private ConfigReader() {

        this.readerServiceLoader = ServiceLoader.load(InstructionReader.class);
        this.processorConfigReaderServiceLoader = ServiceLoader.load(ProcessorConfigReader.class);
        this.instructionReaderList = new ArrayList<>();
        this.instructionReaderMap = new HashMap<>();
        this.readerServiceLoader.forEach(r -> instructionReaderList.add(r));
        this.readerServiceLoader.forEach(r -> instructionReaderMap.put(r.getType(), r));
        this.stringProcessorConfigReaderMap = new HashMap<>();
        this.processorConfigReaderServiceLoader.forEach(r -> stringProcessorConfigReaderMap.put(r.getName(), r));
    }

    /**
     * Reads the system configuration from the file given.
     *
     * @param file
     * @return
     * @throws ForgetMeExecutionException
     */
    public SystemConfig readSystemConfig(File file) throws ForgetMeExecutionException {

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
                    loadDirectories((JSONArray) directories, systemConfig, basePath);
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

    private void loadDirectories(JSONArray directories, SystemConfig systemConfig, Path basePath)
            throws ForgetMeExecutionException {

        for (Object e : directories) {
            if (e instanceof JSONObject) {
                JSONObject dirConfig = (JSONObject) e;
                Object type = dirConfig.get(ConfigConstants.CONFIG_ELEMENT_TYPE);
                Object dir = dirConfig.get(ConfigConstants.CONFIG_ELEMENT_DIR);
                Object processor = dirConfig.get(ConfigConstants.CONFIG_ELEMENT_PROCESSOR);
                Properties additionalProperties = getAdditionalProperties(dirConfig, new SystemEnv());
                if (type instanceof String && dir instanceof String && processor instanceof String) {
                    String processorName = (String) processor;
                    if (systemConfig.getProcessors().contains(processorName)) {
                        InstructionReader instructionReader = instructionReaderMap.get(processor);
                        Path path = Paths.get((String) dir);
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

    private Properties getAdditionalProperties(JSONObject dirConfig, Environment environment) {

        Properties properties = new Properties();
        dirConfig.forEach((key, value) -> {
            properties.setProperty(key.toString(), value.toString()); //TODO: Parse environment
        });
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

        for (Iterator iterator = extensions.iterator(); iterator.hasNext(); ) {
            Object e = iterator.next();
            if (e instanceof JSONObject) {
                JSONObject extension = (JSONObject) e;
                Object processor = extension.get(ConfigConstants.CONFIG_ELEMENT_PROCESSOR);
                Object type = extension.get(ConfigConstants.CONFIG_ELEMENT_TYPE);
                Object dir = extension.get(ConfigConstants.CONFIG_ELEMENT_DIR);
                if (processor instanceof String && dir instanceof String && type instanceof String) {
                    ProcessorConfigReader processorConfigReader = stringProcessorConfigReaderMap.get(type);
                    if (processorConfigReader == null) {
                        throw new ForgetMeExecutionException(
                                "No processor configuration extension found for : " + processor + ", dir: " + dir);
                    } else {
                        Path path = basePath.resolve((String) dir);
                        ProcessorConfig processorConfig = null;
                        try {
                            processorConfig = processorConfigReader.readProcessorConfig(path);
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
        ;
    }
}
