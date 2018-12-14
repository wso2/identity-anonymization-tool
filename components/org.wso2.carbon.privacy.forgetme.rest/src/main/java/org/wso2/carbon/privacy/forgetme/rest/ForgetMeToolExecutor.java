/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

package org.wso2.carbon.privacy.forgetme.rest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.privacy.forgetme.rest.domain.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The class which executes the forget me tool script with needed arguments.
 * <p>
 * The applicable script file (.sh or .bat) is picked based on the OS and run as a separate process.
 */
public class ForgetMeToolExecutor {

    private final static Log log = LogFactory.getLog(ForgetMeToolExecutor.class);

    private static final String SYSTEM_PROPERTY_CARBON_HOME = "carbon.home";
    private static final String FORGETME_TOOL_HOME = "repository/components/tools/forget-me";
    private static final String SCRIPT_NAME_BASH = "forgetme.sh";
    private static final String SCRIPT_NAME_WINDOWS = "forgetme.bat";
    private static final String SYSTEM_PROPERTY_OS_NAME = "os.name";
    private static final String USER_STORE_EXTENSION_CONF_RELATIVE_PATH = "extensions/user-store/conf";

    public void execute(Integer tenantId, String tenantDomain, String userStoreDomain, String username, User user)
            throws Exception {

        String scriptName = getScriptForOS();

        if (scriptName == null) {
            throw new Exception(String.format("The operating system '%s' doesn't support the forget-me tool script.",
                    System.getProperty(SYSTEM_PROPERTY_OS_NAME)));
        }

        String scriptPath = new File(System.getProperty(SYSTEM_PROPERTY_CARBON_HOME)).toPath().resolve(
                FORGETME_TOOL_HOME + "/bin/" + scriptName).toString();

        String arguments = buildForgetMeToolArguments(tenantId, tenantDomain, userStoreDomain, username, user);

        Process process = Runtime.getRuntime().exec(scriptPath + " " + arguments);

        if (log.isInfoEnabled()) {
            log.info(IOUtils.toString(process.getInputStream()));
        }
    }

    private String getScriptForOS() {

        String osName = System.getProperty(SYSTEM_PROPERTY_OS_NAME);

        String scriptName = null;

        if (isUnixOS(osName) || isMacOS(osName)) {
            scriptName = SCRIPT_NAME_BASH;
        } else if (isWindowsOS(osName)) {
            scriptName = SCRIPT_NAME_WINDOWS;
        }

        return scriptName;
    }

    private boolean isWindowsOS(String osName) {

        return osName.toUpperCase().indexOf("WIN") >= 0;
    }

    public boolean isUnixOS(String osName) {

        return (osName.toUpperCase().indexOf("NIX") >= 0 ||
                osName.toUpperCase().indexOf("NUX") >= 0 ||
                osName.toUpperCase().indexOf("AIX") > 0);

    }

    public static boolean isMacOS(String osName) {

        return (osName.toUpperCase().indexOf("MAC") >= 0);

    }

    private String buildForgetMeToolArguments(Integer tenantId, String tenantDomain,
                                              String userStoreDomain, String username, User user) {

        List<String> args = new ArrayList<>();

        args.add("-d");
        String configDirectoryPath = new File(System.getProperty(SYSTEM_PROPERTY_CARBON_HOME)).toPath().resolve(
                FORGETME_TOOL_HOME + "/" + USER_STORE_EXTENSION_CONF_RELATIVE_PATH).toString();
        args.add(configDirectoryPath);

        args.add("-U");
        args.add(username);

        args.add("-pu");
        args.add(user.getUsername());

        args.add("-TID");
        args.add(String.valueOf(tenantId));

        args.add("-T");
        args.add(tenantDomain);

        args.add("-D");
        args.add(userStoreDomain);

        String arguments = "";
        for (String arg : args) {
            arguments = arguments + " " + arg;
        }

        return arguments;
    }
}
