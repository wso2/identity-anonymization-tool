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

package org.wso2.carbon.privacy.forgetme.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.report.ReportAppender;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * Default report generator.
 *
 */
public class PlainTextReportAppender implements ReportAppender, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(PlainTextReportAppender.class);
    private File file;
    private String name;
    private Writer writer;

    public PlainTextReportAppender(File file, String name) {

        this.file = file;
    }

    public void open() throws InstructionExecutionException {

        if (file.exists()) {
            if (!file.isFile()) {
                throw new InstructionExecutionException("Could not open file for writing. Not a file: " + file);
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new InstructionExecutionException("Could not create a report file : " + file);
            }
        }
        try {
            writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new InstructionExecutionException("Could not open file for writing : " + file, e);
        }
    }

    public void close() {

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error("Error closing the report file for : " + name + " , File:  " + file, e);
            }
        }
    }

    @Override
    public void appendSection(String format, Object... data) {

        String title = String.format(format, data);

        StringBuilder separator = new StringBuilder();
        for (int i = 0; i < title.length(); i++) {
            separator.append("=");
        }
        String line = separator.toString();
        doAppend(line);
        doAppend(title);
        doAppend(line);
    }

    @Override
    public void append(String format, Object... data) {

        doAppend(format, data);
    }

    @Override
    public void appendSectionEnd(String format, Object... data) {

        doAppend("\n");
        String endNote = String.format(format, data);

        StringBuilder separator = new StringBuilder();
        for (int i = 0; i < endNote.length(); i++) {
            separator.append("-");
        }
        String line = separator.toString();
        doAppend(line);
        doAppend(endNote);
        doAppend(line);
    }

    /**
     * Do the internal append.
     * @param format
     * @param data
     */
    private void doAppend(String format, Object... data) {

        if (writer != null && format != null) {
            try {
                writer.append(String.format(format, data));
                writer.append("\n");
            } catch (IOException e) {
                logger.error("Error writing log line: " + String.format(format, data));
            }
        }
    }
}
