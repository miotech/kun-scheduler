package com.miotech.kun.workflow.common.workflow.service;

import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.Executor;

import javax.inject.Inject;

@Singleton
public class WorkflowService {

    @Inject
    private Executor executor;

    public boolean getMaintenanceMode() {
        return executor.getMaintenanceMode();
    }

    public void setMaintenanceMode(boolean mode) {
        executor.setMaintenanceMode(mode);
    }
}
