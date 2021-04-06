package com.miotech.kun.dataplatform.exception;

public class DataPlatformBaseException extends RuntimeException {
    private final int statusCode;

    public DataPlatformBaseException(int statusCode) {
        super();
        this.statusCode = statusCode;
    }

    public DataPlatformBaseException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
