package com.miotech.kun.dataplatform.common.deploy.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskPayload;
import lombok.Data;

@Data
public class DeployedTaskVO {
    @JsonSerialize(using= ToStringSerializer.class)
    private final Long id;

    @JsonSerialize(using= ToStringSerializer.class)
    private final Long workflowTaskId;

    @JsonSerialize(using= ToStringSerializer.class)
    private final Long taskDefinitionId;

    private final String name;

    private final String taskTemplateName;

    @JsonSerialize(using= ToStringSerializer.class)
    private final Long owner;

    @JsonProperty("isArchived")
    private final boolean archived;

    private final TaskPayload taskPayload;
}