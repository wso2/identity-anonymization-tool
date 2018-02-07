package org.wso2.carbon.privacy.forgetme.api.runtime;

import java.nio.file.Path;

/**
 * Reader for processor config.
 * Implements SPI.
 */
public interface ProcessorConfigReader<T extends ProcessorConfig> {

    /**
     * Returns the unique name for the processor config.
     * This is used to lookup the relevant config from the system configuration file.
     *
     * @return
     */
    String getName();

    /**
     * Reads the processor config from the given directory.
     * The contents of the directory is implementation specific.
     *
     * @param path
     * @return
     */
    T readProcessorConfig(Path path) throws ModuleException;

}
