package com.miotech.kun.openapi.model.request;

import lombok.Data;

@Data
public class TaskRunPriorityChangeRequest {

    private Long taskRunId;

    private Integer priority;
}
