package com.miotech.kun.common.exception;

public class DuplicatedNameException extends RuntimeException {

    public DuplicatedNameException() {
    }

    public DuplicatedNameException(String message) {
        super(message);
    }

    public DuplicatedNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedNameException(Throwable cause) {
        super(cause);
    }

    public DuplicatedNameException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
