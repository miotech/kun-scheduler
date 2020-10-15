package com.miotech.kun.workflow.worker.local.rpc;

import com.google.inject.Inject;
import com.miotech.kun.workflow.facade.WorkflowWorkerFacade;
import com.miotech.kun.workflow.worker.local.OperatorLauncher;

//@Singleton
public class LocalWorkflowWorkerFacadeImpl implements WorkflowWorkerFacade {

    private OperatorLauncher operatorLauncher;

    @Inject
    public LocalWorkflowWorkerFacadeImpl(OperatorLauncher operatorLauncher){
        this.operatorLauncher = operatorLauncher;
    }

    @Override
    public boolean heartBeat() {
        return operatorLauncher.heartBeatReceive();
    }

    @Override
    public boolean killTask() {
        return operatorLauncher.killTask();
    }
}
