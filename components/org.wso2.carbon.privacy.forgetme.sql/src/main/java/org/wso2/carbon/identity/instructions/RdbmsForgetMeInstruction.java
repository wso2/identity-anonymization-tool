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
import org.wso2.carbon.identity.exception.ModuleException;
import org.wso2.carbon.identity.exception.SQLModuleException;
import org.wso2.carbon.identity.module.DomainAppendedSQLExecutionModule;
import org.wso2.carbon.identity.module.DomainSeparatedSQLExecutionModule;
import org.wso2.carbon.identity.module.Module;
import org.wso2.carbon.identity.sql.SQLFileReader;
import org.wso2.carbon.identity.sql.SQLQuery;
import org.wso2.carbon.identity.sql.UserSQLQuery;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResult;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

import java.nio.file.Path;
import java.util.List;

public class RdbmsForgetMeInstruction implements ForgetMeInstruction {

    private static Logger log = LoggerFactory.getLogger(RdbmsForgetMeInstruction.class);

    private Path sqlDir;

    public RdbmsForgetMeInstruction(Path sqlDir) {

        // TODO: Do we need the datasource name here ?
        this.sqlDir = sqlDir;
    }

    @Override
    public ForgetMeResult execute(UserIdentifier userIdentifier, ProcessorConfig processorConfig,
            Environment environment) {

        log.info("Executing RdbmsForgetMeInstruction");

        SQLFileReader sqlFileReader = new SQLFileReader(sqlDir);

        List<SQLQuery> sqlQueries;
        try {
            sqlQueries = sqlFileReader.readAllQueries();

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
            }
        } catch (ModuleException e) {
            // TODO: What should we do here ?
            e.printStackTrace();
        }

        return new ForgetMeResult();
    }
}
