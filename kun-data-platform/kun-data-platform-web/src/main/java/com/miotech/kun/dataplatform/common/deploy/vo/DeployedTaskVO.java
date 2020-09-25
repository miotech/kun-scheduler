package com.miotech.kun.dataplatform.common.deploy.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskPayload;
import lombok.Data;

@Data
public class DeployedTaskVO {

    private final Long id;

    private final String name;

    private final String taskTemplateName;

    private final Long owner;

    @JsonProperty("isArchived")
    private final boolean archived;

    private final TaskPayload taskPayload;
}