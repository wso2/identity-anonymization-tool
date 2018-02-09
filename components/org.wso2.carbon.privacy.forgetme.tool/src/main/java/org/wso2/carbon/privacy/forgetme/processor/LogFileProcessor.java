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

package org.wso2.carbon.privacy.forgetme.processor;

import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;

public class LogFileProcessor implements ForgetMeProcessor {

    public ForgetMeResult process(UserIdentity userIdentity, FileInstructionSet instructionSet) {
        ForgetMeResult result = new ForgetMeResult();

        return result;
    }

    public void processLineByLine(File input, File output, Charset charset) throws IOException {
        try (Scanner scanner = new Scanner(input, charset.name());
                BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            while (scanner.hasNextLine()) {
                processLine(scanner.nextLine(), writer);
            }
        }
    }

    private void processLine(String s, BufferedWriter writer) throws IOException {
        writer.write(s);
        writer.newLine();
    }
}
