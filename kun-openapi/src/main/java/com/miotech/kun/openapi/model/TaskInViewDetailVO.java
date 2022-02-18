package com.miotech.kun.openapi.model;

import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import lombok.Data;

@Data
public class TaskInViewDetailVO {
    private final Long taskId;

    private final String taskName;

    public static TaskInViewDetailVO from(TaskDefinition taskDefinition) {
        return new TaskInViewDetailVO(taskDefinition.getDefinitionId(), taskDefinition.getName());
    }
}
