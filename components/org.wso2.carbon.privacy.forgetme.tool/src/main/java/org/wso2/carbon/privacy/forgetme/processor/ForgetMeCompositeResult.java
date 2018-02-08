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

package org.wso2.carbon.privacy.forgetme.processor;

import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite result which carries other result within nested result tree.
 *
 */
public class ForgetMeCompositeResult extends ForgetMeResult {

    private List<ForgetMeResult> parts = new ArrayList<>();

    /**
     * Adds an entry to the composite result.
     *
     * @param entry the entry to be added.
     */
    public void addEntry(ForgetMeResult entry) {
        parts.add(entry);
    }
}
