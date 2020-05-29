package com.miotech.kun.workflow.common.exception;

public class NameConflictException extends RuntimeException {

    public NameConflictException() {
    }

    public NameConflictException(String message) {
        super(message);
    }

    public NameConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public NameConflictException(Throwable cause) {
        super(cause);
    }

    public NameConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
