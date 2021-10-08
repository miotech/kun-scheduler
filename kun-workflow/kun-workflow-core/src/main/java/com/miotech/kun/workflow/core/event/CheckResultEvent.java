package com.miotech.kun.workflow.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.commons.pubsub.event.PublicEvent;

public class CheckResultEvent extends PublicEvent {
    private final long taskRunId;
    private final boolean checkStatus;

    public CheckResultEvent(long taskRunId, boolean checkStatus) {
        this.taskRunId = taskRunId;
        this.checkStatus = checkStatus;
    }

    @JsonCreator
    public CheckResultEvent(@JsonProperty("timestamp") long timestamp,
                            @JsonProperty("taskRunId") Long taskRunId,
                            @JsonProperty("checkStatus") boolean checkStatus) {
        super(timestamp);
        this.taskRunId = taskRunId;
        this.checkStatus = checkStatus;
    }

    public long getTaskRunId() {
        return taskRunId;
    }

    public boolean getCheckStatus() {
        return checkStatus;
    }

    @Override
    public String toString() {
        return "CheckResultEvent{" +
                "taskRunId=" + taskRunId +
                ", checkStatus=" + checkStatus +
                '}';
    }
}
