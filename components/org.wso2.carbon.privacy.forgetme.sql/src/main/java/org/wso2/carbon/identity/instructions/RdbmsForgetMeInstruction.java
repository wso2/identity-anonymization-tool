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

package org.wso2.carbon.identity.instructions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.config.DataSourceConfig;
import org.wso2.carbon.identity.exception.SQLModuleException;
import org.wso2.carbon.identity.module.DomainAppendedSQLExecutionModule;
import org.wso2.carbon.identity.module.DomainSeparatedSQLExecutionModule;
import org.wso2.carbon.identity.module.Module;
import org.wso2.carbon.identity.sql.SQLFileReader;
import org.wso2.carbon.identity.sql.SQLQuery;
import org.wso2.carbon.identity.sql.UserSQLQuery;
import org.wso2.carbon.privacy.forgetme.api.report.ReportAppender;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResult;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionExecutionException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

import java.nio.file.Path;
import java.util.List;

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
    }

    @Override
    public ForgetMeResult execute(UserIdentifier userIdentifier, ProcessorConfig processorConfig,
            Environment environment, ReportAppender reportAppender) throws InstructionExecutionException {

        SQLFileReader sqlFileReader = new SQLFileReader(sqlDir);
        reportAppender.appendSection("Processing SQL in directory %s", sqlDir);
        try {
            List<SQLQuery> sqlQueries = sqlFileReader.readAllQueries();

            for (SQLQuery sqlQuery : sqlQueries) {

                UserSQLQuery userSQLQuery = new UserSQLQuery();
                userSQLQuery.setSqlQuery(sqlQuery);
                userSQLQuery.setUserIdentifier(userIdentifier);

                String datasourceName = sqlQuery.getBaseDirectory();

                DataSourceConfig dataSourceConfig = ((DatasourceProcessorConfig) processorConfig)
                        .getDataSourceConfig(datasourceName);

                Module<UserSQLQuery> sqlExecutionModule;
                switch (sqlQuery.getSqlQueryType()) {
                case DOMAIN_APPENDED:
                    sqlExecutionModule = new DomainAppendedSQLExecutionModule(dataSourceConfig);
                    break;
                case DOMAIN_SEPARATED:
                    sqlExecutionModule = new DomainSeparatedSQLExecutionModule(dataSourceConfig);
                    break;
                default:
                    throw new SQLModuleException("Cannot find a suitable execution module.");
                }
                sqlExecutionModule.execute(userSQLQuery);
                reportAppender.appendSection("Executed query %s", userSQLQuery);
            }
        } catch (ModuleException e) {
            throw new InstructionExecutionException("Error occured while executing sql from : " + sqlDir, e);
        }

        reportAppender.appendSection("Completed all SQLs in directory %s", sqlDir);
        return new ForgetMeResult();
    }
}
