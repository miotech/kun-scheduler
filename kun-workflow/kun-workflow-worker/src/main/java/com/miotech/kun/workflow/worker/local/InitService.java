package com.miotech.kun.workflow.worker.local;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.rpc.RpcPublisher;
import com.miotech.kun.workflow.facade.WorkflowWorkerFacade;

@Singleton
public class InitService {

    @Inject
    private RpcPublisher rpcPublisher;
    @Inject
    private WorkflowWorkerFacade workerFacade;

    public void publishRpcServices() {
        rpcPublisher.exportService(WorkflowWorkerFacade.class, "1.0", workerFacade);
    }
}
