package com.miotech.kun.openapi.model.request;

import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskPayload;
import lombok.Data;

@Data
public class TaskCreateRequest {
    private final String taskName;

    private final String taskTemplateName;

    private final Long taskViewId;

    private final TaskPayload taskPayload;
}
