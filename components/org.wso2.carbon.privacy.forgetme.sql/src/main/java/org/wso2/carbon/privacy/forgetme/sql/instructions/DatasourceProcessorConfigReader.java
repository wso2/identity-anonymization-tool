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

import org.wso2.carbon.datasource.core.DataSourceManager;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfigReader;
import org.wso2.carbon.privacy.forgetme.sql.exception.SQLModuleException;

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
