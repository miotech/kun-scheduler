package com.miotech.kun.workflow.executor;

public interface WorkerMonitor{
    public boolean register(Long taskAttemptId, WorkerEventHandler handler);//为pod注册一个watcher监控pod的状态变更
    public boolean unRegister(Long taskAttemptId);
    public void unRegisterAll();
}