package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;

public interface TaskAttemptExecutor{
    public boolean submit(TaskAttempt taskAttempt);

    public void execute(TaskAttempt taskAttempt);

    public void check(TaskAttempt taskAttempt);

    public boolean cancel(Long taskAttemptId);
}
