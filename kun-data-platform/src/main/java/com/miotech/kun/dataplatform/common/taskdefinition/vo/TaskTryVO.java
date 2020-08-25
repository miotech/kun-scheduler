package com.miotech.kun.dataplatform.common.taskdefinition.vo;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import lombok.Data;
import org.json.simple.JSONObject;

@Data
public class TaskTryVO {
    private final Long id;

    private final Long definitionId;

    private final Long workflowTaskId;

    private final Long workflowTaskRunId;

    private final Long creator;

    private final TaskRunStatus status;

    private final JSONObject taskConfig;
}
