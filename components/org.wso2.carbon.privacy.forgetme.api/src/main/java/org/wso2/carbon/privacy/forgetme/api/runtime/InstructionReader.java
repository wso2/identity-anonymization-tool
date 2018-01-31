package org.wso2.carbon.privacy.forgetme.api.runtime;

import java.nio.file.Path;
import java.util.List;

/**
 * Definition of the instruction reader.
 */
public interface InstructionReader {

    String getType();

    List<ForgetMeInstruction> read(Path contentDirectory, Environment environment);
}
