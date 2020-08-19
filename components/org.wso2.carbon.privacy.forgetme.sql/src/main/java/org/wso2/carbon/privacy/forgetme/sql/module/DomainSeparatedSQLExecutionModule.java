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

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * SQL execution module to execute queries that has domain name separated (In a different column) from the username.
 */
public class DomainSeparatedSQLExecutionModule implements Module<UserSQLQuery> {

    private static final Logger log = LoggerFactory.getLogger(DomainSeparatedSQLExecutionModule.class);

    private static final String USERNAME = "username";
    private static final String TENANT_ID = "tenant_id";
    private static final String USER_STORE_DOMAIN = "user_store_domain";
    private static final String PSEUDONYM = "pseudonym";

    private DataSource dataSource;

    public DomainSeparatedSQLExecutionModule(DataSourceConfig dataSourceConfig) throws SQLModuleException {

        try {
            dataSource = dataSourceConfig.getDatasource();
            if ( dataSource != null && log.isDebugEnabled()) {
                log.debug("Data source initialized with name: {}.", dataSource.getClass());
            }
        } catch (SQLModuleException e) {
            throw new SQLModuleException("Error occurred while initializing the data source.", e);
        }
    }

    @Override
    public void execute(UserSQLQuery userSQLQuery) throws ModuleException {

        if (dataSource == null) {
            log.warn("No data source configured for name: " + userSQLQuery.getSqlQuery().getBaseDirectory());
            return;
        }

        try (Connection connection = dataSource.getConnection()) {

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection,
                    userSQLQuery.getSqlQuery().toString());

            for (int i = 0; i < userSQLQuery.getNumberOfPlacesToReplace(USERNAME); i++) {
                namedPreparedStatement.setString(USERNAME, userSQLQuery.getUserIdentifier().getUsername());
            }

            for (int i = 0; i < userSQLQuery.getNumberOfPlacesToReplace(USER_STORE_DOMAIN); i++) {
                namedPreparedStatement.setString(USER_STORE_DOMAIN, userSQLQuery.getUserIdentifier()
                        .getUserStoreDomain());
            }

            for (int i = 0; i < userSQLQuery.getNumberOfPlacesToReplace(TENANT_ID); i++) {
                namedPreparedStatement.setInt(TENANT_ID, userSQLQuery.getUserIdentifier().getTenantId());
            }

            for (int i = 0; i < userSQLQuery.getNumberOfPlacesToReplace(PSEUDONYM); i++) {
                namedPreparedStatement.setString(PSEUDONYM, userSQLQuery.getUserIdentifier().getPseudonym());
            }

            namedPreparedStatement.getPreparedStatement().execute();

            if (log.isDebugEnabled()) {
                log.debug("Executed the sql query: {}.", userSQLQuery.getSqlQuery().toString());
            }
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
    }
}
