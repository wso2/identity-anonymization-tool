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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.privacy.forgetme.config;

import org.wso2.carbon.privacy.forgetme.api.report.CloseableReportAppenderBuilder;

import java.nio.file.Path;
import java.util.Map;

/**
 * Holds configuration needed to build the report appender.
 */
public class ReportAppenderConfig {

    private Path reportDirectoryPath;
    private Map<String, String> properties;
    private CloseableReportAppenderBuilder reportReader;

    public ReportAppenderConfig(Path reportDirectoryPath, Map<String, String> properties, CloseableReportAppenderBuilder reportReader) {

        this.reportDirectoryPath = reportDirectoryPath;
        this.properties = properties;
        this.reportReader = reportReader;
    }

    /**
     * Returns the directory path configured for report generation.
     *
     * @return directory path
     */
    public Path getReportDirectoryPath() {

        return reportDirectoryPath;
    }

    /**
     * Returns properties configured
     *
     * @return report properties
     */
    public Map<String, String> getProperties() {

        return properties;
    }

    /**
     * Returns the CloseableReportAppenderBuilder instance.
     *
     * @return CloseableReportAppenderBuilder instance
     */
    public CloseableReportAppenderBuilder getReportAppenderBuilder() {

        return reportReader;
    }
}
