package com.miotech.kun.workflow.executor;

import com.miotech.kun.workflow.core.model.worker.WorkerSnapshot;

public interface WorkerEventHandler {
    public void onReceiveSnapshot(WorkerSnapshot workerSnapshot);
    public void onReceivePollingSnapShot(WorkerSnapshot workerSnapshot);
}
