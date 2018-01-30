package org.wso2.carbon.privacy.forgetme.api.runtime;

import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;

public interface ForgetMeInstruction {

    ForgetMeResultSet execute(Environment environment, UserIdentifier userIdentifier);

}
