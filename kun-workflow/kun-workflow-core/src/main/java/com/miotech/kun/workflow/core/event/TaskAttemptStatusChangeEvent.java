package com.miotech.kun.workflow.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

public class TaskAttemptStatusChangeEvent extends Event {
    private final long attemptId;
    private final TaskRunStatus from;
    private final TaskRunStatus to;
    private final String taskName;
    private final Long taskId;

    public TaskAttemptStatusChangeEvent(long attemptId, TaskRunStatus from, TaskRunStatus to, String taskName, Long taskId) {
        this.attemptId = attemptId;
        this.from = from;
        this.to = to;
        this.taskName = taskName;
        this.taskId = taskId;
    }

    @JsonCreator
    public TaskAttemptStatusChangeEvent(@JsonProperty("timestamp") long timestamp,
                                        @JsonProperty("attemptId") Long attemptId,
                                        @JsonProperty("fromStatus") TaskRunStatus from,
                                        @JsonProperty("toStatus") TaskRunStatus to,
                                        @JsonProperty("taskId") Long taskId,
                                        @JsonProperty("taskName") String taskName) {
        super(timestamp);
        this.attemptId = attemptId;
        this.from = from;
        this.to = to;
        this.taskName = taskName;
        this.taskId = taskId;
    }

    public long getAttemptId() {
        return attemptId;
    }

    public TaskRunStatus getFromStatus() {
        return from;
    }

    public TaskRunStatus getToStatus() {
        return to;
    }

    public String getTaskName() {
        return taskName;
    }

    public Long getTaskId() {
        return taskId;
    }

    @JsonIgnore
    public Long getTaskRunId() {
        // taskAttemptId = taskRunId | attemptNum
        return IdGenerator.getInstance().split(attemptId)[0];
    }
}
