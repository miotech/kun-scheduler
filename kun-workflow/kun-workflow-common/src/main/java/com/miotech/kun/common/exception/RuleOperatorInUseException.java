package com.miotech.kun.common.exception;

public class RuleOperatorInUseException extends RuntimeException {
    public RuleOperatorInUseException() {
    }

    public RuleOperatorInUseException(String message) {
        super(message);
    }

    public RuleOperatorInUseException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuleOperatorInUseException(Throwable cause) {
        super(cause);
    }

    public RuleOperatorInUseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
