package org.wso2.carbon.privacy.forgetme;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResult;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;
import org.wso2.carbon.privacy.forgetme.config.SystemConfig;
import org.wso2.carbon.privacy.forgetme.runtime.ForgetMeExecutionException;
import org.wso2.carbon.privacy.forgetme.runtime.SystemEnv;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Processes the forget Me request from the external user.
 * Delegates the forget me tasks to different subsystems.
 *
 */
public class ForgetMeTool {

    private ForgetMeExecutionEngine forgetMeExecutionEngine;

    public static void main(String[] args) throws Exception {

        Options options = new Options();

        options.addOption("d", true, "Directory to scan");
        options.addOption("ch", true, "Carbon Home");
        options.addOption("U", true, "User Name");
        options.addOption("D", true, "User Domain");
        options.addOption("T", true, "Tenant Domain");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String homeDir = null;
        if (cmd.hasOption("d")) {
            homeDir = cmd.getOptionValue("d");

        } else {
            printError(options);
            return;
        }
        UserIdentifier userIdentifier = null;
        if (cmd.hasOption("U")) {
            String userName = cmd.getOptionValue("U");
            String domainName = cmd.getOptionValue("D", "PRIMARY");
            String tenantName = cmd.getOptionValue("T", "-1234");
            userIdentifier = createUserIdentifier(userName, domainName, tenantName);
        } else {
            printError(options);
            return;
        }
        ForgetMeTool forgetMeTool = new ForgetMeTool();
        forgetMeTool.process(homeDir, userIdentifier);
    }

    private static void printError(Options options) {

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("forget-me", options);
    }

    public ForgetMeResult process(String homeDir, UserIdentifier userIdentifier) throws ForgetMeExecutionException {

        ForgetMeResult forgetMeResult;
        ConfigReader configReader = ConfigReader.getInstance();
        Environment environment = new SystemEnv();
        try {
            File home = new File(homeDir).getAbsoluteFile().getCanonicalFile();
            SystemConfig systemConfig = configReader.readSystemConfig(new File(home, "config.json"));
            forgetMeExecutionEngine = new ForgetMeExecutionEngine(userIdentifier, environment, systemConfig);
            forgetMeResult = forgetMeExecutionEngine.execute();
        } catch (IOException e) {
            throw new ForgetMeExecutionException("Could not load config from directory: " + homeDir, e, "E_INIT", null);
        }
        return forgetMeResult;
    }

    private static UserIdentifier createUserIdentifier(String userName, String domainName, String tenantName) {

        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUsername(userName);
        userIdentifier.setUserStoreDomain(domainName);
        userIdentifier.setTenantDomain(tenantName);
        userIdentifier.setPseudonym(UUID.randomUUID().toString());
        return userIdentifier;
    }

}
