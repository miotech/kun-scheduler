package com.miotech.kun.workflow.executor;

import com.google.common.util.concurrent.ListenableFuture;

public interface TaskRunner {
    /**
     * 执行任务。
     * @return
     */
    public ListenableFuture<ExecResult> run(ExecCommand command);

    /**
     * 终止任务
     */
    public void abort();

    /**
     * 强制终止任务
     */
    public void forceAbort();
}
