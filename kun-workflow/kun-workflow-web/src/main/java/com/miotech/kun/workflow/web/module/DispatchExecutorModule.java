package com.miotech.kun.workflow.web.module;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.executor.DispatchExecutor;
import com.miotech.kun.workflow.executor.kubernetes.*;

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
