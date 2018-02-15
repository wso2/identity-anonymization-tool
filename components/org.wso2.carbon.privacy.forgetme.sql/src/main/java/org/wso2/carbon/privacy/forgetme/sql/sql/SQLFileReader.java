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

import org.wso2.carbon.privacy.forgetme.sql.exception.SQLReaderException;
import org.wso2.carbon.privacy.forgetme.sql.util.LambdaExceptionUtils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Utility class to read sql files in a given folder.
 */
public class SQLFileReader {

    private Path path;

    public SQLFileReader(String folderPath) {
        path = Paths.get(folderPath);
    }

    public SQLFileReader(Path folderPath) {
        path = folderPath;
    }

    /**
     * Read all the SQL files in the given folder.
     *
     * @return List of {{@link SQLQuery}.
     * @throws SQLReaderException Error while reading the files.
     */
    public List<SQLQuery> readAllQueries() throws SQLReaderException {

        List<SQLQuery> sqlQueries = new ArrayList<>();
        try (Stream<Path> pathStream = Files.walk(path)) {
            pathStream.forEach(LambdaExceptionUtils.rethrowConsumer(paths -> {
                PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.sql");
                if (matcher.matches(paths.getFileName())) {
                    SQLQuery sqlQuery = new SQLQuery(new String(Files.readAllBytes(paths)));
                    sqlQuery.setBaseDirectory(paths.getParent().toFile().getName());
                    sqlQuery.setSqlQueryType(
                            getQueryTypeForSQLQuery(paths.getFileName().toString(), paths.getParent()));
                    sqlQueries.add(sqlQuery);
                }
            }));
        } catch (IOException e) {
            throw new SQLReaderException("Error occurred while reading the SQL files.", e);
        }
        return sqlQueries;
    }

    /**
     * Get the type of this query. To decide the query type there will be a control file (.properties) with the same
     * name of the SQL query file. Inside that file we define the query type. If there isn't any control file, we
     * assume that this query is default type.
     *
     * @param queryFileName Name of the SQL query file.
     * @param basePath      Base path where this query file exist.
     * @return Type of the query as an Enum of {{@link SQLQueryType}}
     * @throws SQLReaderException Error while reading the file.
     */
    public SQLQueryType getQueryTypeForSQLQuery(String queryFileName, Path basePath) throws SQLReaderException {

        if (!Files.isDirectory(basePath)) {
            throw new SQLReaderException("Invalid base path. Base path should be a directory.");
        }

        if (Files.exists(Paths.get(basePath.toString(), queryFileName + ".properties"))) {
            try {
                Properties properties = new Properties();
                properties.load(Files.newInputStream(Paths.get(basePath.toString(), queryFileName + ".properties")));
                String type = properties.getProperty("type");
                return SQLQueryType.valueOf(type);
            } catch (IOException e) {
                throw new SQLReaderException("Error occurred while reading the SQL property files.", e);
            }
        }

        // If no properties file, we assume it is as a domain separated one.
        return SQLQueryType.DOMAIN_SEPARATED;
    }
}
