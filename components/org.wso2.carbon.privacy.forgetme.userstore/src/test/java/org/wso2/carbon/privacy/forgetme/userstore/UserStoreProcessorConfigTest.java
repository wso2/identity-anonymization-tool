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

package org.wso2.carbon.privacy.forgetme.userstore;

import org.testng.annotations.BeforeTest;
import org.wso2.carbon.privacy.forgetme.userstore.handler.JDBCUserStoreHandler;
import org.wso2.carbon.privacy.forgetme.userstore.handler.LDAPUserStoreHandler;
import org.wso2.carbon.privacy.forgetme.userstore.handler.UserStoreHandler;
import org.wso2.carbon.privacy.forgetme.userstore.instructions.UserStoreProcessorConfig;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class UserStoreProcessorConfigTest {

    private static final String USER_STORE_PROPERTY_DOMAIN_NAME = "DomainName";
    UserStoreProcessorConfig userStoreProcessorConfig;

    @BeforeTest
    public void init() {

        Path carbonHome = new File("src/test/resources/carbon-home").toPath();

        Map<String, String> properties = new HashMap<>();
        properties.put("handler-mapping;org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager", "read-write-ldap-handler");
        properties.put("handler-mapping;org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager", "jdbc-handler");

        userStoreProcessorConfig = new UserStoreProcessorConfig(carbonHome, properties);

    }

    @org.testng.annotations.Test
    public void testGetUserStoreHandlerForPrimaryUserStore() throws Exception {

        String userStoreDomainName = "PRIMARY";

        UserStoreHandler handler = userStoreProcessorConfig.getUserStoreHandlerFactory().getUserStoreHandler(
                -1234, userStoreDomainName);
        assertEquals(handler.getClass().getName(), JDBCUserStoreHandler.class.getName());
        assertEquals(handler.getRealmConfiguration().getUserStoreProperty(USER_STORE_PROPERTY_DOMAIN_NAME),
                userStoreDomainName);

    }

    @org.testng.annotations.Test
    public void testGetUserStoreHandlerForSuperTenantSecondaryUserStore() throws Exception {

        String userStoreDomainName = "SECONDARYLDAP";

        UserStoreHandler handler = userStoreProcessorConfig.getUserStoreHandlerFactory().getUserStoreHandler(
                -1234, userStoreDomainName);

        assertEquals(handler.getClass().getName(), LDAPUserStoreHandler.class.getName());
        assertEquals(handler.getRealmConfiguration().getUserStoreProperty(USER_STORE_PROPERTY_DOMAIN_NAME),
                userStoreDomainName);

    }

    @org.testng.annotations.Test
    public void testGetUserStoreHandlerForTenantSecondaryUserStore() throws Exception {

        String userStoreDomainName = "SEONDARYT1LDAP";

        UserStoreHandler handler = userStoreProcessorConfig.getUserStoreHandlerFactory().getUserStoreHandler(
                1, userStoreDomainName);

        assertEquals(handler.getClass().getName(), LDAPUserStoreHandler.class.getName());
        assertEquals(handler.getRealmConfiguration().getUserStoreProperty(USER_STORE_PROPERTY_DOMAIN_NAME),
                userStoreDomainName);
    }

}
