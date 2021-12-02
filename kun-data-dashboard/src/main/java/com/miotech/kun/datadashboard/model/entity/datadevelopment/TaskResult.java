package com.miotech.kun.datadashboard.model.entity.datadevelopment;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import lombok.Data;

@Data
public class TaskResult {

    String status;

    TaskRunStatus finalStatus;

    Integer taskCount;

    public TaskResult(String status, TaskRunStatus finalStatus, Integer taskCount) {
        this.status = status;
        this.finalStatus = finalStatus;
        this.taskCount = taskCount;
    }
}
