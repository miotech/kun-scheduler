package com.miotech.kun.openapi.model;

import lombok.Data;

@Data
public class TaskCommitRequest {
    private final Long taskId;

    private final String message;
}
