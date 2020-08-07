package com.miotech.kun.workflow.executor.local.thread;

import com.google.common.util.concurrent.ListenableFuture;
import com.miotech.kun.workflow.executor.ExecCommand;
import com.miotech.kun.workflow.executor.ExecResult;
import com.miotech.kun.workflow.executor.TaskRunner;

// TODO: need to implement
public class ThreadTaskRunner implements TaskRunner {
    @Override
    public ListenableFuture<ExecResult> run(ExecCommand command) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forceAbort() {
        throw new UnsupportedOperationException();
    }
}
