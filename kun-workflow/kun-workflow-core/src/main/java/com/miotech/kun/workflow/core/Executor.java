package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;

public interface Executor {
    /**
     * 提交一个TaskAttempt以运行。
     * @param taskAttempt
     */
    public void submit(TaskAttempt taskAttempt);

    /**
     * 取消一个TaskAttempt的运行。
     * @param taskAttempt
     * @return
     */
    public default boolean cancel(TaskAttempt taskAttempt) {
        return cancel(taskAttempt.getId());
    }

    /**
     * 取消一个TaskAttempt的运行。
     * @param taskAttemptId
     * @return
     */
    public boolean cancel(Long taskAttemptId);
}
