package com.miotech.kun.workflow.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.commons.pubsub.event.PrivateEvent;

import java.util.Objects;

public class TaskRunTransitionEvent extends PrivateEvent {

    private final TaskRunTransitionEventType type;

    private final Long taskRunId;

    private final Long taskAttemptId;

    private final FromTaskRunContext fromTaskRunContext;

    @JsonCreator
    public TaskRunTransitionEvent(@JsonProperty("type") TaskRunTransitionEventType type,
                                  @JsonProperty("taskAttemptId") Long taskAttemptId,
                                  @JsonProperty("fromTaskRunId") FromTaskRunContext fromTaskRunContext) {
        this(type, null, taskAttemptId, fromTaskRunContext);
    }

    public TaskRunTransitionEvent(TaskRunTransitionEventType type, Long taskRunId, Long taskAttemptId,
                                  FromTaskRunContext fromTaskRunContext) {
        this.type = type;
        this.taskRunId = taskRunId;
        this.taskAttemptId = taskAttemptId;
        this.fromTaskRunContext = fromTaskRunContext;
    }

    public TaskRunTransitionEventType getType() {
        return type;
    }

    public Long getTaskRunId() {
        return taskRunId;
    }

    public Long getTaskAttemptId() {
        return taskAttemptId;
    }

    public FromTaskRunContext getFromTaskRunContext() {
        return fromTaskRunContext;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskRunTransitionEvent event = (TaskRunTransitionEvent) o;
        return type == event.type && taskRunId.equals(event.taskRunId) && taskAttemptId.equals(event.taskAttemptId) && Objects.equals(fromTaskRunContext, event.fromTaskRunContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, taskRunId, taskAttemptId, fromTaskRunContext);
    }

    @Override
    public String toString() {
        return "TaskRunTransitionEvent{" +
                "type=" + type +
                ", taskRunId=" + taskRunId +
                ", taskAttemptId=" + taskAttemptId +
                ", fromTaskRunContext=" + fromTaskRunContext +
                '}';
    }

    public static final class Builder {
        private TaskRunTransitionEventType type;
        private Long taskRunId;
        private Long taskAttemptId;
        private FromTaskRunContext fromTaskRunContext;

        private Builder() {
        }

        public Builder withType(TaskRunTransitionEventType type) {
            this.type = type;
            return this;
        }

        public Builder withTaskRunId(Long taskRunId) {
            this.taskRunId = taskRunId;
            return this;
        }

        public Builder withTaskAttemptId(Long taskAttemptId) {
            this.taskAttemptId = taskAttemptId;
            return this;
        }

        public Builder withFromTaskRunContext(FromTaskRunContext fromTaskRunContext) {
            this.fromTaskRunContext = fromTaskRunContext;
            return this;
        }

        public TaskRunTransitionEvent build() {
            TaskRunTransitionEvent taskRunTransitionEvent = new TaskRunTransitionEvent(type, taskRunId, taskAttemptId, fromTaskRunContext);
            return taskRunTransitionEvent;
        }
    }
}
