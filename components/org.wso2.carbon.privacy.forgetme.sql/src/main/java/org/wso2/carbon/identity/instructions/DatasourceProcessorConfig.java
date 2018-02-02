package org.wso2.carbon.identity.instructions;

import org.wso2.carbon.identity.config.DataSourceConfig;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Module config for datasources.
 *
 */
public class DatasourceProcessorConfig implements ProcessorConfig {

    private Map<String, DataSourceConfig> dataSourceConfigMap = new HashMap<>();

    public void addConfig(String name, DataSourceConfig dataSourceConfig) {
        dataSourceConfigMap.put(name, dataSourceConfig);
    }

    public DataSourceConfig getDataSourceConfig(String name) {
        return dataSourceConfigMap.get(name);
    }
}
