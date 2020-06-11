package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;

import java.util.List;

public interface Executor {
    public void submit(TaskAttempt taskAttempt);

    public void submit(List<TaskAttempt> taskAttempts);
}
