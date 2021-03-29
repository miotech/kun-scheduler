package com.miotech.kun.workflow.executor;

import com.miotech.kun.workflow.core.model.worker.WorkerInstance;

public interface WorkerMonitor{
    public boolean register(WorkerInstance workerInstance, WorkerEventHandler handler);//为pod注册一个watcher监控pod的状态变更
    public boolean unRegister(long taskAttemptId);
}