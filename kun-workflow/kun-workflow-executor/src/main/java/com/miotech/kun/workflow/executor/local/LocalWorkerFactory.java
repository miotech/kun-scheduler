package com.miotech.kun.workflow.executor.local;

import com.miotech.kun.workflow.core.execution.HeartBeatMessage;
import com.miotech.kun.workflow.executor.WorkerFactory;
import com.miotech.kun.workflow.facade.WorkflowWorkerFacade;
import com.miotech.kun.workflow.worker.Worker;
import com.miotech.kun.workflow.worker.local.LocalWorker;

import javax.inject.Inject;

public class LocalWorkerFactory implements WorkerFactory {

    @Inject
    private WorkflowWorkerFacade workerFacade;

    public Worker createWorker(){
        return new LocalWorker(workerFacade);
    }

    @Override
    public Worker getWorker(HeartBeatMessage message) {
        LocalWorker localWorker = new LocalWorker(workerFacade);
        localWorker.bind(message);
        return localWorker;
    }

    @Override
    public Boolean killWorker(HeartBeatMessage message) {
        Worker worker = getWorker(message);
        return worker.forceAbort();
    }
}
