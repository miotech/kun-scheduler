package com.miotech.kun.commons.web.exception;

public class ExceptionResponse {

    private final int code;

    private final String message;

    public ExceptionResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

}
