package com.miotech.kun.openapi.model.request;

import lombok.Data;

@Data
public class TaskCommitRequest {
    private final Long taskId;

    private final String message;
}
