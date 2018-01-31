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
