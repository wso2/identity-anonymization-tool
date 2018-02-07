package org.wso2.carbon.identity;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.wso2.carbon.identity.config.DataSourceConfig;
import org.wso2.carbon.identity.exception.SQLModuleException;
import org.wso2.carbon.identity.instructions.DatasourceProcessorConfig;
import org.wso2.carbon.identity.instructions.DatasourceProcessorConfigReader;
import org.wso2.carbon.identity.module.DomainAppendedSQLExecutionModule;
import org.wso2.carbon.identity.module.DomainSeparatedSQLExecutionModule;
import org.wso2.carbon.identity.module.Module;
import org.wso2.carbon.identity.sql.SQLFileReader;
import org.wso2.carbon.identity.sql.SQLQuery;
import org.wso2.carbon.identity.sql.UserSQLQuery;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

import java.nio.file.Paths;
import java.util.List;
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

        SQLFileReader sqlFileReader = new SQLFileReader(Paths.get("components", "org.wso2.carbon.identity.forgetme.sql",
                "src", "main", "resources", "sql"));
        List<SQLQuery> sqlQueries;

        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.setUsername("admin");
        userIdentifier.setUserStoreDomain("PRIMARY");
        userIdentifier.setTenantDomain("carbon.super");
        userIdentifier.setPseudonym(UUID.randomUUID().toString());

        DatasourceProcessorConfigReader reader = new DatasourceProcessorConfigReader();
        DatasourceProcessorConfig processorConfig = reader.readProcessorConfig(
                Paths.get("components", "org.wso2.carbon.identity.forgetme.sql", "src", "main", "resources", "conf",
                        "datasources"));
        DataSourceConfig dataSourceConfig = processorConfig.getDataSourceConfig("WSO2_CARBON_DB");

        try {
            sqlQueries = sqlFileReader.readAllQueries();

            for (SQLQuery sqlQuery : sqlQueries) {

                UserSQLQuery userSQLQuery = new UserSQLQuery();
                userSQLQuery.setSqlQuery(sqlQuery);
                userSQLQuery.setUserIdentifier(userIdentifier);

                String datasourceName = sqlQuery.getBaseDirectory();

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
    }
}
