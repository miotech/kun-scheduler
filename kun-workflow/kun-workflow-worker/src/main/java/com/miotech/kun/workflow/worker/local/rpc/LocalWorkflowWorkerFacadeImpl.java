package com.miotech.kun.workflow.worker.local.rpc;

import com.google.inject.Inject;
import com.miotech.kun.workflow.facade.WorkflowWorkerFacade;
import com.miotech.kun.workflow.worker.local.OperatorLauncher;

public class LocalWorkflowWorkerFacadeImpl implements WorkflowWorkerFacade {

    private OperatorLauncher operatorLauncher;

    @Inject
    public LocalWorkflowWorkerFacadeImpl(OperatorLauncher operatorLauncher){
        this.operatorLauncher = operatorLauncher;
    }

    @Override
    public boolean killTask(Boolean abortByUser) {
        return operatorLauncher.killTask(abortByUser);
    }
}
