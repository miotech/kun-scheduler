package com.miotech.kun.workflow.common.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExceptionResponse {

    private final int code;

    private final String message;

    @JsonCreator
    public ExceptionResponse(
            @JsonProperty("code") int code,
            @JsonProperty("message") String message
    ) {
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
