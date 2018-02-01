package org.wso2.carbon.identity.instructions;

import org.wso2.carbon.identity.DataSourceConfig;
import org.wso2.carbon.identity.Processor;
import org.wso2.carbon.identity.SQLExecutionProcessor;
import org.wso2.carbon.identity.UserSQLQuery;
import org.wso2.carbon.identity.exception.CompliancyToolException;
import org.wso2.carbon.identity.exception.SQLReaderException;
import org.wso2.carbon.identity.sql.SQLFileReader;
import org.wso2.carbon.identity.sql.SQLQuery;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResult;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RdbmsForgetMeInstruction implements ForgetMeInstruction {

    private Path sqlDir;
    private Path dataSourceConfigDir;
    private String datasourceName;

    public RdbmsForgetMeInstruction(Path sqlDir, String datasourceName) {
        this.sqlDir = sqlDir;
        this.datasourceName = datasourceName;
    }

    @Override
    public ForgetMeResult execute(UserIdentifier userIdentifier, ProcessorConfig processorConfig,
            Environment environment) {
        System.out.println("Executing RdbmsForgetMeInstruction");
        DataSourceConfig dataSourceConfig = ((DatasourceProcessorConfig) processorConfig).getDataSourceConfig(datasourceName);
        SQLFileReader sqlFileReader = new SQLFileReader(sqlDir);

        List<SQLQuery> sqlQueries = null;
        try {
            sqlQueries = sqlFileReader.readAllQueries();
        } catch (SQLReaderException e) {
            e.printStackTrace();
        }

        List<UserSQLQuery> userSQLQueryList = new ArrayList<>();
        for (SQLQuery sqlQuery : sqlQueries) {
            UserSQLQuery userSQLQuery = new UserSQLQuery();
            userSQLQuery.setSqlQuery(sqlQuery);
            userSQLQuery.setUserIdentifier(userIdentifier);
            userSQLQueryList.add(userSQLQuery);
        }
        try {
            Processor<UserSQLQuery> sqlExecutionProcessor = new SQLExecutionProcessor(dataSourceConfig);
            sqlExecutionProcessor.execute(userSQLQueryList);
        } catch (CompliancyToolException e) {
            e.printStackTrace();
        }

        return new ForgetMeResult();
    }
}
