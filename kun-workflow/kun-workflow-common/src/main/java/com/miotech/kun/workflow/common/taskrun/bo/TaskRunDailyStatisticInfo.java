package com.miotech.kun.workflow.common.taskrun.bo;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.OffsetDateTime;

public class TaskRunDailyStatisticInfo {
    private final OffsetDateTime day;

    private final TaskRunStatus status;

    private final Integer count;

    public TaskRunDailyStatisticInfo(OffsetDateTime day, TaskRunStatus status, Integer count) {
        this.day = day;
        this.status = status;
        this.count = count;
    }
}
