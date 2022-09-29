package com.miotech.kun.openapi.model.request;

import lombok.Data;

@Data
public class TaskViewCreateRequest {
    private String taskViewName;

    public TaskViewCreateRequest() {}
}
