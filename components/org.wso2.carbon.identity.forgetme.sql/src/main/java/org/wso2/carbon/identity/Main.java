package org.wso2.carbon.identity;

import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.identity.sql.SQLFileReader;
import org.wso2.carbon.identity.sql.SQLQuery;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Hello world!
 *
 */
public class Main {

    public static void main(String[] args) throws DataSourceException, IOException {

        SQLFileReader sqlFileReader = new SQLFileReader(Paths.get("src", "main", "resources", "sql"));
        List<SQLQuery> sqlQueries = sqlFileReader.readAllQueries();

        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUsername("admin");
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

        Processor<UserSQLQuery> sqlExecutionProcessor = new SQLExecutionProcessor();
        sqlExecutionProcessor.execute(userSQLQueryList);
    }
}
