package com.miotech.kun.workflow.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.exception.ExceptionResponse;
import com.miotech.kun.workflow.common.taskrun.factory.TaskRunLogVOFactory;
import com.miotech.kun.workflow.common.taskrun.factory.TaskRunStateVOFactory;
import com.miotech.kun.workflow.common.taskrun.filter.TaskRunSearchFilter;
import com.miotech.kun.workflow.common.taskrun.service.TaskRunService;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunLogVO;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunStateVO;
import com.miotech.kun.workflow.common.taskrun.vo.TaskRunVO;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.web.KunWebServerTestBase;
import com.miotech.kun.workflow.web.entity.PaginationVO;
import com.miotech.kun.workflow.web.serializer.JsonSerializer;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

public class TaskRunControllerTest extends KunWebServerTestBase {

    private final TaskRunService taskRunService = mock(TaskRunService.class);

    @Inject
    private JsonSerializer jsonSerializer;

    /**
     * A utility function to define search behaviors for taskrun service
     * @param filterToMatch
     * @param thenReturn
     */
    private void setupMockitoWithTaskRunSearchFilter(TaskRunSearchFilter filterToMatch, List<TaskRun> thenReturn) {
        Mockito.when(taskRunService.searchTaskRuns(ArgumentMatchers.eq(filterToMatch)))
                .thenReturn(thenReturn);
    }

    private PaginationVO<TaskRunVO> jsonToPaginationVO(String json) {
        return jsonSerializer.toObject(json, new TypeReference<PaginationVO<TaskRunVO>>() {});
    }

    private void prepareMockTaskRunsWithStatus() {
        List<TaskRun> allTaskRunCollection = new ArrayList<>();
        for (int i = 0; i < 200; i += 1) {
            // 100 CREATED, 50 RUNNING, 30 SUCCESS, 20 FAILED
            TaskRun taskRun = MockTaskRunFactory.createTaskRun();
            if (i < 100) {
                taskRun = taskRun.cloneBuilder().withStatus(TaskRunStatus.CREATED).build();
            } else if (i < 150) {
                taskRun = taskRun.cloneBuilder().withStatus(TaskRunStatus.RUNNING).build();
            } else if (i < 180) {
                taskRun = taskRun.cloneBuilder().withStatus(TaskRunStatus.SUCCESS).build();
            } else {
                taskRun = taskRun.cloneBuilder().withStatus(TaskRunStatus.FAILED).build();
            }
            allTaskRunCollection.add(taskRun);
        }
        TaskRunSearchFilter defaultPaginatedFilter = TaskRunSearchFilter.newBuilder()
                .withPageNum(1)
                .withPageSize(100)
                .build();
        setupMockitoWithTaskRunSearchFilter(defaultPaginatedFilter, allTaskRunCollection.subList(0, 100));
        // When matches running status filter
        TaskRunSearchFilter createdStatusFilter = defaultPaginatedFilter.cloneBuilder()
                .withStatus(TaskRunStatus.CREATED).build();
        setupMockitoWithTaskRunSearchFilter(
                createdStatusFilter,
                allTaskRunCollection.stream()
                        .filter(run -> Objects.equals(run.getStatus(), TaskRunStatus.CREATED))
                        .collect(Collectors.toList()));
        // When matches running status filter
        TaskRunSearchFilter runningStatusFilter = defaultPaginatedFilter.cloneBuilder()
                .withStatus(TaskRunStatus.RUNNING).build();
        setupMockitoWithTaskRunSearchFilter(
                runningStatusFilter,
                allTaskRunCollection.stream()
                        .filter(run -> Objects.equals(run.getStatus(), TaskRunStatus.RUNNING))
                        .collect(Collectors.toList()));
        // When matches success status filter
        TaskRunSearchFilter successStatusFilter = defaultPaginatedFilter.cloneBuilder()
                .withStatus(TaskRunStatus.SUCCESS).build();
        setupMockitoWithTaskRunSearchFilter(
                successStatusFilter,
                allTaskRunCollection.stream()
                        .filter(run -> Objects.equals(run.getStatus(), TaskRunStatus.SUCCESS))
                        .collect(Collectors.toList()));
    }

    private void prepareMockTaskRunsWithTimeRange() {
        List<TaskRun> allTaskRunCollection = new ArrayList<>();
        for (int i = 0; i < 100; i += 1) {
            OffsetDateTime startAt = OffsetDateTime.of(2020, ((i + 1) / 30) + 3, i % 30 + 1, 0, 0, 0, 0, ZoneOffset.ofHours(0));
            OffsetDateTime endAt = OffsetDateTime.of(2021, ((i + 1) / 30) + 3, i % 30 + 1, 0, 0, 0, 0, ZoneOffset.ofHours(0));
            TaskRun taskRun = MockTaskRunFactory.createTaskRun().cloneBuilder()
                    .withStatus(TaskRunStatus.RUNNING)
                    .withStartAt(startAt)
                    .withEndAt(endAt)
                    .build();
            allTaskRunCollection.add(taskRun);
        }
        TaskRunSearchFilter defaultPaginatedFilter = TaskRunSearchFilter.newBuilder()
                .withPageNum(1)
                .withPageSize(100)
                .build();
        // setup return behavior on filtering offsetDatetime
        OffsetDateTime timePointMarch10th = OffsetDateTime.of(2020, 3, 10, 0, 0, 0, 0, ZoneOffset.ofHours(0));
        TaskRunSearchFilter filterDateFromMarch10th = defaultPaginatedFilter.cloneBuilder()
                .withDateFrom(timePointMarch10th)
                .build();
        setupMockitoWithTaskRunSearchFilter(
                filterDateFromMarch10th,
                allTaskRunCollection.stream()
                        .filter(run -> run.getStartAt().isAfter(timePointMarch10th))
                        .collect(Collectors.toList()));

        OffsetDateTime timePointApril10th = OffsetDateTime.of(2021, 4, 10, 0, 0, 0, 0, ZoneOffset.ofHours(0));
        TaskRunSearchFilter filterDateToApril10th = defaultPaginatedFilter.cloneBuilder()
                .withDateFrom(timePointMarch10th)
                .withDateTo(timePointApril10th)
                .build();
        setupMockitoWithTaskRunSearchFilter(
                filterDateToApril10th,
                allTaskRunCollection.stream()
                        .filter(run -> run.getStartAt().isAfter(timePointMarch10th) && run.getEndAt().isBefore(timePointApril10th))
                        .collect(Collectors.toList()));
    }

    private static final class IsTaskRunSearchFilterWithTags implements ArgumentMatcher<TaskRunSearchFilter> {
        @Override
        public boolean matches(TaskRunSearchFilter argument) {
            return Objects.nonNull(argument) &&
                    Objects.nonNull(argument.getTags());
        }
    }

    private static List<TaskRun> mockFilterTaskRuns(List<TaskRun> allTaskRunCollection, TaskRunSearchFilter filter) {
        return allTaskRunCollection.stream()
                .filter(taskRun -> {
                    if (Objects.isNull(filter.getTags()) || (filter.getTags().isEmpty())) {
                        return true;
                    }
                    // else
                    List<Tag> tags = filter.getTags();
                    if (taskRun.getTask().getTags().size() != tags.size()) {
                        return false;
                    }
                    for (int i = 0; i < tags.size(); ++i) {
                        if (!Objects.equals(taskRun.getTask().getTags().get(i), tags.get(i))) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private void prepareMockTaskRunsWithTags() {
        List<TaskRun> allTaskRunCollection = new ArrayList<>();
        for (int i = 0; i < 200; i += 1) {
            // 100 records with tag: "version" = "1.0", 50 with "version" = "2.0"
            TaskRun taskRun = MockTaskRunFactory.createTaskRun();
            if (i < 100) {
                taskRun = taskRun.cloneBuilder().withTask(taskRun.getTask().cloneBuilder()
                        .withTags(Lists.newArrayList(
                                new Tag("version", "1.0")
                        )).build())
                        .build();
            } else if (i < 150) {
                taskRun = taskRun.cloneBuilder().withTask(taskRun.getTask().cloneBuilder()
                        .withTags(Lists.newArrayList(
                                new Tag("version", "2.0")
                        )).build())
                        .build();
            }
            allTaskRunCollection.add(taskRun);
        }

        Mockito.doAnswer(invocation -> {
            TaskRunSearchFilter filter = invocation.getArgument(0);
            return mockFilterTaskRuns(allTaskRunCollection, filter);
        }).when(taskRunService).searchTaskRuns(argThat(new IsTaskRunSearchFilterWithTags()));
    }

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

    @Test
    public void searchTaskRuns_withEmptyQueryParameter_shouldReturnDefaultPaginatedListOfTaskRuns() {
        // Prepare
        TaskRunSearchFilter emptyFilter = TaskRunSearchFilter.newBuilder().build();
        TaskRunSearchFilter defaultPaginatedFilter = TaskRunSearchFilter.newBuilder()
                .withPageNum(1)
                .withPageSize(100)
                .build();
        List<TaskRun> allTaskRunCollection = new ArrayList<>();
        for (int i = 0; i < 200; i += 1) {
            allTaskRunCollection.add(MockTaskRunFactory.createTaskRun());
        }
        // By default, an empty search filter will return all task runs
        setupMockitoWithTaskRunSearchFilter(emptyFilter, allTaskRunCollection);
        // Our REST API should provide an default pagination configuration with page size = 100
        setupMockitoWithTaskRunSearchFilter(defaultPaginatedFilter, allTaskRunCollection.subList(0, 100));
        Mockito.doReturn(200).when(taskRunService).fetchTotalCount(eq(defaultPaginatedFilter));

        // Process
        String response = get("/taskruns");
        PaginationVO<TaskRunVO> responseTaskRunResults = jsonToPaginationVO(response);

        // Validate
        assertThat(responseTaskRunResults.getRecords().size(), is(100));
        assertThat(responseTaskRunResults.getTotalCount(), is(200));
    }

    @Test
    public void getTaskRuns_withStatusFilter_shouldReturnFilteredResults() {
        // Prepare
        prepareMockTaskRunsWithStatus();

        // Process
        String responseFilterByStatusCreated = get("/taskruns?status=CREATED");
        String responseFilterByStatusRunning = get("/taskruns?status=RUNNING");
        String responseFilterByStatusSuccess = get("/taskruns?status=SUCCESS");

        // Validate
        PaginationVO<TaskRunVO> taskRunListFilterByCreatedStatus = jsonToPaginationVO(responseFilterByStatusCreated);
        PaginationVO<TaskRunVO> taskRunListFilterByRunningStatus = jsonToPaginationVO(responseFilterByStatusRunning);
        PaginationVO<TaskRunVO> taskRunListFilterBySuccessStatus = jsonToPaginationVO(responseFilterByStatusSuccess);

        assertThat(taskRunListFilterByCreatedStatus.getRecords().size(), is(100));
        assertThat(taskRunListFilterByRunningStatus.getRecords().size(), is(50));
        assertThat(taskRunListFilterBySuccessStatus.getRecords().size(), is(30));
    }

    @Test
    public void getTaskRuns_withInvalidStatusFilter_shouldReturnBadRequest() {
        // Prepare
        prepareMockTaskRunsWithStatus();

        // Process
        String responseFilterByInvalidStatus = get("/taskruns?status=THIS_IS_INVALID");

        // Validation
        ExceptionResponse exceptionResponse = jsonSerializer.toObject(responseFilterByInvalidStatus, ExceptionResponse.class);
        assertThat(exceptionResponse.getCode(), is(400));
    }

    @Test
    public void getTaskRuns_withDateRangeFilters_shouldReturnFilteredResultsProperly() {
        // Prepare
        prepareMockTaskRunsWithTimeRange();

        // Process
        String responseWithStartTimeRange = get("/taskruns?dateFrom=2020-03-10T00:00:00%2B00:00");
        String responseWithStartTimeRangeAndEndRange = get("/taskruns?dateFrom=2020-03-10T00:00:00%2B00:00&dateTo=2021-04-10T00:00:00%2B00:00");

        PaginationVO<TaskRunVO> taskRunListFilterByStartTimeRange = jsonToPaginationVO(responseWithStartTimeRange);
        PaginationVO<TaskRunVO> taskRunListFilterByStartTimeRangeAndEndRange = jsonToPaginationVO(responseWithStartTimeRangeAndEndRange);

        // Validation
        assertThat(taskRunListFilterByStartTimeRange.getRecords().size(), is(90));
        assertThat(taskRunListFilterByStartTimeRangeAndEndRange.getRecords().size(), is(28));
    }

    @Test
    public void getTaskRuns_withInvalidDateRangeFilters_shouldReturnBadRequest() {
        // Prepare
        prepareMockTaskRunsWithTimeRange();

        // Process
        String responseWithBadStartTimeRange = get("/taskruns?dateFrom=2020-03-10T00:00:00");

        // Validate
        ExceptionResponse exceptionResponse = jsonSerializer.toObject(responseWithBadStartTimeRange, ExceptionResponse.class);
        assertThat(exceptionResponse.getCode(), is(400));
    }

    @Test
    public void searchTaskRuns_withTags_shouldResponseFilteredResults() {
        // Prepare
        prepareMockTaskRunsWithTags();

        // Process
        String responseWithTagsVersionOne = post("/taskruns/_search", "{\"tags\": [{\"key\": \"version\", \"value\": \"1.0\"}]}");
        String responseWithTagsVersionTwo = post("/taskruns/_search", "{\"tags\": [{\"key\": \"version\", \"value\": \"2.0\"}]}");
        String responseWithTagsVersionThree = post("/taskruns/_search", "{\"tags\": [{\"key\": \"version\", \"value\": \"3.0\"}]}");

        PaginationVO<TaskRunVO> taskRunListFilterByTagVersionOne = jsonToPaginationVO(responseWithTagsVersionOne);
        PaginationVO<TaskRunVO> taskRunListFilterByTagVersionTwo = jsonToPaginationVO(responseWithTagsVersionTwo);
        PaginationVO<TaskRunVO> taskRunListFilterByTagVersionThree = jsonToPaginationVO(responseWithTagsVersionThree);

        // Validate
        assertThat(taskRunListFilterByTagVersionOne.getRecords().size(), is(100));
        assertThat(taskRunListFilterByTagVersionTwo.getRecords().size(), is(50));
        assertThat(taskRunListFilterByTagVersionThree.getRecords().size(), is(0));
    }
}
