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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.report.ReportAppender;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResult;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionExecutionException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;
import org.wso2.carbon.privacy.forgetme.userstore.exception.UserStoreInstructionExecutionException;
import org.wso2.carbon.privacy.forgetme.userstore.exception.UserStoreModuleException;
import org.wso2.carbon.privacy.forgetme.userstore.handler.UserStoreHandler;

/**
 * Forget-Me instruction which executes against a user store.
 * The applicable user store is found based on the user store domain and the tenant domain.
 */
public class UserStoreForgetMeInstruction implements ForgetMeInstruction {

    private static final Logger log = LoggerFactory.getLogger(UserStoreForgetMeInstruction.class);

    public UserStoreForgetMeInstruction() {

    }

    @Override
    public ForgetMeResult execute(UserIdentifier userIdentifier, ProcessorConfig processorConfig,
                                  Environment environment, ReportAppender reportAppender)
            throws InstructionExecutionException {

        try {

            String logMessage = String.format("Renaming user in tenant : '%d' user store domain : '%s'",
                    userIdentifier.getTenantId(), userIdentifier.getUserStoreDomain());

            if (log.isDebugEnabled()) {
                log.debug(logMessage);
            }

            reportAppender.appendSection(logMessage);

            UserStoreProcessorConfig config = (UserStoreProcessorConfig) processorConfig;

            // Get the applicable user store handler.
            UserStoreHandler userStoreHandler = config.getUserStoreHandlerFactory()
                    .getUserStoreHandler(userIdentifier.getTenantId(), userIdentifier.getUserStoreDomain());

            if (userStoreHandler == null) {
                throw new UserStoreInstructionExecutionException(String.format("An applicable handler can't be found " +
                                "for the tenant id : '%d' and user store domain : '%s'",
                        userIdentifier.getTenantId(), userIdentifier.getUserStoreDomain()));
            }

            // Rename the user.
            userStoreHandler.renameUser(userIdentifier.getUsername(), userIdentifier.getPseudonym());

            logMessage = String.format("Renamed user in tenant : '%d' user store domain : '%s'",
                    userIdentifier.getTenantId(), userIdentifier.getUserStoreDomain());
            reportAppender.appendSection(logMessage);

            log.info(logMessage);

            return new ForgetMeResult();

        } catch (UserStoreModuleException e) {
            throw new UserStoreInstructionExecutionException("An error occurred while renaming user.", e);
        }
    }

}
