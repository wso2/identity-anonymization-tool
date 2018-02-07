package org.wso2.carbon.privacy.forgetme.api.runtime;

/**
 * Thrown when executing an instruction upon any error.
 *
 */
public class InstructionExecutionException extends ModuleException {

    public InstructionExecutionException(String message) {
        super(message);
    }

    public InstructionExecutionException(Throwable throwable) {
        super(throwable);
    }

    public InstructionExecutionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
