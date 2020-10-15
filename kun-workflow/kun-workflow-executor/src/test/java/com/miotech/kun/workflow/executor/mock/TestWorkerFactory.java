package com.miotech.kun.workflow.executor.mock;

import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.executor.WorkerFactory;
import com.miotech.kun.workflow.worker.Worker;

public class TestWorkerFactory implements WorkerFactory {
    @Override
    public Worker createWorker() {
        return null;
    }

    @Override
    public Worker getWorker(HeartBeatMessage message) {
        return null;
    }
}
