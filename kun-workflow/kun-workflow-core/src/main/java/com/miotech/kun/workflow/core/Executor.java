package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;

public interface Executor {
    public void submit(TaskAttempt taskAttempt);
}
