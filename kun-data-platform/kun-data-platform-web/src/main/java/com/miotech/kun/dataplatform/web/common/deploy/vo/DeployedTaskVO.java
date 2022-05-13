package com.miotech.kun.dataplatform.web.common.deploy.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskPayload;
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

    private final String owner;

    @JsonProperty("isArchived")
    private final boolean archived;

    private final TaskPayload taskPayload;
}