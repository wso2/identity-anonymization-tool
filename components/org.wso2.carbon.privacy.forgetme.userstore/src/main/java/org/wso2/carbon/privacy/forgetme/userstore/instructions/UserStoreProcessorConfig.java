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

package org.wso2.carbon.privacy.forgetme.userstore.instructions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;
import org.wso2.carbon.privacy.forgetme.userstore.config.UserStoreHandlerFactory;
import org.wso2.carbon.privacy.forgetme.userstore.exception.UserStoreModuleException;
import org.wso2.carbon.privacy.forgetme.userstore.handler.UserStoreHandler;
import org.wso2.carbon.privacy.forgetme.userstore.util.SecondaryUserStoreReader;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Module config for user stores.
 */
public class UserStoreProcessorConfig implements ProcessorConfig {

    private UserStoreHandlerFactory userStoreHandlerFactory;

    public UserStoreProcessorConfig(Path carbonHome, Map<String, String> properties) {
        this.userStoreHandlerFactory = new UserStoreHandlerFactory(carbonHome, properties);
    }

    public UserStoreHandlerFactory getUserStoreHandlerFactory() {

        return userStoreHandlerFactory;
    }
}
