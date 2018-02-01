package org.wso2.carbon.privacy.forgetme;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfigReader;
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
import java.util.List;
import java.util.Map;
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


    public SystemConfig readSystemConfig(File file) throws ForgetMeExecutionException {

        SystemConfig systemConfig = new SystemConfig();
        JSONParser jsonParser = new JSONParser();
        Path basePath = file.toPath().getParent();

        try {
            Object parsedObject = jsonParser.parse(new FileReader(file));
            if (parsedObject instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) parsedObject;

                Object processors = jsonObject.get("processors");
                if (processors instanceof JSONArray) {
                    loadProcessors((JSONArray) processors, systemConfig);
                }

                Object extensions = jsonObject.get("extensions");
                if (extensions instanceof JSONArray) {
                    loadExtensions((JSONArray) extensions, systemConfig, basePath);
                }

                Object directories = jsonObject.get("directories");
                if (directories instanceof JSONArray) {
                    loadDirectories((JSONArray) directories, systemConfig, basePath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return systemConfig;
    }

    private void loadDirectories(JSONArray directories, SystemConfig systemConfig, Path basePath)
            throws ForgetMeExecutionException {

        for (Object e : directories) {
            if (e instanceof JSONObject) {
                JSONObject dirConfig = (JSONObject) e;
                Object type = dirConfig.get("type");
                Object dir = dirConfig.get("dir");
                Object processor = dirConfig.get("processor");
                if (type instanceof String && dir instanceof String && processor instanceof String) {
                    String processorName = (String) processor;
                    if (systemConfig.getProcessors().contains(processorName)) {
                        InstructionReader instructionReader = instructionReaderMap.get(processor);
                        Path path = Paths.get((String) dir);
                        if (!path.isAbsolute()) {
                            path = basePath.resolve((String) dir);
                        }
                        if (instructionReader != null) {
                            systemConfig.addInstructionReader(path, instructionReader);
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

    private void loadProcessors(JSONArray processors, SystemConfig systemConfig) {
        processors.forEach(e -> {
            if (e instanceof String) {
                systemConfig.addProcessor((String) e);
            }
        });
    }

    private void loadExtensions(JSONArray extensions, SystemConfig systemConfig, Path basePath) {
        extensions.forEach(e -> {
            if (e instanceof JSONObject) {
                JSONObject extension = (JSONObject) e;
                Object processor = extension.get("processor");
                Object type = extension.get("type");
                Object dir = extension.get("dir");
                if (processor instanceof String && dir instanceof String && type instanceof String) {
                    ProcessorConfigReader processorConfigReader = stringProcessorConfigReaderMap.get(type);
                    if (processorConfigReader == null) {
                        System.err.println("No processor configuration extension found for : " + dir);
                    } else {
                        Path path = basePath.resolve((String) dir);
                        ProcessorConfig processorConfig = processorConfigReader.readProcessorConfig(path);
                        systemConfig.addProcessorConfig((String) processor, processorConfig);
                        if (log.isDebugEnabled()) {
                            log.debug("Loaded processor config : {} from directory : {}", processorConfig, path);
                        }
                    }
                }
            }
        });
    }
}
