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

package org.wso2.carbon.identity.config;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.exception.SQLModuleException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents a data source configuration.
 */
public class DataSourceConfig {

    private Path dataSourceConfigPath;
    private String dataSourceName;

    public DataSourceConfig(Path dataSourceConfigPath, String dataSourceName) throws SQLModuleException {

        if (dataSourceConfigPath == null || StringUtils.isEmpty(dataSourceName)) {
            throw new SQLModuleException("All values are mandatory. Cannot be null or empty.");
        }

        this.dataSourceConfigPath = dataSourceConfigPath;
        this.dataSourceName = dataSourceName;
    }

    public DataSourceConfig(String dataSourceConfigPath, String dataSourceName) throws SQLModuleException {

        if (StringUtils.isEmpty(dataSourceConfigPath) || StringUtils.isEmpty(dataSourceName)) {
            throw new SQLModuleException("All values are mandatory, Cannot be null or empty.");
        }

        this.dataSourceConfigPath = Paths.get(dataSourceConfigPath);
        this.dataSourceName = dataSourceName;
    }

    public Path getDataSourceConfigPath() {
        return dataSourceConfigPath;
    }

    public void setDataSourceConfigPath(Path dataSourceConfigPath) {
        this.dataSourceConfigPath = dataSourceConfigPath;
    }

    public void setDataSourceConfigPath(String dataSourceConfigPath) {
        this.dataSourceConfigPath = Paths.get(dataSourceConfigPath);
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }
}
