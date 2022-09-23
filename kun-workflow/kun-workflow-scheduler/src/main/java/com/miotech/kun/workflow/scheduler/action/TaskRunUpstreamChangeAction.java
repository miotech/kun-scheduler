package com.miotech.kun.workflow.scheduler.action;

import com.miotech.kun.workflow.core.event.TaskRunTransitionEvent;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunAction;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunSMMessage;
import com.miotech.kun.workflow.scheduler.TaskManager;

public class TaskRunUpstreamChangeAction implements TaskRunAction {

    private final TaskManager taskManager;

    public TaskRunUpstreamChangeAction(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void run(TaskRunSMMessage message) {
        TaskAttempt taskAttempt = message.getTaskAttempt();
        TaskRunTransitionEvent event = message.getEvent();
        taskManager.conditionChange(taskAttempt, event.getFromTaskRunContext());
    }

}
