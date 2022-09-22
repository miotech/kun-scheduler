package com.miotech.kun.workflow.scheduler.action;

import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunAction;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunSMMessage;
import com.miotech.kun.workflow.scheduler.TaskManager;

public class TaskRunRetryAction implements TaskRunAction {

    private final TaskManager taskManager;

    public TaskRunRetryAction(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void run(TaskRunSMMessage message) {
        TaskAttempt taskAttempt = message.getTaskAttempt();
        taskManager.reschedule(taskAttempt.getTaskRun());
    }

}
