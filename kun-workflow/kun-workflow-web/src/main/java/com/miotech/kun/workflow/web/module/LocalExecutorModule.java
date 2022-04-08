package com.miotech.kun.workflow.web.module;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.executor.AbstractQueueManager;
import com.miotech.kun.workflow.executor.WorkerLifeCycleManager;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.executor.local.LocalExecutor;
import com.miotech.kun.workflow.executor.local.LocalProcessLifeCycleManager;
import com.miotech.kun.workflow.executor.local.LocalProcessMonitor;
import com.miotech.kun.workflow.executor.local.LocalQueueManage;

public class LocalExecutorModule extends AppModule {

    private final Props props;

    public LocalExecutorModule(Props props) {
        super(props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(Executor.class).to(LocalExecutor.class);
        bind(WorkerLifeCycleManager.class).to(LocalProcessLifeCycleManager.class);
        bind(AbstractQueueManager.class).to(LocalQueueManage.class);
        bind(WorkerMonitor.class).to(LocalProcessMonitor.class);
    }
}
