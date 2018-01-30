package org.wso2.carbon.privacy.forgetme;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;
import org.wso2.carbon.privacy.forgetme.runtime.SystemEnv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ConfigReader {

    private ServiceLoader<InstructionReader> readerServiceLoader;
    private List<InstructionReader> instructionReaderList;
    private static ConfigReader configReader = new ConfigReader();

    public static ConfigReader getInstance() {
        return configReader;
    }

    private ConfigReader() {

        this.readerServiceLoader = ServiceLoader.load(InstructionReader.class);
        this.instructionReaderList = new ArrayList<>();
        readerServiceLoader.forEach(r -> instructionReaderList.add(r));
    }

    public ForgetMeInstruction loadInstruction(File file) {

        JSONParser jsonParser = new JSONParser();

        Environment environment = new SystemEnv();
        try {
            Object parsedObject = jsonParser.parse(new FileReader(file));
            if (parsedObject instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) parsedObject;
                Object typeObject = jsonObject.get("type");
                if (typeObject instanceof String) {
                    String type = (String)typeObject;
                    InstructionReader selectedReader = instructionReaderList.stream()
                            .filter(t -> t.getType().equals(type)).findAny().orElse(null);
                    if (selectedReader != null) {
                        File directory = file.getParentFile();
                        return selectedReader.read(directory, environment);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;

    }
}
