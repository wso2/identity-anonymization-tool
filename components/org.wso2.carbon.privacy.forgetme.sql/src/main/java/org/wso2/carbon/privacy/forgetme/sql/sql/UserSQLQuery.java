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

package org.wso2.carbon.privacy.forgetme.sql.sql;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

/**
 * Represents a SQL query that is bounded to an user.
 */
public class UserSQLQuery {

    private UserIdentifier userIdentifier;
    private SQLQuery sqlQuery;

    public UserIdentifier getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(UserIdentifier userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public SQLQuery getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(SQLQuery sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public int getNumberOfPlacesToReplace(String substring) {
        return StringUtils.countMatches(sqlQuery.getSqlQuery(), substring);
    }

}
