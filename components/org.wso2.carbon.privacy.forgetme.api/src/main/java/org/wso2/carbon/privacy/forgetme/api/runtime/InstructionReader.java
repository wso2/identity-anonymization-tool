package org.wso2.carbon.privacy.forgetme.api.runtime;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/**
 * Definition of the instruction reader.
 */
public interface InstructionReader {

    String getType();

    List<ForgetMeInstruction> read(Path contentDirectory, Properties properties, Environment environment)
            throws ModuleException;
}
