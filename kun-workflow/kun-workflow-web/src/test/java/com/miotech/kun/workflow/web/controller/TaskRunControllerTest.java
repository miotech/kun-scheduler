package com.miotech.kun.workflow.web.controller;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.taskrun.factory.TaskRunLogVOFactory;
import com.miotech.kun.workflow.common.taskrun.factory.TaskRunStateVOFactory;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunService;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunLogVO;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunStateVO;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunVO;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.web.KunWebServerTestBase;
import com.miotech.kun.workflow.web.serializer.JsonSerializer;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class TaskRunControllerTest extends KunWebServerTestBase {

    private final TaskRunService taskRunService = mock(TaskRunService.class);

    @Inject
    private JsonSerializer jsonSerializer;

    @Test
    public void getTaskRunDetail() {
        long testTaskRunId = 1L;
        TaskRunVO testRunVO = TaskRunVO.newBuilder()
                .withId(testTaskRunId)
                .build();

        Mockito.when(taskRunService.getTaskRunDetail(testTaskRunId))
                .thenReturn(Optional.of(testRunVO));

        String response = get("/taskruns/" + testTaskRunId);
        TaskRunVO result = jsonSerializer.toObject(response, TaskRunVO.class);
        assertEquals(testRunVO.getId(), result.getId());
    }

    @Test
    public void getTaskRunStatus() {
        long testTaskRunId = 1L;
        TaskRunStateVO testRunStatus = TaskRunStateVOFactory.create(TaskRunStatus.CREATED);

        Mockito.when(taskRunService.getTaskStatus(testTaskRunId))
                .thenReturn(Optional.of(testRunStatus));

        String response = get("/taskruns/" + testTaskRunId + "/status");
        TaskRunStateVO result = jsonSerializer.toObject(response, TaskRunStateVO.class);
        assertEquals(testRunStatus.getStatus(), result.getStatus());
    }

    @Test
    public void getTaskRunDetail_withNotFound() {
        long testTaskRunId = 1L;
        Mockito.when(taskRunService.getTaskRunDetail(testTaskRunId))
                .thenThrow(new EntityNotFoundException("taskrun not found"));

        String response = get("/taskruns/" + testTaskRunId);
        assertEquals("{\"code\":404,\"message\":\"taskrun not found\"}", response);
    }

    @Test
    public void getTaskRunLog() {
        long testTaskRunId = 1L;
        List<String> logs = new ImmutableList.Builder<String>()
                .add("hello")
                .build();
        TaskRunLogVO taskRunLogVO = TaskRunLogVOFactory.create(testTaskRunId, 1, 0, 10, logs);

        TaskRunLogVO result;
        String response;

        // No parameter
        Mockito.when(taskRunService.getTaskRunLog(testTaskRunId, -1, 0, Long.MAX_VALUE))
                .thenReturn(taskRunLogVO);
        response = get(String.format("/taskruns/%s/logs", testTaskRunId));
        result = jsonSerializer.toObject(response, TaskRunLogVO.class);
        assertEquals(taskRunLogVO.getLogs(), result.getLogs());
        assertEquals(taskRunLogVO.getAttempt(), result.getAttempt());

        // provide parameter
        Mockito.when(taskRunService.getTaskRunLog(
                testTaskRunId,
                taskRunLogVO.getAttempt(),
                taskRunLogVO.getStartLine(),
                taskRunLogVO.getEndLine()))
                .thenReturn(taskRunLogVO);
        response = get(String.format("/taskruns/%s/logs?attempt=%s&startLine=%s&endLine=%s",
                testTaskRunId,
                taskRunLogVO.getAttempt(),
                taskRunLogVO.getStartLine(),
                taskRunLogVO.getEndLine()));
        result = jsonSerializer.toObject(response, TaskRunLogVO.class);
        assertEquals(taskRunLogVO.getLogs(), result.getLogs());
        assertEquals(taskRunLogVO.getAttempt(), result.getAttempt());

        String msg = "startLine should larger or equal to 0";
        Mockito.when(taskRunService.getTaskRunLog(
                testTaskRunId,
                taskRunLogVO.getAttempt(),
                -1,
                -2))
                .thenThrow(new IllegalArgumentException(msg));
        response = get(String.format("/taskruns/%s/logs?attempt=%s&startLine=%s&endLine=%s",
                testTaskRunId,
                taskRunLogVO.getAttempt(),
                -1, -2));
        assertEquals("{\"code\":400,\"message\":\"" + msg +"\"}", response);
    }
}