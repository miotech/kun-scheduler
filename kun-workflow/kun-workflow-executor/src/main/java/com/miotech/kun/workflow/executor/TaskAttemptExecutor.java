package com.miotech.kun.workflow.executor;

import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;

public interface TaskAttemptExecutor{
    public boolean submit(TaskAttempt taskAttempt);
    public boolean cancel(long taskAttemptId);
}
