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

package org.wso2.carbon.privacy.forgetme.userstore.instructions;

import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * The instruction reader (builder) for user store operations.
 */
public class UserStoreForgetMeInstructionReader implements InstructionReader {

    @Override
    public String getType() {

        return "user-store";
    }

    @Override
    public List<ForgetMeInstruction> read(Path path, Properties properties, Environment environment) {

        List<ForgetMeInstruction> result = new ArrayList<>();

        // There is only one instructions and it renames the user in the relevant user store.
        UserStoreForgetMeInstruction forgetMeInstruction = new UserStoreForgetMeInstruction();
        result.add(forgetMeInstruction);

        return result;
    }
}
