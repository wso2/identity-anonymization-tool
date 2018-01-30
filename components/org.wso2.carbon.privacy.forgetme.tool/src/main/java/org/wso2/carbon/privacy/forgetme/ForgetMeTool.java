package org.wso2.carbon.privacy.forgetme;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResultSet;
import org.wso2.carbon.privacy.forgetme.runtime.SystemEnv;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Processes the forget Me request from the external user.
 * Delegates the forget me tasks to different subsystems.
 *
 */
public class ForgetMeTool {

    public static void main(String[] args) throws Exception {
        Options options = new Options();

        options.addOption("d", true, "Directory to scan");
        options.addOption("ch", true, "Carbon Home");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("d")) {
            String homeDir = cmd.getOptionValue("d");
            ForgetMeTool forgetMeTool = new ForgetMeTool();
            forgetMeTool.process(homeDir);
        } else {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("forget-me", options);
        }
    }

    public ForgetMeResultSet process(String homeDir) {

        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUsername("admin");
        userIdentifier.setUserStoreDomain("PRIMARY");
        userIdentifier.setTenantDomain("-1234");
        userIdentifier.setPseudonym(UUID.randomUUID().toString());

        ForgetMeResultSet forgetMeResultSet = new ForgetMeResultSet();
        try {
            File home = new File(homeDir).getAbsoluteFile().getCanonicalFile();
            if (!home.isDirectory()) {
                //TODO:
                System.out.println("Can not proceed as the given directory is not a real directory : " + home);
            }

            List<ForgetMeInstruction> forgetMeInstructions = scan(home);

            Environment environment = new SystemEnv();
            forgetMeInstructions.stream().forEach(i -> i.execute(environment, userIdentifier));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return forgetMeResultSet;
    }

    private List<ForgetMeInstruction> scan(File home) {
        List<ForgetMeInstruction> forgetMeInstructions = new ArrayList<>();
        List<ForgetMeInstruction> fileInstructions = scanFileConfigs(home);
        forgetMeInstructions.addAll(fileInstructions);
        String[] directories = home.list((current, name) -> new File(current, name).isDirectory());
        for (String moduleDir : directories) {
            System.out.println("Scanning dir : " + moduleDir);
            File subDir = new File(home, moduleDir);
            List<ForgetMeInstruction> subInstructions = scan(subDir);
            forgetMeInstructions.addAll(subInstructions);
        }
        return forgetMeInstructions;
    }

    private List<ForgetMeInstruction> scanFileConfigs(File home) {
        List<ForgetMeInstruction> forgetMeInstructions = new ArrayList<>();
        String[] files = home.list((current, name) -> {
            File f = new File(current, name);
            return f.isFile() && name.endsWith(".json");
        });

        for (String fileName : files) {
            File file = new File(home, fileName);
            System.out.println("Loading file : " + file.getAbsolutePath());
            ForgetMeInstruction instruction = ConfigReader.getInstance().loadInstruction(file);
            if (instruction != null) {
                forgetMeInstructions.add(instruction);
            }
        }

        return forgetMeInstructions;
    }
}
