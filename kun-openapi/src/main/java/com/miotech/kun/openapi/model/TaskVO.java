package com.miotech.kun.openapi.model;

import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import lombok.Data;

@Data
public class TaskVO {
    private final Long taskId;

    private final String taskName;

    public static TaskVO from(TaskDefinition taskDefinition) {
        return new TaskVO(taskDefinition.getDefinitionId(), taskDefinition.getName());
    }
}
