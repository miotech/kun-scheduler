package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;

public interface Executor {
    public void submit(TaskAttempt taskAttempt);

    public default boolean cancel(TaskAttempt taskAttempt) {
        return cancel(taskAttempt.getId());
    }

    public boolean cancel(Long taskAttemptId);
}
