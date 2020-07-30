package com.miotech.kun.dataplatform.common.taskdefinition.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import lombok.Data;

import java.util.List;

@Data
public class TaskRunLogVO {

    private final long taskRunId;

    private final int attempt;

    private final long startLine;

    private final long endLine;

    private final List<String> logs;

    @JsonProperty("isTerminated")
    private final boolean terminated;

    private final TaskRunStatus status;
}

