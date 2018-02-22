/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.privacy.forgetme.analytics.streams.instructions;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.analytics.streams.beans.Streams;
import org.wso2.carbon.privacy.forgetme.analytics.streams.exceptions.AnalyticsStreamsProcessorException;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implements instruction generation for analytics persisted streams.
 */
public class AnalyticsStreamsInstructionReader implements InstructionReader {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsStreamsInstructionReader.class);

    /**
     * Check if the file is a JSON file.
     *
     * @param file File
     * @return
     */
    private static boolean isJsonFile(File file) {
        int index = file.getName().lastIndexOf('.');
        return index > 0 && file.getName().substring(index + 1).equalsIgnoreCase("json");
    }

    @Override
    public String getType() {
        return "analytics-streams";
    }

    @Override
    public List<ForgetMeInstruction> read(Path contentDirectory, Properties properties, Environment environment)
            throws ModuleException {
        List<ForgetMeInstruction> instructions = new ArrayList<>();

        // Get all the JSON files and build the stream object model. The model is required to pass into the instruction.
        File dir = contentDirectory.toFile();
        File[] files = dir.listFiles(file -> file.isFile() && isJsonFile(file));
        if (files != null) {
            List<Streams.Stream> streams = loadStreamDefinitions(files);
            instructions.add(new AnalyticsStreamsInstruction(environment, streams));
        } else {
            throw new AnalyticsStreamsProcessorException("No stream definition files found.");
        }
        return instructions;
    }

    /**
     * Read all the stream definitions in the file system.
     *
     * @param streamFiles List of files which contains stream definitions
     * @return
     */
    private List<Streams.Stream> loadStreamDefinitions(File[] streamFiles) {
        List<Streams.Stream> streams = new ArrayList<>();
        for (File streamFile : streamFiles) {
            try {
                Streams streamCollection = readStreamDefinitionFile(streamFile);
                streams.addAll(streamCollection.getStreams());
            } catch (AnalyticsStreamsProcessorException e) {
                log.error("Error in parsing the stream definition. Ignoring and passing to next file...", e);
            }
        }
        return streams;
    }

    /**
     * Reads stream definition file and creating the object model.
     *
     * @param streamFile Stream definition file
     * @return
     * @throws AnalyticsStreamsProcessorException
     */
    private Streams readStreamDefinitionFile(File streamFile) throws AnalyticsStreamsProcessorException {
        try (FileReader fileReader = new FileReader(streamFile)) {
             return new Gson().fromJson(fileReader, Streams.class);
        } catch (IOException e) {
            throw new AnalyticsStreamsProcessorException("Error in reading stream definition: " +
                    streamFile.getName(), e);
        } catch (JsonParseException e) {
            throw new AnalyticsStreamsProcessorException("Invalid stream definition:" +
                    streamFile.getName(), e);
        }
    }
}
