package com.miotech.kun.workflow.core.event;

import com.miotech.kun.commons.pubsub.event.PrivateEvent;

public class TaskRunTransitionEvent extends PrivateEvent {

    private final TaskRunTransitionEventType type;

    private final Long taskRunId;

    private final Long taskAttemptId;

    public TaskRunTransitionEvent(TaskRunTransitionEventType type, Long taskAttemptId) {
        this(type,null,taskAttemptId);
    }

    public TaskRunTransitionEvent(TaskRunTransitionEventType type, Long taskRunId, Long taskAttemptId){
        this.type = type;
        this.taskRunId = taskRunId;
        this.taskAttemptId = taskAttemptId;
    }

    public TaskRunTransitionEventType getType(){
        return type;
    }

    public Long getTaskRunId(){
        return taskRunId;
    }

    public Long getTaskAttemptId() {
        return taskAttemptId;
    }

    @Override
    public String toString() {
        return "TaskRunEvent{" +
                "type=" + type +
                ", taskRunId=" + taskRunId +
                ", taskAttemptId=" + taskAttemptId +
                '}';
    }
}
