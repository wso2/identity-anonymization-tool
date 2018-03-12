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

package org.wso2.carbon.privacy.forgetme.sql.instructions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.report.ReportAppender;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResult;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionExecutionException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;
import org.wso2.carbon.privacy.forgetme.sql.config.DataSourceConfig;
import org.wso2.carbon.privacy.forgetme.sql.exception.SQLModuleException;
import org.wso2.carbon.privacy.forgetme.sql.module.AMApplicationRegistrationSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.module.DomainAppendedSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.module.DomainSeparatedSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.module.IDNOauthConsumerAppsSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.module.Module;
import org.wso2.carbon.privacy.forgetme.sql.module.SPAppSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.module.TenantAppendedSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.module.TenantSpecificAppendedSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.sql.SQLFileReader;
import org.wso2.carbon.privacy.forgetme.sql.sql.SQLQuery;
import org.wso2.carbon.privacy.forgetme.sql.sql.UserSQLQuery;
import org.wso2.carbon.privacy.forgetme.sql.util.SQLConstants;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.privacy.forgetme.sql.sql.SQLQueryType.SELECT_PROCEEDED_UPDATE;

/**
 * Forget-Me instruction which processes a table in RDBMS.
 * The data-source is passed as a processor config or environment.
 * The SQL(s) to be executed is passed in the directory.
 */
public class RdbmsForgetMeInstruction implements ForgetMeInstruction {

    private static final Logger log = LoggerFactory.getLogger(RdbmsForgetMeInstruction.class);

    private Path sqlDir;

    public RdbmsForgetMeInstruction(Path sqlDir) {

        this.sqlDir = sqlDir;
        if (log.isDebugEnabled()) {
            log.debug("SQL directory path is set to: {}. ", sqlDir.toString());
        }
    }

    @Override
    public ForgetMeResult execute(UserIdentifier userIdentifier, ProcessorConfig processorConfig,
            Environment environment, ReportAppender reportAppender) throws InstructionExecutionException {

        SQLFileReader sqlFileReader = new SQLFileReader(sqlDir);
        reportAppender.appendSection("Processing SQL in directory %s", sqlDir);
        log.info("Processing SQL in directory {}", sqlFileReader);
        try {
            Map<String, SQLQuery> sqlQueries = sqlFileReader.readAllQueries();

            for (Map.Entry<String, SQLQuery> entry : sqlQueries.entrySet()) {
                SQLQuery sqlQuery = entry.getValue();
                UserSQLQuery userSQLQuery = new UserSQLQuery();
                userSQLQuery.setSqlQuery(sqlQuery);
                userSQLQuery.setUserIdentifier(userIdentifier);

                String datasourceName = sqlQuery.getBaseDirectory();

                DataSourceConfig dataSourceConfig = ((DatasourceProcessorConfig) processorConfig)
                        .getDataSourceConfig(datasourceName);

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

                if (log.isDebugEnabled()) {
                    log.debug("{} module selected for {} SQL query.", sqlQuery.getSqlQueryType(), sqlQuery);
                }

                if(!SELECT_PROCEEDED_UPDATE.equals(sqlQuery.getSqlQueryType())) {
                    reportAppender.append("Executed query %s", userSQLQuery);
                }
            }
        } catch (ModuleException e) {
            throw new InstructionExecutionException("Error occurred while executing sql from : " + sqlDir, e);
        }

        reportAppender.appendSection("Completed all SQLs in directory %s", sqlDir);
        log.info("Completed all SQLs in directory {}.", sqlDir);
        return new ForgetMeResult();
    }

    /**
     * Creates a map of a select query and the corresponding update query
     *
     * @param selectQuery Select query
     * @param updateQuery Update query
     * @param userIdentifier User identifier
     * @return A map containing both select and update queries
     */
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
