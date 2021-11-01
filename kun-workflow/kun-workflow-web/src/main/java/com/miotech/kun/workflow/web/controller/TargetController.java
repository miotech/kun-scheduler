package com.miotech.kun.workflow.web.controller;

import com.miotech.kun.commons.web.annotation.QueryParameter;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.workflow.common.executetarget.ExecuteTargetService;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class TargetController {

    @Inject
    private ExecuteTargetService executeTargetService;

    @RouteMapping(url= "/targets", method = "GET")
    public List<ExecuteTarget> getTask(@QueryParameter(defaultValue = "1") int pageNum,
                                       @QueryParameter(defaultValue = "100") int pageSize) {
        return executeTargetService.fetchExecuteTargets();
    }
}
