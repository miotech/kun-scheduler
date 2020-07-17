package com.miotech.kun.workflow.client.model;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskRunState {

    private TaskRunStatus status;

    public TaskRunState() {
        this.status = TaskRunStatus.CREATED;
    }

    public TaskRunState(TaskRunStatus status) {
        this.status = status;
    }

    public void setStatus(TaskRunStatus status) {
        this.status = status;
    }

    public TaskRunStatus getStatus() {
        return status;
    }
}
