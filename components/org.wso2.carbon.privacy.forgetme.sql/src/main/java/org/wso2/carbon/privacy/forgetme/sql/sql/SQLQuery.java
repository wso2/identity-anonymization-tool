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

/**
 * Represents a SQL query.
 */
public class SQLQuery {

    private String sqlQuery;
    private String baseDirectory;
    private SQLQueryType sqlQueryType;
    private String followedByQuery;

    public SQLQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    @Override
    public String toString() {
        return sqlQuery;
    }

    public SQLQueryType getSqlQueryType() {
        return sqlQueryType;
    }

    public void setSqlQueryType(SQLQueryType sqlQueryType) {
        this.sqlQueryType = sqlQueryType;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public String getFollowedByQuery() {
        return followedByQuery;
    }

    public void setFollowedByQuery(String followedByQuery) {
        this.followedByQuery = followedByQuery;
    }

}
