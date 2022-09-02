package com.miotech.kun.dataplatform.web.common.deploy.vo;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.model.PageRequest;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = false)
@Data
public class ScheduledTaskRunSearchRequest extends PageRequest {

    private List<Long> definitionIds;

    private Optional<String> owner;

    private String taskTemplateName;

    private String name;

    private TaskRunStatus status;

    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    private List<String> scheduleTypes;

    private Long locateTaskRunId;

    public ScheduledTaskRunSearchRequest(Integer pageSize,
                                         Integer pageNum,
                                         Optional<String> owner,
                                         List<Long> definitionIds,
                                         String taskTemplateName,
                                         String name,
                                         TaskRunStatus status,
                                         OffsetDateTime startTime,
                                         OffsetDateTime endTime,
                                         List<String> scheduleTypes,
                                         Long locateTaskRunId) {
        super(pageSize, pageNum);
        this.owner = owner;
        this.definitionIds = definitionIds != null ? definitionIds : ImmutableList.of();
        this.taskTemplateName = taskTemplateName;
        this.name = name;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.scheduleTypes = scheduleTypes;
        this.locateTaskRunId = locateTaskRunId;
    }
}