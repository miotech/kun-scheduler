package com.miotech.kun.workflow.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunService;
import com.miotech.kun.workflow.core.model.vo.TaskRunVO;
import com.miotech.kun.workflow.web.annotation.RouteVariable;
import com.miotech.kun.workflow.web.annotation.RouteMapping;

import java.util.List;
import java.util.Optional;

@Singleton
public class TaskRunController {
    @Inject
    private TaskRunService taskRunService;

    @RouteMapping(url = "/taskruns/{taskRunId}", method = "GET")
    public TaskRunVO getTaskRunDetail(@RouteVariable long taskRunId) {
        Optional<TaskRunVO> voOptional = taskRunService.getTaskRunDetail(taskRunId);
        return voOptional.orElseThrow(EntityNotFoundException::new);
    }

    @RouteMapping(url = "/taskruns", method = "GET")
    public List<TaskRunVO> getTaskRuns() {
        return null;
    }
}
