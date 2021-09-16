package com.miotech.kun.workflow.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.commons.pubsub.event.PublicEvent;

public class TaskRunCreatedEvent extends PublicEvent {

    private final Long taskId;

    private final Long taskRunId;

    public TaskRunCreatedEvent(Long taskId, Long taskRunId) {
        this.taskId = taskId;
        this.taskRunId = taskRunId;
    }

    @JsonCreator
    public TaskRunCreatedEvent(@JsonProperty("timestamp") long timestamp, @JsonProperty("taskId") Long taskId,
                               @JsonProperty("taskRunId") Long taskRunId) {
        super(timestamp);
        this.taskId = taskId;
        this.taskRunId = taskRunId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getTaskRunId() {
        return taskRunId;
    }

}