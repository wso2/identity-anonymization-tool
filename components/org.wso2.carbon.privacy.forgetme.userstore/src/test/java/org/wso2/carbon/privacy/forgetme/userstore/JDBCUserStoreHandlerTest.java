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
import org.testng.annotations.Test;
import org.wso2.carbon.privacy.forgetme.userstore.handler.UserStoreHandler;
import org.wso2.carbon.privacy.forgetme.userstore.instructions.UserStoreProcessorConfig;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class JDBCUserStoreHandlerTest {

    private UserStoreProcessorConfig userStoreProcessorConfig;

    @BeforeTest
    public void init() throws Exception {

        Connection connection = null;

        try {

            Path carbonHome = new File("src/test/resources/carbon-home").toPath();

            Map<String, String> properties = new HashMap<>();
            properties.put("org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager", "jdbc-handler");

            userStoreProcessorConfig = new UserStoreProcessorConfig(carbonHome, properties);

            Class.forName("org.h2.Driver");

            String initSQLPath = "src/test/resources/sql/init.sql";

            //Create a connection with an init script to populate data.
            connection = DriverManager.getConnection("jdbc:h2:mem:user_store;INIT=runscript from '"
                    + initSQLPath + "';DB_CLOSE_DELAY=-1");

        } finally {

            if (connection != null) {
                connection.close();
            }
        }
    }

    @Test
    public void testRenameExistingUserInSuperTenant() throws Exception {

        int tenantId = -1234;

        UserStoreHandler handler = userStoreProcessorConfig.getUserStoreHandlerFactory().getUserStoreHandler(tenantId,
                "PRIMARY");

        String existingUsername = "user1";
        String newUsername = "user2";

        assertFalse(isUserExists(newUsername, tenantId));

        handler.renameUser(existingUsername, newUsername);

        assertTrue(isUserExists(newUsername, tenantId));
    }

    @Test
    public void testRenameExistingUserInNonSuperTenant() throws Exception {

        int tenantId = 1;

        UserStoreHandler handler = userStoreProcessorConfig.getUserStoreHandlerFactory().getUserStoreHandler(tenantId,
                "PRIMARY");

        String existingUsername = "user1";
        String newUsername = "user2";

        assertFalse(isUserExists(newUsername, tenantId));

        handler.renameUser(existingUsername, newUsername);

        assertTrue(isUserExists(newUsername, tenantId));
    }

    @Test
    public void testRenameNonExistingUser() throws Exception {

        int tenantId = -1234;

        UserStoreHandler handler = userStoreProcessorConfig.getUserStoreHandlerFactory().getUserStoreHandler(tenantId,
                "PRIMARY");

        String existingUsername = "non-existing-user";
        String newUsername = "user3";

        assertFalse(isUserExists(newUsername, tenantId));

        handler.renameUser(existingUsername, newUsername);

        assertFalse(isUserExists(newUsername, tenantId));
    }

    private boolean isUserExists(String username, int tenantId) throws Exception {

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {

            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:mem:user_store;DB_CLOSE_DELAY=-1");

            statement = connection.prepareStatement("SELECT COUNT(*) from UM_USER WHERE UM_USER_NAME=? " +
                    "AND UM_TENANT_ID=?");
            statement.setString(1, username);
            statement.setInt(2, tenantId);

            resultSet = statement.executeQuery();

            while (resultSet.next()) {

                int count = resultSet.getInt(1);

                return count == 1 ? true : false;
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new Exception(e);
        } finally {

            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                connection.close();
            }

        }
        return false;
    }
}
