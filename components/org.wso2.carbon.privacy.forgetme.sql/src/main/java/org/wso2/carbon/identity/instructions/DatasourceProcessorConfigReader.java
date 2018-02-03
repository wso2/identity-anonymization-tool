package org.wso2.carbon.identity.instructions;

import org.wso2.carbon.identity.config.DataSourceConfig;
import org.wso2.carbon.identity.exception.SQLModuleException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfigReader;

import java.nio.file.Path;

/**
 * Module config reader for Datasource elements.
 */
public class DatasourceProcessorConfigReader implements ProcessorConfigReader<DatasourceProcessorConfig> {

    @Override
    public String getName() {
        return "datasource";
    }

    @Override
    public DatasourceProcessorConfig readProcessorConfig(Path path) {

        DatasourceProcessorConfig datasourceProcessorConfig = new DatasourceProcessorConfig();
        try {
            DataSourceConfig dataSourceConfig = new DataSourceConfig(path, "WSO2_CARBON_DB");
            datasourceProcessorConfig.addConfig(dataSourceConfig.getDataSourceName(), dataSourceConfig);
        } catch (SQLModuleException e) {
            e.printStackTrace();
        }
        return datasourceProcessorConfig;
    }
}
