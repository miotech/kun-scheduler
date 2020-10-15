package com.miotech.kun.workflow.web.service;

import com.google.inject.Inject;
import com.miotech.kun.commons.rpc.RpcPublisher;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;

public class InitService {

    @Inject
    private RpcPublisher rpcPublisher;
    @Inject
    private WorkflowExecutorFacade executorFacade;

    public void publishRpcServices() {
        rpcPublisher.exportService(WorkflowExecutorFacade.class, "1.0", executorFacade);
    }
}
