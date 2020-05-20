package com.miotech.kun.workflow.web.http;

import java.time.LocalDateTime;

public class InternalErrorMessage {

    private static String INTERNAL_ERROR = "Internal Server Error";
    private static int INTERNAL_ERROR_STATUS = 500;

    private final String message;

    private final String error;

    private final int status;

    private final String path;

    private final LocalDateTime timestamp;

    public InternalErrorMessage(String message, String path, LocalDateTime timestamp) {
        this(message, path, timestamp, INTERNAL_ERROR, INTERNAL_ERROR_STATUS);
    }

    public InternalErrorMessage(String message, String path, LocalDateTime timestamp, String error, int status) {
        this.message = message;
        this.error = error;
        this.status = status;
        this.path = path;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }
}
