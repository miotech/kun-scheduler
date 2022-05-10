package com.miotech.kun.dataplatform.web.common.taskdefinition.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskPayload;
import com.miotech.kun.dataplatform.web.common.commit.vo.TaskCommitVO;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class TaskDefinitionVO {
    private final Long id;

    private final String name;

    private final String taskTemplateName;

    private final TaskPayload taskPayload;

    private final String creator;

    @JsonProperty("isArchived")
    private final boolean archived;

    @JsonProperty("isDeployed")
    private final boolean deployed;

    @JsonProperty("isUpdated")
    private final boolean updated;

    private final String owner;

    private final List<TaskDefinitionProps> upstreamTaskDefinitions;

    private final OffsetDateTime createTime;

    private final OffsetDateTime lastUpdateTime;

    private final String lastModifier;

    private final List<TaskCommitVO> taskCommits;

    public TaskDefinitionVO(Long id,
                            String name,
                            String taskTemplateName,
                            TaskPayload taskPayload,
                            String creator,
                            boolean archived,
                            boolean deployed,
                            boolean updated,
                            String owner,
                            List<TaskDefinitionProps> upstreamTaskDefinitions,
                            String lastModifier,
                            OffsetDateTime lastUpdateTime,
                            OffsetDateTime createTime,
                            List<TaskCommitVO> taskCommits
                            ) {
        this.id = id;
        this.name = name;
        this.taskTemplateName = taskTemplateName;
        this.taskPayload = taskPayload;
        this.creator = creator;
        this.archived = archived;
        this.deployed = deployed;
        this.updated = updated;
        this.owner = owner;
        this.upstreamTaskDefinitions = upstreamTaskDefinitions;
        this.lastModifier = lastModifier;
        this.lastUpdateTime = lastUpdateTime;
        this.createTime = createTime;
        this.taskCommits = taskCommits;
    }
}
