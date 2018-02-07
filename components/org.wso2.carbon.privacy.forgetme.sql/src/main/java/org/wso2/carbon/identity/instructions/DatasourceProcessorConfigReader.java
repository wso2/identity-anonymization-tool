package org.wso2.carbon.identity.instructions;

import org.wso2.carbon.datasource.core.DataSourceManager;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
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
    public DatasourceProcessorConfig readProcessorConfig(Path path) throws SQLModuleException {

        DataSourceManager dataSourceManager = DataSourceManager.getInstance();

        try {
            dataSourceManager.initDataSources(path.toAbsolutePath().toString());
        } catch (DataSourceException e) {
            throw new SQLModuleException("Error occurred while initializing the data source.", e);
        }

        DatasourceProcessorConfig datasourceProcessorConfig = new DatasourceProcessorConfig(dataSourceManager);

        return datasourceProcessorConfig;
    }
}
