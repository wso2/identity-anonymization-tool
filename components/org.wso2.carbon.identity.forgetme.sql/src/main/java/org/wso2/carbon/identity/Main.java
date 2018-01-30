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

import org.wso2.carbon.identity.exception.CompliancyToolException;
import org.wso2.carbon.identity.sql.SQLFileReader;
import org.wso2.carbon.identity.sql.SQLQuery;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Main entry point of the application.
 */
public class Main {

    public static void main(String[] args) throws CompliancyToolException {

        SQLFileReader sqlFileReader = new SQLFileReader(Paths.get("components", "org.wso2.carbon.identity.forgetme.sql",
                "src", "main", "resources", "sql"));
        List<SQLQuery> sqlQueries = sqlFileReader.readAllQueries();

        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUsername("6ab705a7-8347-42eb-bf0c-ddc2d7f71765");
        userIdentifier.setUserStoreDomain("PRIMARY");
        userIdentifier.setTenantDomain("-1234");
        userIdentifier.setPseudonym(UUID.randomUUID().toString());

        List<UserSQLQuery> userSQLQueryList = new ArrayList<>();
        for (SQLQuery sqlQuery : sqlQueries) {
            UserSQLQuery userSQLQuery = new UserSQLQuery();
            userSQLQuery.setSqlQuery(sqlQuery);
            userSQLQuery.setUserIdentifier(userIdentifier);
            userSQLQueryList.add(userSQLQuery);
        }

        DataSourceConfig dataSourceConfig = new DataSourceConfig(Paths.get("components",
                "org.wso2.carbon.identity.forgetme.sql", "src", "main", "resources", "conf", "datasources"),
                "WSO2_CARBON_DB");

        Processor<UserSQLQuery> sqlExecutionProcessor = new SQLExecutionProcessor(dataSourceConfig);
        sqlExecutionProcessor.execute(userSQLQueryList);
    }
}
