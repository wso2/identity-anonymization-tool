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

package org.wso2.carbon.privacy.forgetme.userstore.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.datasource.core.DataSourceManager;
import org.wso2.carbon.datasource.core.beans.CarbonDataSource;
import org.wso2.carbon.datasource.core.beans.DataSourceMetadata;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.privacy.forgetme.userstore.exception.UserStoreModuleException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * Performs user store operations against a JDBC user store.
 *
 * <b>NOTE:</b>
 * The logic of getting the applicable database connection details has been rewritten as the existing carbon components
 * can't be re-used outside the OSGi environment.
 *
 */
public class JDBCUserStoreHandler extends UserStoreHandler {

    private static final Log log = LogFactory.getLog(JDBCUserStoreHandler.class);

    private static final String USER_STORE_PROPERTY_DB_URL = "url";
    private static final String USER_STORE_PROPERTY_DB_USERNAME = "userName";
    private static final String USER_STORE_PROPERTY_DB_PASSWORD = "password";
    private static final String USER_STORE_PROPERTY_DB_DRIVER_NAME = "driverName";
    private static final String USER_STORE_PROPERTY_DATA_SOURCE = "dataSource";
    private static final String CARBON_DATA_SOURCES_DIRECTORY = "repository/conf/datasources/";

    private static final String HANDLER_PROPERTY_USERNAME_RENAME_QUERY = "rename.query";
    private static final String USERNAME_RENAME_QUERY = "UPDATE UM_USER SET UM_USER_NAME=?" +
            " WHERE UM_USER_NAME=? AND UM_TENANT_ID=?";


    @Override
    public String getName() {

        return "jdbc-handler";
    }

    /**
     * Renames the user in the underlying JDBC user store.
     *
     * @param currentName The existing username.
     * @param newName     The new username.
     * @throws UserStoreModuleException
     */
    @Override
    public void renameUser(String currentName, String newName) throws UserStoreModuleException {

        String query = USERNAME_RENAME_QUERY;

        // If a username rename property is defined as an handler property, use it.
        String queryAsHandlerProperty = getProperty(HANDLER_PROPERTY_USERNAME_RENAME_QUERY);
        if(StringUtils.isNotBlank(queryAsHandlerProperty)){
            query = queryAsHandlerProperty;
        }

        if(log.isDebugEnabled()){
            log.debug(String.format("Username rename query : '%s'", query));
        }

        try(Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)
            ){

            preparedStatement.setString(1, newName);
            preparedStatement.setString(2, currentName);
            preparedStatement.setInt(3, getTenantId());

            preparedStatement.execute();

        } catch (SQLException | ClassNotFoundException | DataSourceException | UserStoreModuleException e) {
            throw new UserStoreModuleException("An error occurred while renaming a user in an JDBC user store.", e);
        }
    }

    private Connection getConnection() throws SQLException, ClassNotFoundException,
            DataSourceException, UserStoreModuleException {

        if (isDatabaseConfiguredInline()) {

            if (log.isDebugEnabled()) {
                log.debug("Found database access details as inline configurations.");
            }

            return getConnectionFromInlineConfiguration();
        } else if (isDataSourceConfiguredInUserStoreConfiguration()) {

            if (log.isDebugEnabled()) {
                log.debug("Found database access details as the data source in user store configurations.");
            }

            return getConnectionFromUserStoreDataSource();
        } else {

            if (log.isDebugEnabled()) {
                log.debug("Found database access details as the data source in realm configurations.");
            }

            return getConnectionFromRealmDataSource();
        }
    }

    private boolean isDatabaseConfiguredInline() {

        return StringUtils.isNotBlank(getRealmConfiguration().getUserStoreProperty(USER_STORE_PROPERTY_DB_URL));
    }

    private Connection getConnectionFromInlineConfiguration() throws ClassNotFoundException, SQLException {

        String url = getRealmConfiguration().getUserStoreProperty(USER_STORE_PROPERTY_DB_URL);
        String userName = getRealmConfiguration().getUserStoreProperty(USER_STORE_PROPERTY_DB_USERNAME);
        String password = getRealmConfiguration().getUserStoreProperty(USER_STORE_PROPERTY_DB_PASSWORD);
        String driverName = getRealmConfiguration().getUserStoreProperty(USER_STORE_PROPERTY_DB_DRIVER_NAME);

        Class.forName(driverName);
        return DriverManager.getConnection(url, userName, password);
    }

    private boolean isDataSourceConfiguredInUserStoreConfiguration() {

        return StringUtils.isNotBlank(getRealmConfiguration().getUserStoreProperty(USER_STORE_PROPERTY_DATA_SOURCE));
    }

    private Connection getConnectionFromUserStoreDataSource() throws DataSourceException, UserStoreModuleException, SQLException {

        String dataSourceName = getRealmConfiguration().getUserStoreProperty(USER_STORE_PROPERTY_DATA_SOURCE);
        DataSource dataSource = getDataSourceByJNDIName(dataSourceName);
        return dataSource.getConnection();
    }

    private Connection getConnectionFromRealmDataSource() throws DataSourceException, UserStoreModuleException, SQLException {

        String dataSourceName = getRealmConfiguration().getRealmProperty(USER_STORE_PROPERTY_DATA_SOURCE);
        DataSource dataSource = getDataSourceByJNDIName(dataSourceName);
        return dataSource.getConnection();
    }

    private DataSource getDataSourceByJNDIName(String jndiName) throws DataSourceException, UserStoreModuleException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Looking for the data source for the JNDI name : '%s'", jndiName));
        }

        DataSourceManager dataSourceManager = DataSourceManager.getInstance();
        dataSourceManager.initDataSources(getCarbonHome().resolve(CARBON_DATA_SOURCES_DIRECTORY).toString());

        if (dataSourceManager.getDataSourceRepository() != null) {

            List<DataSourceMetadata> metadata = dataSourceManager.getDataSourceRepository().getMetadata();

            // We need to iterate through the data source metadata to find the data source name for the JNDI name and
            // then fetch the data source from the repository.
            CarbonDataSource userStoreDataStore = null;
            for (DataSourceMetadata m : metadata) {
                if (m.getJndiConfig().getName().equals(jndiName)) {
                    userStoreDataStore = dataSourceManager.getDataSourceRepository().getDataSource(m.getName());
                }
            }

            if (userStoreDataStore != null) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Found the data source for the JNDI name : '%s'", jndiName));
                }
                return (DataSource) userStoreDataStore.getDataSourceObject();
            } else {
                throw new UserStoreModuleException(String.format("Data source for the JNDI name '%s'can't be found.",
                        jndiName));
            }
        } else {
            throw new UserStoreModuleException(String.format("Data source manager is not initialized " +
                    "to get the data source for the JNDI name '%s'.", jndiName));
        }
    }
}
