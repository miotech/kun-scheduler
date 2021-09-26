package com.miotech.kun.dataplatform.web.common.taskdefinition.vo;

import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskPayload;
import lombok.Data;

@Data
public class UpdateTaskDefinitionRequest {

    private final Long definitionId;

    private final String name;

    private final TaskPayload taskPayload;

    private final Long owner;
}
