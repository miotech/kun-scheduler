package com.miotech.kun.workflow.executor;

import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;

public interface EventHandler {
    public void onReceiveSnapshot(WorkerSnapshot workerSnapshot);
    public void onReceivePollingSnapShot(WorkerSnapshot workerSnapshot);
}
