package com.miotech.kun.workflow.web;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.annotation.BasePackageScan;
import com.miotech.kun.workflow.common.AppModule;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.executor.WorkerFactory;
import com.miotech.kun.workflow.executor.local.LocalExecutor;
import com.miotech.kun.workflow.executor.local.LocalWorkerFactory;
import com.miotech.kun.workflow.executor.rpc.LocalExecutorFacadeImpl;
import com.miotech.kun.workflow.executor.rpc.WorkerClusterConsumer;
import com.miotech.kun.workflow.facade.WorkflowExecutorFacade;
import com.miotech.kun.workflow.facade.WorkflowWorkerFacade;


public class KunWorkflowServerModule extends AppModule {

    public KunWorkflowServerModule(Props props) {
        super(props);
    }

    @Override
    protected void configure() {
        super.configure();
        bind(Executor.class).to(LocalExecutor.class);
        bind(WorkerFactory.class).to(LocalWorkerFactory.class);
        bind(WorkflowExecutorFacade.class).to(LocalExecutorFacadeImpl.class);
    }

    @Provides
    @Singleton
    @BasePackageScan
    public String getPackageScan() {
        return this.getClass().getPackage().getName();
    }

    @Singleton
    @Provides
    public WorkerClusterConsumer workerRpcConsumer() {
//        checkState(RpcBootstrap.isStarted(), "rpc framework should be bootstrapped.");
        return new WorkerClusterConsumer();
    }


    @Singleton
    @Provides
    public WorkflowWorkerFacade workerFacade(WorkerClusterConsumer workerClusterConsumer){
        return workerClusterConsumer.getService("default", WorkflowWorkerFacade.class, "1.0");
    }

}
