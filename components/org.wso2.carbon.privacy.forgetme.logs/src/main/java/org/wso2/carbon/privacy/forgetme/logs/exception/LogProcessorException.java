package org.wso2.carbon.privacy.forgetme.logs.exception;

/**
 * Represents an generic exception occurred in the Log Processor.
 */
public class LogProcessorException extends Exception {

    public LogProcessorException(String message) {
        super(message);
    }

    public LogProcessorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}