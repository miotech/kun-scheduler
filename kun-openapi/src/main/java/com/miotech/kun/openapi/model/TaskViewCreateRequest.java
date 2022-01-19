package com.miotech.kun.openapi.model;

import lombok.Data;

@Data
public class TaskViewCreateRequest {
    private String taskViewName;

    public TaskViewCreateRequest() {}
}
