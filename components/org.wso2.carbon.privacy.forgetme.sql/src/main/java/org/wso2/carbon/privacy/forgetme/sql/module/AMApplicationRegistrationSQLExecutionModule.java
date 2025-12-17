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

import org.apache.commons.lang3.StringUtils;
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
 * SQL execution module to execute the query related to AM_APPLICATION_REGISTRATION table
 */
public class AMApplicationRegistrationSQLExecutionModule implements Module<Map<String, UserSQLQuery>> {

    private static final Logger log = LoggerFactory.getLogger(AMApplicationRegistrationSQLExecutionModule.class);

    private static final String TENANT_ID = "tenant_id";
    private static final String MODIFIED_STRING = "modifiedString";
    private static final String SUBSCRIBER_ID = "subscriberId";
    private static final String SUPER_TENANT = "carbon.super";
    private static final String TENANT_DOMAIN_SEPARATOR = "@";

    private DataSource dataSource;

    public AMApplicationRegistrationSQLExecutionModule(DataSourceConfig dataSourceConfig) throws SQLModuleException {

        try {
            dataSource = dataSourceConfig.getDatasource();
            if (dataSource != null && log.isDebugEnabled()) {
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

        // If this user belongs to a tenant we should append the tenant domain name to username in certain queries.
        if (!StringUtils
                .equals(queries.get(SQLConstants.SELECT_QUERY).getUserIdentifier().getTenantDomain(), SUPER_TENANT)) {
            username = username + TENANT_DOMAIN_SEPARATOR + queries.get(SQLConstants.SELECT_QUERY).getUserIdentifier()
                    .getTenantDomain();
        }

        try (Connection connection = dataSource.getConnection()) {

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection,
                    queries.get(SQLConstants.SELECT_QUERY).getSqlQuery().toString());
            namedPreparedStatement
                    .setInt(TENANT_ID, queries.get(SQLConstants.SELECT_QUERY).getUserIdentifier().getTenantId());

            ResultSet rs = namedPreparedStatement.getPreparedStatement().executeQuery();

            String modifiedInputString = null;
            int subscriberId = -1;
            while (rs.next()) {
                String inputs = rs.getString("INPUTS");
                subscriberId = rs.getInt("SUBSCRIBER_ID");
                modifiedInputString = inputs.replaceAll("username\":\"" + username + "\"",
                        "username\":\"" + queries.get(SQLConstants.SELECT_QUERY).getUserIdentifier().getPseudonym()
                                + "\"");
            }

            if (modifiedInputString != null && subscriberId != -1) {
                NamedPreparedStatement namedPreparedStatement2 = new NamedPreparedStatement(connection,
                        queries.get(SQLConstants.UPDATE_QUERY).getSqlQuery().toString());
                namedPreparedStatement2.setString(MODIFIED_STRING, modifiedInputString);
                namedPreparedStatement2.setInt(SUBSCRIBER_ID, subscriberId);
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
