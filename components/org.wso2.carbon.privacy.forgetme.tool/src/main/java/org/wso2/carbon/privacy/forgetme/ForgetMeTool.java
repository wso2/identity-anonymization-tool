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

package org.wso2.carbon.privacy.forgetme;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Processes the forget Me request from the external user.
 * Delegates the forget me tasks to different subsystems.
 *
 */
public class ForgetMeTool {

    private static final Logger log = LoggerFactory.getLogger(ForgetMeTool.class);

    private static final String CMD_OPTION_CONFIG_DIR = "d";
    private static final String CMD_OPTION_CONFIG_CARBON_HOME = "carbon";
    private static final String CMD_OPTION_CONFIG_USER_NAME = "U";
    private static final String CMD_OPTION_CONFIG_USER_DOMAIN = "D";
    private static final String CMD_OPTION_CONFIG_TENANT_DOMAIN = "T";
    private static final String CMD_OPTION_CONFIG_TENANT_ID = "TID";
    private static final String CMD_OPTION_CONFIG_USER_PSEUDONYM = "pu";
    private static final String CMD_OPTION_HELP = "help";
    private static final String CMD_OPTION_ENABLE_SHA256_HASHING = "sha256";

    private static final String DEFAULT_TENANT_DOMAIN = "carbon.super";
    private static final String DEFAULT_TENANT_ID = "-1234";
    private static final String DEFAULT_USER_DOMAIN = "PRIMARY";
    private static final String CARBON_HOME = "CARBON_HOME";

    private static final String COMMAND_NAME = "forget-me";
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final String CONF_DIRECTORY = "/conf";

    private ForgetMeExecutionEngine forgetMeExecutionEngine;

    public static void main(String[] args) throws Exception {

        Options options = new Options();

        options.addOption(CMD_OPTION_CONFIG_DIR, true, "Directory where config.json file located (mandatory)");
        options.addOption(CMD_OPTION_HELP, false, "Help");
        options.addOption(CMD_OPTION_CONFIG_CARBON_HOME, true, "Carbon Home (optional)");
        options.addOption(CMD_OPTION_CONFIG_USER_NAME, true, "User Name (mandatory)");
        options.addOption(CMD_OPTION_CONFIG_USER_DOMAIN, true, "User Domain (optional, default: PRIMARY");
        options.addOption(CMD_OPTION_CONFIG_TENANT_DOMAIN, true, "Tenant Domain (optional, default: carbon.super)");
        options.addOption(CMD_OPTION_CONFIG_TENANT_ID, true, "Tenant ID (optional. default: -1234)");
        options.addOption(CMD_OPTION_CONFIG_USER_PSEUDONYM, true,
                "Pseudonym, which the user name to be replaced with (optional)");
        options.addOption(CMD_OPTION_ENABLE_SHA256_HASHING, false, "Enable SHA256 hashing for anonymizing the ID attribute (optional)");

        if (System.getProperty("carbon.components.dir.path") != null) {
            addJarFileUrls(new File(System.getProperty("carbon.components.dir.path")));
        }


        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption(CMD_OPTION_HELP)) {
            emitHelp(System.out);
            return;
        }

        String homeDir;
        if (cmd.hasOption(CMD_OPTION_CONFIG_DIR)) {
            homeDir = cmd.getOptionValue(CMD_OPTION_CONFIG_DIR);
        } else {
            homeDir = Paths.get(System.getProperty("user.dir")).getParent().toString() + CONF_DIRECTORY;
        }
        UserIdentifier userIdentifier;
        if (cmd.hasOption(CMD_OPTION_CONFIG_USER_NAME)) {
            try {
                userIdentifier = createUserIdentifier(cmd);
            } catch (CommandlineException cpe) {

                // We print the output into stdout.
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
        populateEnvironment(defaultEnvironment, cmd);

        NestedEnvironment nestedEnvironment = new NestedEnvironment(sysEnvironment, defaultEnvironment);
        ForgetMeTool forgetMeTool = new ForgetMeTool();
        forgetMeTool.process(homeDir, userIdentifier, nestedEnvironment);
    }

    /**
     * Writes the help content to the output stream.
     *
     * @param out
     */
    private static void emitHelp(PrintStream out) {

        InputStream inputStream = ForgetMeTool.class.getClassLoader().getResourceAsStream("help.md");
        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
        WritableByteChannel writableByteChannel = Channels.newChannel(out);
        ByteBuffer buffer = ByteBuffer.allocateDirect(512);
        try {
            while (readableByteChannel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    writableByteChannel.write(buffer);
                }
                buffer.clear();
            }
        } catch (IOException e) {
            log.error("Could not read the help file.");
        }
    }

    private static void populateEnvironment(DefaultEnvironment defaultEnvironment, CommandLine cmd) {

        if (cmd.hasOption(CMD_OPTION_CONFIG_CARBON_HOME)) {
            defaultEnvironment.setProperty(CARBON_HOME, cmd.getOptionValue(CMD_OPTION_CONFIG_CARBON_HOME));
        }
    }

    private static String createPseudonym(CommandLine cmd) {

        String pseudonym = cmd.getOptionValue(CMD_OPTION_CONFIG_USER_PSEUDONYM);
        if (StringUtils.isEmpty(pseudonym) && cmd.hasOption(CMD_OPTION_ENABLE_SHA256_HASHING)) {
            String userName = cmd.getOptionValue(CMD_OPTION_CONFIG_USER_NAME);
            pseudonym = DigestUtils.sha256Hex(userName);
            log.info("Generated SHA256 hash for the given ID attribute : " + pseudonym);
        } else if (StringUtils.isEmpty(pseudonym)) {
            pseudonym = UUID.randomUUID().toString();
            log.info("Generating pseudonym as pseudo name is not provided : " + pseudonym);
        }
        return pseudonym;
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
        String pseudonym = createPseudonym(cmd);

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

    /**
     * Add JAR files found in the given directory to the Classpath. This fix is done due to terminal's argument character limitation.
     *
     * @param root the directory to recursively search for JAR files.
     * @throws java.net.MalformedURLException If a provided JAR file URL is malformed
     */
    private static void addJarFileUrls(File root) throws Exception {
        File[] children = root.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isFile() && child.canRead() &&
                    child.getName().toLowerCase().endsWith(".jar") && !child.getName().contains("slf4j")) {
                addPath(child.getPath());
            }
        }
    }

    private static void addPath(String s) throws Exception {
        File f = new File(s);
        URL u = f.toURL();
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> urlClass = URLClassLoader.class;
        Method method = urlClass.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(urlClassLoader, u);
    }



}
