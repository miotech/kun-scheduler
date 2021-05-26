package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.miotech.kun.commons.utils.CustomDateTimeDeserializer;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.OffsetDateTime;

public class TaskAttempt {
    private Long id;

    private Long taskRunId;

    private Long taskId;

    private String taskName;

    private int attempt;

    private String queueName;

    private TaskRunStatus status;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private OffsetDateTime startAt;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private OffsetDateTime endAt;

    public Long getId() {
        return id;
    }

    public Long getTaskRunId() {
        return taskRunId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getAttempt() {
        return attempt;
    }

    public TaskRunStatus getStatus() {
        return status;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public String getQueueName() {
        return queueName;
    }
}
