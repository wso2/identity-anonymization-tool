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

package org.wso2.carbon.identity;

import org.wso2.carbon.datasource.core.DataSourceManager;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.core.impl.DataSourceServiceImpl;
import org.wso2.carbon.identity.util.NamedPreparedStatement;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * Processor to execute given sql scripts.
 */
public class SQLExecutionProcessor implements Processor<UserSQLQuery> {

    private static final String USERNAME = "username";
    private static final String TENANT_DOMAIN = "tenant_domain";
    private static final String USER_STORE_DOMAIN = "user_store_domain";
    private static final String PSEUDONYM = "pseudonym";

    private DataSource dataSource;

    public SQLExecutionProcessor() throws DataSourceException {

        DataSourceManager dataSourceManager = DataSourceManager.getInstance();
        Path configFilePath = Paths.get("src", "main", "resources", "conf", "datasources");
        DataSourceService dataSourceService = new DataSourceServiceImpl();
        String analyticsDataSourceName = "WSO2_CARBON_DB";

        dataSourceManager.initDataSources(configFilePath.toFile().getAbsolutePath());
        dataSource = (DataSource) dataSourceService.getDataSource(analyticsDataSourceName);
    }

    private void runSQL(UserSQLQuery userSQLQuery) throws SQLException {

        try (Connection connection = dataSource.getConnection()) {
            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection, userSQLQuery
                    .getSqlQuery().toString());
            namedPreparedStatement.setString(USERNAME, userSQLQuery.getUserIdentifier().getUsername());
            namedPreparedStatement.setString(USER_STORE_DOMAIN, userSQLQuery.getUserIdentifier().getUserStoreDomain());
            namedPreparedStatement.setString(TENANT_DOMAIN, userSQLQuery.getUserIdentifier().getTenantDomain());
            namedPreparedStatement.setString(PSEUDONYM, userSQLQuery.getUserIdentifier().getPseudonym());

            namedPreparedStatement.getPreparedStatement().execute();
        }
    }

    @Override
    public void execute(List<UserSQLQuery> list) {

        for (UserSQLQuery userSQLQuery : list) {
            try {
                runSQL(userSQLQuery);
            } catch (SQLException e) {
                return;
            }
        }
    }
}
