package com.miotech.kun.workflow.scheduler.action;

import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunAction;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunSMMessage;

public class TaskRunSubmitAction implements TaskRunAction {

    private final Executor executor;

    public TaskRunSubmitAction(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void run(TaskRunSMMessage message) {
        TaskAttempt taskAttempt = message.getTaskAttempt();
        executor.submit(taskAttempt);
    }

}
