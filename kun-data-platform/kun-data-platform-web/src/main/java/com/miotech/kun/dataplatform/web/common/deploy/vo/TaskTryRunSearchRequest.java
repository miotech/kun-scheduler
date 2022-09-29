package com.miotech.kun.dataplatform.web.common.deploy.vo;

import com.miotech.kun.common.model.PageRequest;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class TaskTryRunSearchRequest extends PageRequest {

    private List<Long> definitionIds;

    private TaskRunStatus status;

    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    public TaskTryRunSearchRequest(Integer pageSize, Integer pageNum, List<Long> definitionIds, TaskRunStatus status,
                                   OffsetDateTime startTime, OffsetDateTime endTime) {
        super(pageSize, pageNum);
        this.definitionIds = definitionIds;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
