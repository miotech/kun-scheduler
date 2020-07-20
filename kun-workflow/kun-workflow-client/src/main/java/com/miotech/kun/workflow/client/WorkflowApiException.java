package com.miotech.kun.workflow.client;

public class WorkflowApiException extends RuntimeException {

    public WorkflowApiException(String message) {
        super(message);
    }

    public WorkflowApiException(String message, Throwable cause) {
        super(message, cause);
    }

}
