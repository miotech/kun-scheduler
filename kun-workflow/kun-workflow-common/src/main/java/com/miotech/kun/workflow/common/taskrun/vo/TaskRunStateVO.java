package com.miotech.kun.workflow.common.taskrun.vo;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunStateVO {

    private TaskRunStatus status;

    public void setStatus(TaskRunStatus status) {
        this.status = status;
    }

    public TaskRunStatus getStatus() {
        return status;
    }
}
