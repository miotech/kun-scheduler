package com.miotech.kun.workflow.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miotech.kun.commons.testing.MockServerTestBase;
import com.miotech.kun.workflow.client.model.*;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.miotech.kun.workflow.client.mock.MockingFactory.*;
import static com.miotech.kun.workflow.utils.DateTimeUtils.atMillisecondPrecision;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class WorkflowApiTest extends MockServerTestBase {

    private WorkflowApi wfApi = new WorkflowApi(getAddress());

    @Test
    public void createOperator() {
        Operator operator = mockOperator();
        mockPost("/operators", JSONUtils.toJsonString(operator), JSONUtils.toJsonString(operator));
        Operator result = wfApi.createOperator(operator);
        assertTrue(result.getId() > 0);
        assertThat(result.getName(), is(operator.getName()));
        assertThat(result.getClassName(), is(operator.getClassName()));
        assertThat(result.getDescription(), is(operator.getDescription()));
    }

    @Test
    public void searchOperators() {
        Operator operator = mockOperator();
        List<Operator> operators = Collections.singletonList(operator);
        mockGet("/operators?name=" + operator.getName(), JSONUtils.toJsonString(new PaginationResult<>(1, 0, 1, operators)));

        OperatorSearchRequest request = OperatorSearchRequest
                .newBuilder()
                .withName(operator.getName())
                .build();
        PaginationResult<Operator> result = wfApi.getOperators(request);
        assertThat(result.getRecords().size(), is(1));
        Operator op = result.getRecords().get(0);
        assertTrue(op.getId() > 0);
        assertThat(op.getName(), is(operator.getName()));
        assertThat(op.getClassName(), is(operator.getClassName()));
        assertThat(op.getDescription(), is(operator.getDescription()));
    }

    @Test
    public void getOperator() {
        Operator operator = mockOperator();
        mockGet("/operators/1", JSONUtils.toJsonString(operator));
        assertThat(wfApi.getOperator(1L), sameBeanAs(operator));
    }

    @Test
    public void updateOperator() {
        Operator operator = mockOperator();
        mockPut("/operators/" + 1, JSONUtils.toJsonString(operator),  JSONUtils.toJsonString(operator));
        assertThat(wfApi.updateOperator(1L, operator), sameBeanAs(operator));
    }

    @Test
    public void deleteOperator() {
        mockDelete("/operators/" + 1, mockDeleteResponse());
        wfApi.deleteOperator(1L);
    }

    @Test
    public void createTask() {
        Task task = mockTask();
        mockPost("/tasks", JSONUtils.toJsonString(task),  JSONUtils.toJsonString(task));
        Task result = wfApi.createTask(task);
        assertTrue(result.getId() > 0);
        assertThat(result.getName(), is(task.getName()));
        assertThat(result.getDependencies(), is(task.getDependencies()));
        assertThat(result.getDescription(), is(task.getDescription()));
        assertThat(result.getScheduleConf(), is(task.getScheduleConf()));
        assertThat(result.getTags(), is(task.getTags()));
    }

    @Test
    public void getTask() {
        Task task = mockTask();
        mockGet("/tasks/1" , JSONUtils.toJsonString(task));
        Task result = wfApi.getTask(1L);
        assertTrue(result.getId() > 0);
        assertThat(result.getName(), is(task.getName()));
        assertThat(result.getDependencies(), is(task.getDependencies()));
        assertThat(result.getDescription(), is(task.getDescription()));
        assertThat(result.getScheduleConf(), is(task.getScheduleConf()));
        assertThat(result.getTags(), is(task.getTags()));
    }

    @Test
    public void updateTask() {
        Task task = mockTask();
        mockPut("/tasks/1", JSONUtils.toJsonString(task),  JSONUtils.toJsonString(task));
        Task result = wfApi.updateTask(task.getId(), task);
        assertTrue(result.getId() > 0);
        assertThat(result.getName(), is(task.getName()));
        assertThat(result.getDependencies(), is(task.getDependencies()));
        assertThat(result.getDescription(), is(task.getDescription()));
        assertThat(result.getScheduleConf(), is(task.getScheduleConf()));
        assertThat(result.getTags(), is(task.getTags()));
    }

    @Test
    public void deleteTask() {
        mockDelete("/tasks/1", mockDeleteResponse());
        wfApi.deleteTask(1L);
    }

    @Test
    public void searchTasks() {
        Task task = mockTask();
        List<Task> tasks = Collections.singletonList(task);
        TaskSearchRequest request = TaskSearchRequest
                .newBuilder()
                .withName(task.getName())
                .withTags(task.getTags())
                .build();
        mockPost("/tasks/_search",
                JSONUtils.toJsonString(request),
                JSONUtils.toJsonString(new PaginationResult<>(1,0,1,  tasks)));

        PaginationResult<Task> result = wfApi.searchTasks(request);
        assertThat(result.getRecords().size(), is(1));
        Task taskRun1 = result.getRecords().get(0);
        assertTrue(taskRun1.getId() > 0);
    }

    @Test
    public void runTasks() {
        RunTaskRequest runTaskRequest = new RunTaskRequest();
        runTaskRequest.addTaskVariable(1L, Maps.newHashMap());
        mockPost("/tasks/_run", JSONUtils.toJsonString(runTaskRequest.getRunTasks()), "[1]");
        List<Long> result = wfApi.runTasks(runTaskRequest);
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(1L));
    }

    @Test
    public void getTaskRun() {
        TaskRun taskRun = mockTaskRun();
        mockGet("/taskruns/1" , JSONUtils.toJsonString(taskRun));
        TaskRun result = wfApi.getTaskRun(1L);
        assertTrue(result.getId() > 0);
        assertThat(result.getInlets(), sameBeanAs(taskRun.getInlets()));
        assertThat(result.getOutlets(), sameBeanAs(taskRun.getOutlets()));
        assertThat(result.getTask(), sameBeanAs(taskRun.getTask()));
        assertThat(result.getDependencyTaskRunIds(), is(taskRun.getDependencyTaskRunIds()));
        assertThat(result.getAttempts(), is(taskRun.getAttempts()));
        // JSON timestamp can only preserve millisecond-level precision
        assertThat(result.getEndAt(), is(atMillisecondPrecision(taskRun.getEndAt())));
        assertThat(result.getStartAt(), is(atMillisecondPrecision(taskRun.getStartAt())));
    }

    @Test
    public void getTaskRunStatus() {
        TaskRunState state = new TaskRunState(TaskRunStatus.CREATED);
        mockGet("/taskruns/1/status" , JSONUtils.toJsonString(state));
        TaskRunState result = wfApi.getTaskRunStatus(1L);
        assertThat(result.getStatus(), is(TaskRunStatus.CREATED));
    }

    @Test
    public void getTaskRunLog() {
        TaskRunLog runLog = new TaskRunLog();
        mockGet("/taskruns/1/logs" , JSONUtils.toJsonString(runLog));
        TaskRunLogRequest runLogRequest = TaskRunLogRequest.newBuilder()
                .withTaskRunId(1L)
                .build();
        TaskRunLog result = wfApi.getTaskRunLog(runLogRequest);
        assertThat(result, sameBeanAs(runLog));
    }

    @Test
    public void getTaskRuns() {
        TaskRun taskRun = mockTaskRun();
        List<TaskRun> taskRuns = Collections.singletonList(taskRun);
        TaskRunSearchRequest request = TaskRunSearchRequest
                .newBuilder()
                .withTaskRunIds(
                        Collections.singletonList(1L)
                )
                .build();
        mockGet("/taskruns?taskRunIds=1", JSONUtils.toJsonString(new PaginationResult<>(1,0,1, taskRuns)));

        PaginationResult<TaskRun> result = wfApi.getTaskRuns(request);
        assertThat(result.getRecords().size(), is(1));
        TaskRun taskRun1 = result.getRecords().get(0);
        assertTrue(taskRun1.getId() > 0);
    }

    @Test
    public void searchTaskRuns() {
        TaskRun taskRun = mockTaskRun();
        List<TaskRun> taskRuns = Collections.singletonList(taskRun);
        TaskRunSearchRequest request = TaskRunSearchRequest
                .newBuilder()
                .withTaskRunIds(
                        Collections.singletonList(1L)
                )
                .withDateFrom(taskRun.getStartAt())
                .withDateTo(taskRun.getEndAt())
                .build();
        mockPost("/taskruns/_search",
                JSONUtils.toJsonString(request),
                JSONUtils.toJsonString(new PaginationResult<>(1,0,1,  taskRuns)));

        PaginationResult<TaskRun> result = wfApi.searchTaskRuns(request);
        assertThat(result.getRecords().size(), is(1));
        TaskRun taskRun1 = result.getRecords().get(0);
        assertTrue(taskRun1.getId() > 0);
    }

    @Test
    public void getLatestTaskRuns_shouldWork() {
        Task task = mockTask();
        mockPost("/tasks", JSONUtils.toJsonString(task),  JSONUtils.toJsonString(task));

        TaskRun taskRun = mockTaskRun();
        List<TaskRun> taskRuns = Collections.singletonList(taskRun);
        Map<Long, List<TaskRun>> mockResult = new HashMap<>();
        mockResult.put(task.getId(), taskRuns);
        mockGet(
                "/taskruns/latest?taskIds=" + task.getId() + "&limit=10",
                JSONUtils.toJsonString(mockResult)
        );

        Map<Long, List<TaskRun>> result = wfApi.getLatestTaskRuns(Lists.newArrayList(task.getId()), 10);
        assertThat(result, sameBeanAs(mockResult));
    }
}
