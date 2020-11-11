package com.miotech.kun.workflow.worker.local;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.miotech.kun.commons.rpc.RpcConsumer;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.annotation.BasePackageScan;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;
import com.miotech.kun.workflow.facade.WorkflowWorkerFacade;
import com.miotech.kun.workflow.worker.Worker;
import com.miotech.kun.workflow.worker.local.rpc.LocalWorkflowWorkerFacadeImpl;

public class WorkerModule  extends AbstractModule {
    private final Props props;

    private final Integer RPC_TIMEOUT = 5000;

    public WorkerModule(Props props) {
        this.props = props;
    }

    @Override
    protected void configure() {
        Names.bindProperties(binder(), props.toProperties());
        bind(WorkflowWorkerFacade.class).to(LocalWorkflowWorkerFacadeImpl.class);
        bind(Worker.class).to(LocalWorker.class);
        bind(Props.class).toInstance(props);
    }

    @Provides
    @Singleton
    @BasePackageScan
    public String getPackageScan() {
        return this.getClass().getPackage().getName();
    }

    @Singleton
    @Provides
    public WorkflowExecutorFacade workflowExecutorFacade(RpcConsumer rpcConsumer) {
        rpcConsumer.setTimeout(RPC_TIMEOUT);
        return rpcConsumer.getService("default", WorkflowExecutorFacade.class, "1.0");
    }
}
