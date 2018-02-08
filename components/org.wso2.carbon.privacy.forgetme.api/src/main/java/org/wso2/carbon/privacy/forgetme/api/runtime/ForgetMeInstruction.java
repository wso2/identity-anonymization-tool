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

import org.wso2.carbon.privacy.forgetme.api.report.ReportAppender;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

/**
 * General instruction to be executed.
 * Each table, log file, etc. has its own instruction.
 */
public interface ForgetMeInstruction {

    /**
     * Executes the the given instruction on given user Identifier.
     *
     * @param userIdentifier  The user Identifier to delete.
     * @param processorConfig  Uses this common processor configuration.
     * @param environment
     * @return
     * @throws InstructionExecutionException
     */
    ForgetMeResult execute(UserIdentifier userIdentifier, ProcessorConfig processorConfig, Environment environment,
            ReportAppender reportAppender) throws InstructionExecutionException;

}
