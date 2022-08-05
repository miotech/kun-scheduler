package com.miotech.kun.workflow.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.workflow.core.model.executor.ExecutorInfo;
import com.miotech.kun.workflow.common.workflow.service.WorkflowService;

@Singleton
public class WorkflowController {

    @Inject
    private WorkflowService workflowService;

    @RouteMapping(url = "/getMaintenanceMode", method = "GET")
    public boolean getMaintenanceMode() {
        return workflowService.getMaintenanceMode();
    }

    @RouteMapping(url = "/setMaintenanceMode", method = "POST")
    public void setMaintenanceMode(@QueryParameter Boolean mode) {
        workflowService.setMaintenanceMode(mode);
    }

    @RouteMapping(url = "/getExecutorInfo", method = "GET")
    public ExecutorInfo getExecutorInfo() {
        return workflowService.getExecutorInfo();
    }
}
