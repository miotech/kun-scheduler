package com.miotech.kun.workflow.client;

public class WorkflowClientException extends RuntimeException {

    public WorkflowClientException(String message) {
        super(message);
    }

    public WorkflowClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
