package org.wso2.carbon.privacy.forgetme.api.runtime;

import java.io.File;

public interface InstructionReader {
    String getType();

    ForgetMeInstruction read(File contentDirectory, Environment environment);
}
