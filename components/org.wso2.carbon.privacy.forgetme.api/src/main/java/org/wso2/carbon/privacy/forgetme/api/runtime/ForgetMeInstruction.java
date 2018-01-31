package org.wso2.carbon.privacy.forgetme.api.runtime;

import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

public interface ForgetMeInstruction {

    ForgetMeResult execute(UserIdentifier userIdentifier, ProcessorConfig processorConfig, Environment environment);

}
