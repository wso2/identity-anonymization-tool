package org.wso2.carbon.identity.instructions;

import org.wso2.carbon.datasource.core.DataSourceManager;
import org.wso2.carbon.identity.config.DataSourceConfig;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;

/**
 * Module config for datasources.
 *
 */
public class DatasourceProcessorConfig implements ProcessorConfig {

    private DataSourceManager dataSourceManager;

    public DatasourceProcessorConfig(DataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
    }

    public DataSourceConfig getDataSourceConfig(String name) {
        return new DataSourceConfig(name, dataSourceManager);
    }
}
