package com.miotech.kun.dataplatform.common.taskdefinition.vo;

import com.miotech.kun.dataplatform.model.taskdefinition.TaskPayload;
import lombok.Data;

@Data
public class UpdateTaskDefinitionRequest {

    private final Long definitionId;

    private final String name;

    private final TaskPayload taskPayload;

    private final Long owner;
}
