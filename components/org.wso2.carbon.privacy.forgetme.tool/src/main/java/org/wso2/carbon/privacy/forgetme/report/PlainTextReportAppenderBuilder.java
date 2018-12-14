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

package org.wso2.carbon.privacy.forgetme.report;

import org.wso2.carbon.privacy.forgetme.api.report.CloseableReportAppender;
import org.wso2.carbon.privacy.forgetme.api.report.CloseableReportAppenderBuilder;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Report appender builder for plain text report appenders.
 */
public class PlainTextReportAppenderBuilder implements CloseableReportAppenderBuilder {

    private static final String LOG_USER_IDENTIFIER_PROPERTY = "log-user-identifier";

    @Override
    public String getType() {

        return "plain-text";
    }

    @Override
    public CloseableReportAppender build(String processor, Path reportDirectoryPath, Map<String, String> properties,
                                         UserIdentifier userIdentifier)
            throws ModuleException {

        Path reportFilePath = Paths.get(reportDirectoryPath.toString(), getReportFileName(processor));
        if (properties.containsKey(LOG_USER_IDENTIFIER_PROPERTY) && Boolean.parseBoolean(properties.get(LOG_USER_IDENTIFIER_PROPERTY))) {
            return new UserIdentifierQualifiedPlainTextReportAppender(reportFilePath.toFile(), processor,
                    userIdentifier);
        }

        return new PlainTextReportAppender(reportFilePath.toFile(), processor);
    }

    private String getReportFileName(String processor) {

        return "Report-" + processor + "-" + System.currentTimeMillis() + ".txt";
    }
}
