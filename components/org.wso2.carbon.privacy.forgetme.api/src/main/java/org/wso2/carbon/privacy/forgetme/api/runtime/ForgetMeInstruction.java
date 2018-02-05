package org.wso2.carbon.privacy.forgetme.api.runtime;

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
    ForgetMeResult execute(UserIdentifier userIdentifier, ProcessorConfig processorConfig, Environment environment)
            throws InstructionExecutionException;

}
