package com.miotech.kun.workflow.core.model.taskrun;

import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;

public class TaskRunSMMessage {

    private final TaskRunTransitionEvent event;
    private final TaskAttempt taskAttempt;

    public TaskRunSMMessage(TaskRunTransitionEvent event, TaskAttempt taskAttempt) {
        this.event = event;
        this.taskAttempt = taskAttempt;
    }

    public TaskRunTransitionEvent getEvent() {
        return event;
    }

    public TaskAttempt getTaskAttempt() {
        return taskAttempt;
    }
}
