package com.miotech.kun.openapi.model;

import lombok.Data;

@Data
public class TaskRunPriorityChangeRequest {

    private Long taskRunId;

    private Integer priority;
}
