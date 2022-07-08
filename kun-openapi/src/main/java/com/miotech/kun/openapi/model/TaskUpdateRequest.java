package com.miotech.kun.openapi.model;

import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskPayload;
import lombok.Data;

@Data
public class TaskUpdateRequest {
    private Long taskId;

    private String taskName;

    private TaskPayload taskPayload;

    private String owner;
}
