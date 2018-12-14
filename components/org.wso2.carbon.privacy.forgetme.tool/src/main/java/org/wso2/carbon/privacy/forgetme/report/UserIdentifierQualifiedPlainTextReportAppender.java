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

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

import java.io.File;

/**
 * Report generator that appends user identifier details.
 */
public class UserIdentifierQualifiedPlainTextReportAppender extends PlainTextReportAppender {

    private UserIdentifier userIdentifier;

    public UserIdentifierQualifiedPlainTextReportAppender(File file, String name, UserIdentifier userIdentifier) {

        super(file, name);
        this.userIdentifier = userIdentifier;
    }

    @Override
    public void appendSection(String format, Object... data) {

        super.appendSection(getUserIdentifierQualifiedFormat(format), getExtendedData(data));
    }

    @Override
    public void appendSectionEnd(String format, Object... data) {

        super.appendSectionEnd(getUserIdentifierQualifiedFormat(format), getExtendedData(data));
    }

    private String getUserIdentifierQualifiedFormat(String format) {

        return format.concat("\nUsername: %s Tenant Domain: %s User Store Domain: %s Pseudonym: %s");
    }

    private Object[] getExtendedData(Object[] data) {

        int length = data.length;
        Object[] extendedData = new Object[length + 4];
        ArrayUtils.addAll(extendedData, data);
        extendedData[length] = userIdentifier.getUsername();
        extendedData[length + 1] = userIdentifier.getTenantDomain();
        extendedData[length + 2] = userIdentifier.getUserStoreDomain();
        extendedData[length + 3] = userIdentifier.getPseudonym();

        return extendedData;
    }
}
