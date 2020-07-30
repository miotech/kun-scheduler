package com.miotech.kun.dataplatform.common.taskdefinition.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskPayload;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class TaskDefinitionVO {
    private final Long id;

    private final String name;

    private final String taskTemplateName;

    private final TaskPayload taskPayload;

    private final Long creator;

    @JsonProperty("isArchived")
    private final boolean archived;

    @JsonProperty("isDeployed")
    private final boolean deployed;

    private final Long owner;

    private final List<TaskDefinitionProps> upstreamTaskDefinitions;

    private final OffsetDateTime createTime;

    private final OffsetDateTime lastUpdateTime;

    private final Long lastModifier;

    public TaskDefinitionVO(Long id,
                            String name,
                            String taskTemplateName,
                            TaskPayload taskPayload,
                            Long creator,
                            boolean archived,
                            boolean deployed,
                            Long owner,
                            List<TaskDefinitionProps> upstreamTaskDefinitions,
                            Long lastModifier,
                            OffsetDateTime lastUpdateTime,
                            OffsetDateTime createTime
                            ) {
        this.id = id;
        this.name = name;
        this.taskTemplateName = taskTemplateName;
        this.taskPayload = taskPayload;
        this.creator = creator;
        this.archived = archived;
        this.deployed = deployed;
        this.owner = owner;
        this.upstreamTaskDefinitions = upstreamTaskDefinitions;
        this.lastModifier = lastModifier;
        this.lastUpdateTime = lastUpdateTime;
        this.createTime = createTime;
    }
}
