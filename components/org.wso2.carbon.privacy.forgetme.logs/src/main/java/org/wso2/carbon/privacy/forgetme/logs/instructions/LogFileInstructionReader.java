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
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.privacy.forgetme.logs.beans.Patterns;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;
import org.wso2.carbon.privacy.forgetme.logs.exception.LogProcessorException;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Implements Instruction generation for log file processing.
 */
public class LogFileInstructionReader implements InstructionReader {

    private static final Logger logger = LoggerFactory.getLogger(LogFileInstructionReader.class);

    private static final String NAME = "log-file";
    private static final String LOG_FILE_PATH_PROPERTY = "log-file-path";
    private static final String LOG_FILE_NAME_REGEX_PROPERTY = "log-file-name-regex";

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public List<ForgetMeInstruction> read(Path patternDir, Properties properties, Environment environment)
            throws ModuleException {

        File contentDirectory = patternDir.toFile();
        File[] patternFiles = contentDirectory
                .listFiles(fileName -> fileName.isFile() && isXmlExtension(fileName.getName()));

        List<Patterns.Pattern> patternList = loadReplacementPatterns(patternFiles);

        List<File> logFiles = listMatchingLogFiles(patternDir, properties);

        return logFiles.stream().map((logFile) -> new LogFileInstruction(patternList, logFile))
                .collect(Collectors.toList());
    }

    private List<Patterns.Pattern> loadReplacementPatterns(File[] patternFiles) throws ModuleException {

        List<Patterns.Pattern> patternList = new ArrayList<>();

        for (int i = 0; i < patternFiles.length; i++) {
            File file = patternFiles[i];
            try {
                Patterns patterns = readXML(file);
                patternList.addAll(patterns.getPattern());
            } catch (LogProcessorException e) {
                throw new ModuleException("Could not read the file : " + file, e);
            }
        }
        return patternList;
    }

    /**
     * Lists the matching log files in the directory.
     *
     * @param patternDir
     * @param properties
     * @return
     * @throws ModuleException
     */
    private List<File> listMatchingLogFiles(Path patternDir, Properties properties) throws ModuleException {

        String logFilePath = properties.getProperty(LOG_FILE_PATH_PROPERTY);
        if (logFilePath == null) {
            throw new ModuleException(
                    "Property : " + LOG_FILE_PATH_PROPERTY + " is not set for the processor :" + NAME + " for path : "
                            + patternDir);
        }
        String logFileNameRegex = properties.getProperty(LOG_FILE_NAME_REGEX_PROPERTY);
        if (logFileNameRegex == null) {
            throw new ModuleException(
                    "Property : " + LOG_FILE_NAME_REGEX_PROPERTY + " is not set for the processor :" + NAME
                            + " for path : " + patternDir);
        }
        return scanForFiles(logFilePath, logFileNameRegex);
    }

    private List<File> scanForFiles(String logFilePath, String logFileNameRegex) {

        File dir = new File(logFilePath);
        Pattern regexPattern = Pattern.compile(logFileNameRegex);

        File[] logFiles = dir.listFiles((dir1, name) -> regexPattern.matcher(name).matches());
        return Arrays.asList(logFiles);
    }

    /**
     * Read xml file for regex pattern configurations.
     *
     * @param xmlFile The config file.
     * @return Patterns object.
     * @throws LogProcessorException
     */
    private Patterns readXML(File xmlFile) throws LogProcessorException {

        if (logger.isDebugEnabled()) {
            logger.debug("Reading pattern configuration file at : " + xmlFile);
        }
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Patterns.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Patterns patterns = (Patterns) unmarshaller.unmarshal(xmlFile);

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            if (logger.isDebugEnabled()) {
                logger.debug("Read pattern configuration file success : " + xmlFile);
            }
            return patterns;
        } catch (JAXBException ex) {
            throw new LogProcessorException("Error occurred while unmarshalling xml content.", ex);
        }
    }

    private boolean isXmlExtension(String name) {
        return true;
    }
}
