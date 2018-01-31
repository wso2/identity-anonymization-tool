package org.wso2.carbon.privacy.forgetme.runtime;

/**
 * ForgetMe execution exception.
 */
public class ForgetMeExecutionException extends Exception {

    private String errorCode;
    private String subCode;

    public ForgetMeExecutionException(String message, Throwable cause, String errorCode, String subCode) {
        this(message, cause);
        this.errorCode = errorCode;
        this.subCode = subCode;
    }

    public ForgetMeExecutionException() {
    }

    public ForgetMeExecutionException(String message) {
        super(message);
    }

    public ForgetMeExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForgetMeExecutionException(Throwable cause) {
        super(cause);
    }

    public ForgetMeExecutionException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
