package org.wso2.carbon.privacy.forgetme.logs.exception;

import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionExecutionException;

/**
 * Represents an generic exception occurred in the Log Processor.
 */
public class LogProcessorException extends InstructionExecutionException {

    public LogProcessorException(String message) {
        super(message);
    }

    public LogProcessorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}