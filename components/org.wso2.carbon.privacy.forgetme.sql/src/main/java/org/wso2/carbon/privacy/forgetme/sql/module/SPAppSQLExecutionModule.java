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

package org.wso2.carbon.privacy.forgetme.sql.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.privacy.forgetme.sql.config.DataSourceConfig;
import org.wso2.carbon.privacy.forgetme.sql.exception.SQLModuleException;
import org.wso2.carbon.privacy.forgetme.sql.sql.UserSQLQuery;
import org.wso2.carbon.privacy.forgetme.sql.util.NamedPreparedStatement;
import org.wso2.carbon.privacy.forgetme.sql.util.SQLConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * SQL execution module to execute the query related to SP_APPS table
 */
public class SPAppSQLExecutionModule implements Module<Map<String, UserSQLQuery>> {

    private static final Logger log = LoggerFactory.getLogger(SPAppSQLExecutionModule.class);

    private static final String USERNAME = "username";
    private static final String TENANT_ID = "tenant_id";
    private static final String USER_STORE_DOMAIN = "user_store_domain";
    private static final String MODIFIED_APP_NAME = "modifiedAppName";
    private static final String MODIFIED_DESCRIPTION = "modifiedDescription";
    private static final String PSEUDONYM = "pseudonym";

    private DataSource dataSource;

    public SPAppSQLExecutionModule(DataSourceConfig dataSourceConfig) throws SQLModuleException {

        try {
            dataSource = dataSourceConfig.getDatasource();
            if (log.isDebugEnabled()) {
                log.debug("Data source initialized with name: {}.", dataSource.getClass());
            }
        } catch (SQLModuleException e) {
            throw new SQLModuleException("Error occurred while initializing the data source.", e);
        }
    }

    @Override
    public void execute(Map<String, UserSQLQuery> queries) throws ModuleException {

        if (dataSource == null) {
            log.warn("No data source configured for name: " + queries.get(SQLConstants.SELECT_QUERY).getSqlQuery()
                    .getBaseDirectory());
            return;
        }

        String username = queries.get(SQLConstants.SELECT_QUERY).getUserIdentifier().getUsername();

        try (Connection connection = dataSource.getConnection()) {

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection,
                    queries.get(SQLConstants.SELECT_QUERY).getSqlQuery().toString());
            namedPreparedStatement.setString(USERNAME, username);
            namedPreparedStatement
                    .setInt(TENANT_ID, queries.get(SQLConstants.SELECT_QUERY).getUserIdentifier().getTenantId());
            namedPreparedStatement.setString(USER_STORE_DOMAIN,
                    queries.get(SQLConstants.SELECT_QUERY).getUserIdentifier().getUserStoreDomain());

            ResultSet rs = namedPreparedStatement.getPreparedStatement().executeQuery();

            String modifiedApplicationName = null;
            String modifiedDescription = null;
            while (rs.next()) {
                String appName = rs.getString("APP_NAME");
                String description = rs.getString("DESCRIPTION");
                modifiedApplicationName = appName.replaceAll("(^" + username + "_)",
                        queries.get(SQLConstants.SELECT_QUERY).getUserIdentifier().getPseudonym() + "_");
                modifiedDescription = description.replaceAll("(\\s" + username + "_)",
                        " " + queries.get(SQLConstants.SELECT_QUERY).getUserIdentifier().getPseudonym() + "_");
            }

            if (modifiedApplicationName != null && modifiedDescription != null) {
                NamedPreparedStatement namedPreparedStatement2 = new NamedPreparedStatement(connection,
                        queries.get(SQLConstants.UPDATE_QUERY).getSqlQuery().toString());
                namedPreparedStatement2.setString(MODIFIED_APP_NAME, modifiedApplicationName);
                namedPreparedStatement2.setString(MODIFIED_DESCRIPTION, modifiedDescription);
                namedPreparedStatement2.setString(PSEUDONYM,
                        queries.get(SQLConstants.SELECT_QUERY).getUserIdentifier().getPseudonym());
                namedPreparedStatement2.setString(USERNAME, username);
                namedPreparedStatement2.setString(USER_STORE_DOMAIN,
                        queries.get(SQLConstants.SELECT_QUERY).getUserIdentifier().getUserStoreDomain());
                namedPreparedStatement2
                        .setInt(TENANT_ID, queries.get(SQLConstants.SELECT_QUERY).getUserIdentifier().getTenantId());
                namedPreparedStatement2.getPreparedStatement().executeUpdate();
                if (log.isDebugEnabled()) {
                    log.debug("Executed the sql query: {}.",
                            queries.get(SQLConstants.SELECT_QUERY).getSqlQuery().toString());
                }
            }
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
    }
}
