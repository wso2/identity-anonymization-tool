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

package org.wso2.carbon.privacy.forgetme.api.report;

import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

import java.nio.file.Path;
import java.util.Map;

/**
 * Closeable report appender builder which builds the appender for the given parameters.
 */
public interface CloseableReportAppenderBuilder {

    /**
     * Returns the report appender type.
     *
     * @return report appender type
     */
    String getType();

    /**
     * Returns a closeable report appender instantiated for given properties.
     *
     * @param processor processor name
     * @param reportDirectoryPath report path
     * @param properties any property the builder may use to build the appender
     * @param userIdentifier user identifier
     * @return an instance of a CloseableReportAppender
     * @throws ModuleException
     */
    CloseableReportAppender build(String processor, Path reportDirectoryPath, Map<String, String> properties, UserIdentifier
            userIdentifier) throws ModuleException;
}
