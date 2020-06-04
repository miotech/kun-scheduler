package com.miotech.kun.workflow.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunService;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunLogVO;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunStateVO;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunVO;
import com.miotech.kun.workflow.web.annotation.QueryParameter;
import com.miotech.kun.workflow.web.annotation.RouteVariable;
import com.miotech.kun.workflow.web.annotation.RouteMapping;
import com.miotech.kun.workflow.web.annotation.VALUE_DEFAULT;


@Singleton
public class TaskRunController {

    @Inject
    private TaskRunService taskRunService;

    @RouteMapping(url = "/taskruns/{taskRunId}", method = "GET")
    public TaskRunVO getTaskRunDetail(@RouteVariable long taskRunId) {
        return taskRunService.getTaskRunDetail(taskRunId)
                .orElseThrow(() -> new EntityNotFoundException("TaskRun with id \"" + taskRunId + "\" not found"));
    }


    @RouteMapping(url = "/taskruns/{taskRunId}/status", method = "GET")
    public TaskRunStateVO getTaskRunStatus(@RouteVariable long taskRunId) {
        return taskRunService.getTaskStatus(taskRunId)
                .orElseThrow(() -> new EntityNotFoundException("TaskRun with id \"" + taskRunId + "\" not found"));
    }

    /**
     *
     * @param taskRunId
     * @param attempt: if attempt not specified, use -1 mark as latest
     * @param startLine
     * @param endLine
     * @return
     */
    @RouteMapping(url = "/taskruns/{taskRunId}/logs", method = "GET")
    public TaskRunLogVO getTaskRunLog(@RouteVariable long taskRunId,
                                      @QueryParameter(defaultValue = "-1") int attempt,
                                      @QueryParameter(defaultValue = "0") long startLine,
                                      @QueryParameter(defaultValue = VALUE_DEFAULT.MAX_LINES) long endLine) {
        return taskRunService.getTaskRunLog(taskRunId, attempt, startLine, endLine);
    }
}
