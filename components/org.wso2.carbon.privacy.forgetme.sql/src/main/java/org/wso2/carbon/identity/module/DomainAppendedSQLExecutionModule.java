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

package org.wso2.carbon.identity.module;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.datasource.core.DataSourceManager;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.core.impl.DataSourceServiceImpl;
import org.wso2.carbon.identity.config.DataSourceConfig;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.identity.exception.SQLModuleException;
import org.wso2.carbon.identity.sql.UserSQLQuery;
import org.wso2.carbon.identity.util.NamedPreparedStatement;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * SQL execution module to execute queries that has domain name appended to the username.
 */
public class DomainAppendedSQLExecutionModule implements Module<UserSQLQuery> {

    private static final String USERNAME = "username";
    private static final String TENANT_DOMAIN = "tenant_domain";
    private static final String PSEUDONYM = "pseudonym";
    private static final String PRIMARY_DOMAIN = "PRIMARY";
    private static final String DOMAIN_SEPARATOR = "/";

    private DataSource dataSource;

    public DomainAppendedSQLExecutionModule(DataSourceConfig dataSourceConfig) throws SQLModuleException {

        try {
            dataSource = dataSourceConfig.getDatasource();
        } catch (SQLModuleException e) {
            throw new SQLModuleException("Error occurred while initializing the data source.", e);
        }
    }

    @Override
    public void execute(UserSQLQuery userSQLQuery) throws ModuleException {

        String username = userSQLQuery.getUserIdentifier().getUsername();
        
        if (StringUtils.contains(username, PRIMARY_DOMAIN)) {
            username = StringUtils.substringAfter(username, DOMAIN_SEPARATOR);
        }

        try (Connection connection = dataSource.getConnection()) {

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection, userSQLQuery
                    .getSqlQuery().toString());

            for (int i = 0; i < userSQLQuery.getNumberOfPlacesToReplace(USERNAME); i++) {
                namedPreparedStatement.setString(USERNAME, username);
            }

            for (int i = 0; i < userSQLQuery.getNumberOfPlacesToReplace(TENANT_DOMAIN); i++) {
                namedPreparedStatement.setString(TENANT_DOMAIN, userSQLQuery.getUserIdentifier().getTenantDomain());
            }

            for (int i = 0; i < userSQLQuery.getNumberOfPlacesToReplace(PSEUDONYM); i++) {
                namedPreparedStatement.setString(PSEUDONYM, userSQLQuery.getUserIdentifier().getPseudonym());
            }
            namedPreparedStatement.getPreparedStatement().execute();
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
    }
}
