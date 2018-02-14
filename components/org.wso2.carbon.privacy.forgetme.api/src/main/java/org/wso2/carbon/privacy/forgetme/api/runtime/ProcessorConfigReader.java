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

package org.wso2.carbon.privacy.forgetme.api.runtime;

import java.util.Map;

import java.nio.file.Path;

/**
 * Reader for processor config.
 * Implements SPI.
 */
public interface ProcessorConfigReader<T extends ProcessorConfig> {

    /**
     * Returns the unique name for the processor config.
     * This is used to lookup the relevant config from the system configuration file.
     *
     * @return
     */
    String getName();

    /**
     * Reads the processor config from the given directory.
     * The contents of the directory is implementation specific.
     *
     * @param path
     * @return
     */
    T readProcessorConfig(Path path, Map<String, String> properties) throws ModuleException;

}
