package com.miotech.kun.workflow.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.common.operator.service.OperatorService;
import com.miotech.kun.workflow.core.model.bo.TaskInfo;
import com.miotech.kun.workflow.web.annotation.RequestBody;
import com.miotech.kun.workflow.web.annotation.RouteMapping;

import javax.servlet.http.HttpServletRequest;

@Singleton
public class OperatorController {

    private OperatorService operatorService;

    @Inject
    public OperatorController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @RouteMapping(url= "/operators", method = "GET")
    public Object getTask(HttpServletRequest request) {
        return null;
    }

    @RouteMapping(url= "/operators", method = "POST")
    public Object createTask(@RequestBody TaskInfo taskBody) {
        return null;
    }

    @RouteMapping(url= "/operators", method = "DELETE")
    public Object deleteTask(HttpServletRequest request) {
        return null;
    }

    @RouteMapping(url= "/operators", method = "PUT")
    public Object updateTask(HttpServletRequest request) {
        return null;
    }

}
