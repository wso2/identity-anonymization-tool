package org.wso2.carbon.privacy.forgetme;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResult;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;
import org.wso2.carbon.privacy.forgetme.config.SystemConfig;
import org.wso2.carbon.privacy.forgetme.runtime.CommandlineException;
import org.wso2.carbon.privacy.forgetme.runtime.DefaultEnvironment;
import org.wso2.carbon.privacy.forgetme.runtime.ForgetMeExecutionException;
import org.wso2.carbon.privacy.forgetme.runtime.NestedEnvironment;
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

    private static final Logger logger = LoggerFactory.getLogger(ForgetMeTool.class);

    private static final String CMD_OPTION_CONFIG_DIR = "d";
    private static final String CMD_OPTION_CONFIG_CARBON_HOME = "carbon";
    private static final String CMD_OPTION_CONFIG_USER_NAME = "U";
    private static final String CMD_OPTION_CONFIG_USER_DOMAIN = "D";
    private static final String CMD_OPTION_CONFIG_TENANT_DOMAIN = "T";
    private static final String CMD_OPTION_CONFIG_TENANT_ID = "TID";
    private static final String CMD_OPTION_CONFIG_USER_PSEUDONYM = "pu";

    private static final String DEFAULT_TENANT_DOMAIN = "carbon.super";
    private static final String DEFAULT_TENANT_ID = "-1234";
    private static final String DEFAULT_USER_DOMAIN = "PRIMARY";
    private static final String CARBON_HOME = "CARBON_HOME";

    private static final String COMMAND_NAME = "forget-me";
    private static final String CONFIG_FILE_NAME = "config.json";

    private ForgetMeExecutionEngine forgetMeExecutionEngine;

    public static void main(String[] args) throws Exception {

        Options options = new Options();

        options.addOption(CMD_OPTION_CONFIG_DIR, true, "Directory to scan");
        options.addOption(CMD_OPTION_CONFIG_CARBON_HOME, true, "Carbon Home");
        options.addOption(CMD_OPTION_CONFIG_USER_NAME, true, "User Name");
        options.addOption(CMD_OPTION_CONFIG_USER_DOMAIN, true, "User Domain");
        options.addOption(CMD_OPTION_CONFIG_TENANT_DOMAIN, true, "Tenant Domain");
        options.addOption(CMD_OPTION_CONFIG_TENANT_ID, true, "Tenant ID");
        options.addOption(CMD_OPTION_CONFIG_USER_PSEUDONYM, true, "Pseudonym");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String homeDir;
        if (cmd.hasOption(CMD_OPTION_CONFIG_DIR)) {
            homeDir = cmd.getOptionValue(CMD_OPTION_CONFIG_DIR);
        } else {
            printError(options);
            return;
        }
        UserIdentifier userIdentifier;
        if (cmd.hasOption(CMD_OPTION_CONFIG_USER_NAME)) {
            try {
                userIdentifier = createUserIdentifier(cmd);
            } catch (CommandlineException cpe) {
                //We print the output into stdout.
                System.out.println(cpe.getMessage());
                printError(options);
                return;
            }
        } else {
            printError(options);
            return;
        }

        Environment sysEnvironment = new SystemEnv();
        DefaultEnvironment defaultEnvironment = new DefaultEnvironment();
        polulateEnvironment(defaultEnvironment, cmd);

        NestedEnvironment nestedEnvironment = new NestedEnvironment(sysEnvironment, defaultEnvironment);
        ForgetMeTool forgetMeTool = new ForgetMeTool();
        forgetMeTool.process(homeDir, userIdentifier, nestedEnvironment);
    }

    private static void polulateEnvironment(DefaultEnvironment defaultEnvironment, CommandLine cmd) {

        if (cmd.hasOption(CMD_OPTION_CONFIG_CARBON_HOME)) {
            defaultEnvironment.setProperty(CARBON_HOME, cmd.getOptionValue(CMD_OPTION_CONFIG_CARBON_HOME));
        }
    }

    private static String createPseudonym(String optionValue) {

        String result = optionValue;
        if (StringUtils.isEmpty(optionValue)) {
            result = UUID.randomUUID().toString();
            logger.info("Generating pseudonym as pseudo name is not provided : " + result);
        }
        return result;
    }

    private static void printError(Options options) {

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(COMMAND_NAME, options);
    }

    public ForgetMeResult process(String homeDir, UserIdentifier userIdentifier, Environment environment)
            throws ForgetMeExecutionException {

        ForgetMeResult forgetMeResult;
        ConfigReader configReader = ConfigReader.getInstance();
        try {
            File home = new File(homeDir).getAbsoluteFile().getCanonicalFile();
            SystemConfig systemConfig = configReader.readSystemConfig(new File(home, CONFIG_FILE_NAME), environment);
            systemConfig.setWorkDir(home.toPath());
            forgetMeExecutionEngine = new ForgetMeExecutionEngine(userIdentifier, environment, systemConfig);
            forgetMeResult = forgetMeExecutionEngine.execute();
        } catch (IOException e) {
            throw new ForgetMeExecutionException("Could not load config from directory: " + homeDir, e, "E_INIT", null);
        }
        return forgetMeResult;
    }

    private static UserIdentifier createUserIdentifier(CommandLine cmd) throws CommandlineException {

        String userName = cmd.getOptionValue(CMD_OPTION_CONFIG_USER_NAME);
        String domainName = cmd.getOptionValue(CMD_OPTION_CONFIG_USER_DOMAIN, DEFAULT_USER_DOMAIN);
        String tenantName = cmd.getOptionValue(CMD_OPTION_CONFIG_TENANT_DOMAIN, DEFAULT_TENANT_DOMAIN);
        String tenantId = cmd.getOptionValue(CMD_OPTION_CONFIG_TENANT_ID);
        String pseudonym = createPseudonym(cmd.getOptionValue(CMD_OPTION_CONFIG_USER_PSEUDONYM));

        if (!DEFAULT_TENANT_DOMAIN.equals(tenantName) && StringUtils.isEmpty(tenantId)) {
            throw new CommandlineException("Tenant ID needs to be passed for tenant name : " + tenantName);
        }

        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUsername(userName);
        userIdentifier.setUserStoreDomain(domainName);
        userIdentifier.setTenantDomain(tenantName);
        userIdentifier.setPseudonym(pseudonym);

        if (StringUtils.isNoneEmpty(tenantId)) {
            try {
                int tid = Integer.parseInt(tenantId);
                userIdentifier.setTenantId(tid);
            } catch (NumberFormatException nfe) {
                throw new CommandlineException("Error in converting the number as tenant ID : " + tenantId);
            }
        }

        return userIdentifier;
    }

}
