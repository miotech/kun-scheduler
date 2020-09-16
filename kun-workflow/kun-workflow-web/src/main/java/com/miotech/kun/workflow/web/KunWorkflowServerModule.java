package com.miotech.kun.workflow.web;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.annotation.BasePackageScan;
import com.miotech.kun.workflow.common.AppModule;
import com.miotech.kun.workflow.core.Executor;
import com.miotech.kun.workflow.executor.local.LocalExecutor;


public class KunWorkflowServerModule extends AppModule {

    public KunWorkflowServerModule(Props props) {
        super(props);
    }

    @Override
    protected void configure() {
        super.configure();
        bind(Executor.class).to(LocalExecutor.class);
    }

    @Provides
    @Singleton
    @BasePackageScan
    public String getPackageScan() {
        return this.getClass().getPackage().getName();
    }

}
