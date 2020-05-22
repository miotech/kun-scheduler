package com.miotech.kun.workflow.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.common.taskrun.service.TaskRunService;
import com.miotech.kun.workflow.core.model.vo.TaskRunVO;
import com.miotech.kun.workflow.web.annotation.RouteVariable;
import com.miotech.kun.workflow.web.annotation.RouteMapping;

import java.util.List;

@Singleton
public class TaskRunController {
    @Inject
    private TaskRunService taskRunService;

    @RouteMapping(url = "/taskruns/{taskRunId}", method = "GET")
    public TaskRunVO getTaskRunDetail(@RouteVariable long taskRunId) {
        return taskRunService.getTaskRunDetail(taskRunId);
    }

    @RouteMapping(url = "/taskruns", method = "GET")
    public List<TaskRunVO> getTaskRuns() {
        return null;
    }
}
