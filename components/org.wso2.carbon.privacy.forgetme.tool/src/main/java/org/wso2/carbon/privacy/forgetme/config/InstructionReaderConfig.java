package org.wso2.carbon.privacy.forgetme.config;

import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;

import java.util.Properties;

/**
 * Holds the configurations for the instruction reader.
 *
 */
public class InstructionReaderConfig {
    private Properties properties;
    private InstructionReader instructionReader;

    public InstructionReaderConfig(InstructionReader instructionReader, Properties properties) {
        this.properties = properties;
        this.instructionReader = instructionReader;
    }

    public Properties getProperties() {
        return properties;
    }

    public InstructionReader getInstructionReader() {
        return instructionReader;
    }
}
