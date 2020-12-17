package com.miotech.kun.workflow.executor;

import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.worker.Worker;

public interface WorkerFactory {

    public Worker createWorker();

    public Worker getWorker(HeartBeatMessage message);

    public Boolean killWorker(HeartBeatMessage message);
}
