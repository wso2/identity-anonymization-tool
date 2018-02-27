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

package org.wso2.carbon.privacy.forgetme.sql.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;
import org.wso2.carbon.privacy.forgetme.sql.config.DataSourceConfig;
import org.wso2.carbon.privacy.forgetme.sql.exception.SQLModuleException;
import org.wso2.carbon.privacy.forgetme.sql.instructions.DatasourceProcessorConfigReader;
import org.wso2.carbon.privacy.forgetme.sql.module.AMApplicationRegistrationSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.module.DomainAppendedSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.module.IDNOauthConsumerAppsSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.module.Module;
import org.wso2.carbon.privacy.forgetme.sql.module.SPAppSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.module.TenantAppendedSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.module.TenantSpecificAppendedSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.sql.SQLFileReader;
import org.wso2.carbon.privacy.forgetme.sql.sql.SQLQuery;
import org.wso2.carbon.privacy.forgetme.sql.sql.UserSQLQuery;
import org.wso2.carbon.privacy.forgetme.sql.instructions.DatasourceProcessorConfig;
import org.wso2.carbon.privacy.forgetme.sql.module.DomainSeparatedSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.util.SQLConstants;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Unit test for simple Main.
 */
public class MainTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MainTest(String testName) {

        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(MainTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws SQLModuleException {

        SQLFileReader sqlFileReader = new SQLFileReader(Paths.get("components", "org.wso2.carbon.privacy.forgetme.sql",
                "src", "main", "resources", "sql"));
        Map<String, SQLQuery> sqlQueries;

        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUsername("admin");
        userIdentifier.setUserStoreDomain("PRIMARY");
        userIdentifier.setTenantDomain("carbon.super");
        userIdentifier.setPseudonym(UUID.randomUUID().toString());

        DatasourceProcessorConfigReader reader = new DatasourceProcessorConfigReader();
        DatasourceProcessorConfig processorConfig = reader.readProcessorConfig(
                Paths.get("components", "org.wso2.carbon.privacy.forgetme.sql", "src", "main", "resources", "conf",
                        "datasources"), new HashMap<>());
        DataSourceConfig dataSourceConfig = processorConfig.getDataSourceConfig("WSO2_CARBON_DB");

        try {
            sqlQueries = sqlFileReader.readAllQueries();

            for (Map.Entry<String, SQLQuery> entry : sqlQueries.entrySet()) {
                SQLQuery sqlQuery = entry.getValue();
                UserSQLQuery userSQLQuery = new UserSQLQuery();
                userSQLQuery.setSqlQuery(sqlQuery);
                userSQLQuery.setUserIdentifier(userIdentifier);

                Module<UserSQLQuery> sqlExecutionModule;
                Module<Map<String, UserSQLQuery>> extendedExecutionModule;
                switch (sqlQuery.getSqlQueryType()) {
                    case DOMAIN_APPENDED:
                        sqlExecutionModule = new DomainAppendedSQLExecutionModule(dataSourceConfig);
                        sqlExecutionModule.execute(userSQLQuery);
                        break;
                    case DOMAIN_SEPARATED:
                        sqlExecutionModule = new DomainSeparatedSQLExecutionModule(dataSourceConfig);
                        sqlExecutionModule.execute(userSQLQuery);
                        break;
                    case TENANT_SPECIFIC_APPENDED:
                        sqlExecutionModule = new TenantSpecificAppendedSQLExecutionModule(dataSourceConfig);
                        sqlExecutionModule.execute(userSQLQuery);
                        break;
                    case TENANT_APPENDED:
                        sqlExecutionModule = new TenantAppendedSQLExecutionModule(dataSourceConfig);
                        sqlExecutionModule.execute(userSQLQuery);
                        break;
                    case AM_APPLICATION_REGISTRATION_UPDATE:
                        extendedExecutionModule = new AMApplicationRegistrationSQLExecutionModule(
                                dataSourceConfig);
                        extendedExecutionModule.execute(getSelectAndUpdateQueries(userSQLQuery,
                                sqlQueries.get(userSQLQuery.getSqlQuery().getFollowedByQuery()), userIdentifier));
                        break;
                    case IDN_OAUTH_CONSUMER_APPS_UPDATE:
                        extendedExecutionModule = new IDNOauthConsumerAppsSQLExecutionModule(dataSourceConfig);
                        extendedExecutionModule.execute(getSelectAndUpdateQueries(userSQLQuery,
                                sqlQueries.get(userSQLQuery.getSqlQuery().getFollowedByQuery()), userIdentifier));
                        break;
                    case SP_APP_UPDATE:
                        extendedExecutionModule = new SPAppSQLExecutionModule(dataSourceConfig);
                        extendedExecutionModule.execute(getSelectAndUpdateQueries(userSQLQuery,
                                sqlQueries.get(userSQLQuery.getSqlQuery().getFollowedByQuery()), userIdentifier));
                        break;
                    case SELECT_PROCEEDED_UPDATE:
                        break;
                    default:
                        throw new SQLModuleException("Cannot find a suitable execution module.");
                }
            }
        } catch (ModuleException e) {
            // TODO: What should we do here ?
            e.printStackTrace();
        }
    }

    private Map<String, UserSQLQuery> getSelectAndUpdateQueries(UserSQLQuery selectQuery, SQLQuery updateQuery,
            UserIdentifier userIdentifier) {
        Map<String, UserSQLQuery> queries = new HashMap<>();
        UserSQLQuery updateSQLQuery = new UserSQLQuery();
        updateSQLQuery.setSqlQuery(updateQuery);
        updateSQLQuery.setUserIdentifier(userIdentifier);
        queries.put(SQLConstants.SELECT_QUERY, selectQuery);
        queries.put(SQLConstants.UPDATE_QUERY, updateSQLQuery);
        return queries;
    }
}
