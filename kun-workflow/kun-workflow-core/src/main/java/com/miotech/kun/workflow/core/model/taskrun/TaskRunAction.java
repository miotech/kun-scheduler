package com.miotech.kun.workflow.core.model.taskrun;

import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.event.TaskRunTransitionEventType;

public abstract class TaskRunAction {

    private final TaskRunState currentState;

    private final TaskRunState nextState;

    private final TaskRunTransitionEventType taskRunTransitionEventType;

    public TaskRunAction(TaskRunState currentState, TaskRunState nextState, TaskRunTransitionEventType taskRunTransitionEventType) {
        this.currentState = currentState;
        this.nextState = nextState;
        this.taskRunTransitionEventType = taskRunTransitionEventType;
    }

    public TaskRunState getCurrentState() {
        return currentState;
    }

    public TaskRunState getNextState() {
        return nextState;
    }

    public TaskRunTransitionEventType getTaskRunEventType() {
        return taskRunTransitionEventType;
    }

    public abstract void doAction(TaskRunTransitionEvent taskRunTransitionEvent);
}
