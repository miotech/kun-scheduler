package com.miotech.kun.workflow.web.module;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.workflow.LocalScheduler;
import com.miotech.kun.workflow.TaskRunStateMachine;
import com.miotech.kun.workflow.common.graph.DatabaseTaskGraph;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunStatistic;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.executor.DispatchExecutor;
import com.miotech.kun.workflow.executor.kubernetes.*;
import com.miotech.kun.workflow.facade.WorkflowServiceFacade;
import com.miotech.kun.workflow.web.service.RecoverService;
import com.miotech.kun.workflow.web.service.WorkflowServiceFacadeImpl;

public class DispatchExecutorModule extends AppModule {

    private final Props props;

    public DispatchExecutorModule(Props props) {
        super(props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(Executor.class).to(DispatchExecutor.class);
        install(new FactoryModuleBuilder()
                .build(KubernetesExecutorFactory.class));
        install(new FactoryModuleBuilder()
                .build(KubernetesResourceManagerFactory.class));
        install(new FactoryModuleBuilder()
                .build(PodLifeCycleManagerFactory.class));
        install(new FactoryModuleBuilder()
                .build(PodEventMonitorFactory.class));
    }
}
