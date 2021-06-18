package com.miotech.kun.workflow.common.taskrun.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.filter.TaskRunSearchFilter;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TaskRunDaoTest extends DatabaseTestBase {
    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskDao taskDao;


    private Clock getMockClock() {
        return Clock.fixed(Instant.parse("2020-01-01T00:00:00.00Z"), ZoneId.systemDefault());
    }

    private List<TaskRun> prepareTaskRunsWithDependencyRelations() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4, "0>>1;0>>2;1>>3;2>>3");
        for (Task task : taskList) {
            taskDao.create(task);
        }
        return prepareTaskRunsWithDependencyRelations(taskList);
    }

    private List<TaskRun> prepareTaskRunsWithDependencyRelations(List<Task> taskList) {
        TaskRun taskRunA = MockTaskRunFactory.createTaskRun(1L, taskList.get(0))
                .cloneBuilder()
                .withDependentTaskRunIds(new ArrayList<>())
                .withStartAt(DateTimeUtils.now().plusHours(1))
                .withEndAt(DateTimeUtils.now().plusHours(2))
                .withStatus(TaskRunStatus.SUCCESS)
                .build();
        TaskRun taskRunB = MockTaskRunFactory.createTaskRun(2L, taskList.get(1))
                .cloneBuilder()
                .withDependentTaskRunIds(Lists.newArrayList(1L))
                .withStartAt(DateTimeUtils.now().plusHours(3))
                .withEndAt(null)
                .withStatus(TaskRunStatus.RUNNING)
                .build();
        TaskRun taskRunC = MockTaskRunFactory.createTaskRun(3L, taskList.get(2))
                .cloneBuilder()
                .withDependentTaskRunIds(Lists.newArrayList(1L))
                .withStartAt(DateTimeUtils.now().plusHours(4))
                .withEndAt(null)
                .withStatus(TaskRunStatus.RUNNING)
                .build();
        TaskRun taskRunD = MockTaskRunFactory.createTaskRun(4L, taskList.get(3))
                .cloneBuilder()
                .withDependentTaskRunIds(Lists.newArrayList(2L, 3L))
                .withStartAt(null)
                .withEndAt(null)
                .withStatus(TaskRunStatus.CREATED)
                .build();
        taskRunDao.createTaskRun(taskRunA);
        taskRunDao.createTaskRun(taskRunB);
        taskRunDao.createTaskRun(taskRunC);
        taskRunDao.createTaskRun(taskRunD);

        return Lists.newArrayList(taskRunA, taskRunB, taskRunC, taskRunD);
    }

    @After
    public void resetGlobalClock() {
        // Reset global clock after each test
        DateTimeUtils.resetClock();
    }

    @Test
    public void createTaskRun_withValidProperties_shouldSuccess() {
        // Prepare
        Clock mockClock = getMockClock();
        DateTimeUtils.setClock(mockClock);
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");

        for (Task task : taskList) {
            taskDao.create(task);
        }
        TaskRun taskRunA = MockTaskRunFactory.createTaskRun(1L, taskList.get(0))
                .cloneBuilder()
                .withDependentTaskRunIds(new ArrayList<>())
                .withStartAt(DateTimeUtils.now().plusHours(-1))
                .withEndAt(DateTimeUtils.now().plusHours(-1))
                .withStatus(TaskRunStatus.SUCCESS)
                .build();

        taskRunDao.createTaskRun(taskRunA);

        TaskRun sampleTaskRun = MockTaskRunFactory.createTaskRun(2L, taskList.get(1))
                .cloneBuilder()
                .withDependentTaskRunIds(Lists.newArrayList(Long.valueOf(1L)))
                .build();

        // Process
        taskRunDao.createTaskRun(sampleTaskRun);

        // Validate
        Optional<TaskRun> persistedTaskRunOptional = taskRunDao.fetchTaskRunById(2L);
        assertTrue(persistedTaskRunOptional.isPresent());

        TaskRun persistedTaskRun = persistedTaskRunOptional.get();
        assertThat(persistedTaskRun.getId(), is(sampleTaskRun.getId()));
        assertThat(persistedTaskRun.getDependentTaskRunIds(),is(sampleTaskRun.getDependentTaskRunIds()));
        assertThat(persistedTaskRun.getTask().getId(),is(sampleTaskRun.getTask().getId()));
    }

    @Test
    public void createTaskRun_withInvalidProperties_shouldThrowException() {
        // Prepare
        try {
            TaskRun sampleTaskRun = MockTaskRunFactory.createTaskRun(null);
            taskRunDao.createTaskRun(sampleTaskRun);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }
    }

    @Test
    public void fetchLatestTaskAttempt_withArrayOfTaskIds_shouldReturnAttemptVOInCorrectOrder() {
        // Prepare
        Clock mockClock = getMockClock();
        DateTimeUtils.setClock(mockClock);

        // 1. create task runs
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);

        TaskRun[] sampleTaskRuns = {
                MockTaskRunFactory.createTaskRun(1L, task),
                MockTaskRunFactory.createTaskRun(2L, task),
                MockTaskRunFactory.createTaskRun(3L, task)
        };

        taskRunDao.createTaskRun(sampleTaskRuns[0]);
        taskRunDao.createTaskRun(sampleTaskRuns[1]);
        taskRunDao.createTaskRun(sampleTaskRuns[2]);

        // 2. create run attempts (12 attempts in total, 4 attempts each run)
        for (int i = 0; i < 12; i += 1) {
            TaskAttempt attempt = MockTaskRunFactory.createTaskAttempt((long) i + 1, sampleTaskRuns[i / 4], (i % 4) + 1);
            taskRunDao.createAttempt(attempt);
        }

        // Process
        List<Long> queryIds = Lists.newArrayList(2L, 3L, 1L);
        List<TaskAttemptProps> propsList = taskRunDao.fetchLatestTaskAttempt(queryIds);

        // Validate
        assertThat(propsList.size(), is(3));
        for (int i = 0; i < 3; i += 1) {
            // should return latest attempt
            assertThat(propsList.get(i).getAttempt(), is(4));
        }
        // should return in given order as query
        assertThat(propsList.get(0).getTaskRunId(), is(2L));
        assertThat(propsList.get(1).getTaskRunId(), is(3L));
        assertThat(propsList.get(2).getTaskRunId(), is(1L));
    }

    @Test
    public void updateTaskRun_withValidObject_shouldSuccess() {
        // Prepare
        Clock mockClock = getMockClock();
        DateTimeUtils.setClock(mockClock);

        // 1. create task runs
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);

        // 2. create task run
        TaskRun sampleTaskRun = MockTaskRunFactory.createTaskRun(1L, task);
        taskRunDao.createTaskRun(sampleTaskRun);

        // Process
        // 3. Update task run
        TaskRun taskRunWithUpdatedProps = sampleTaskRun.cloneBuilder()
                .withStartAt(DateTimeUtils.now().plusHours(1))
                .withStatus(TaskRunStatus.ABORTED)
                .withCreatedAt(OffsetDateTime.now())
                .build();
        taskRunDao.updateTaskRun(taskRunWithUpdatedProps);

        // Validate
        // 4. fetch and validate
        Optional<TaskRun> persistedTaskRunOptional = taskRunDao.fetchTaskRunById(1L);
        assertTrue(persistedTaskRunOptional.isPresent());
        TaskRun persistedTaskRun = persistedTaskRunOptional.get();
        assertThat(persistedTaskRun, sameBeanAs(taskRunWithUpdatedProps).ignoring(startsWith("createdAt")).ignoring(startsWith("updatedAt")));
        // Here startAt & endAt may differ since database converts datetime offset to system default,
        // but epoch second will guaranteed to be the same
        assertEquals(persistedTaskRun.getStartAt().toEpochSecond(), taskRunWithUpdatedProps.getStartAt().toEpochSecond());
    }

    @Test
    public void deleteTaskRun_byExistingId_shouldReturnRemovedRowNum() {
        // Prepare
        Clock mockClock = getMockClock();
        DateTimeUtils.setClock(mockClock);

        // 1. create task runs
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);

        // 2. create task run
        TaskRun sampleTaskRun = MockTaskRunFactory.createTaskRun(1L, task);
        taskRunDao.createTaskRun(sampleTaskRun);

        // Process
        // 3. Perform delete action
        boolean deletionSuccess = taskRunDao.deleteTaskRun(1L);

        // Validate
        assertTrue(deletionSuccess);

        // 4. fetch and validate
        Optional<TaskRun> persistedTaskRunOptional = taskRunDao.fetchTaskRunById(1L);
        assertFalse(persistedTaskRunOptional.isPresent());

        // 5. Multiple deletions on same id should be idempotent, but returns false flag
        boolean deletionSuccessAfterAction = taskRunDao.deleteTaskRun(1L);
        assertFalse(deletionSuccessAfterAction);
    }

    @Test
    public void fetchLatestTaskRun_withValidTaskId_shouldReturnLatestRun() {
        // Prepare
        // 1. create task runs
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);

        DateTimeUtils.setClock(Clock.fixed(Instant.parse("2020-01-01T00:00:01.00Z"), ZoneId.of("UTC")));
        TaskRun taskrun1 = MockTaskRunFactory.createTaskRun(1L, task);

        DateTimeUtils.setClock(Clock.fixed(Instant.parse("2020-01-01T00:00:02.00Z"), ZoneId.of("UTC")));
        TaskRun taskrun2 = MockTaskRunFactory.createTaskRun(2L, task);

        DateTimeUtils.setClock(Clock.fixed(Instant.parse("2020-01-01T00:00:03.00Z"), ZoneId.of("UTC")));
        TaskRun taskrun3 = MockTaskRunFactory.createTaskRun(3L, task);

        List<TaskRun> sampleTaskRuns = Lists.newArrayList(taskrun1, taskrun2, taskrun3);

        taskRunDao.createTaskRuns(sampleTaskRuns);

        // Process
        TaskRun latestTaskRun = taskRunDao.fetchLatestTaskRun(task.getId());

        assertThat(latestTaskRun.getId(), is(3L));
    }

    @Test
    public void fetchAttempts_ByIdOrList_shouldReturnListOfAttempts() {
        // Prepare
        Clock mockClock = getMockClock();
        DateTimeUtils.setClock(mockClock);

        // 1. create sample task run
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);

        TaskRun sampleTaskRun = MockTaskRunFactory.createTaskRun(1L, task);
        taskRunDao.createTaskRun(sampleTaskRun);

        // 2. create 4 run attempts
        for (int i = 1; i <= 4; i += 1) {
            TaskAttempt attempt = MockTaskRunFactory.createTaskAttempt((long) i, sampleTaskRun, i);
            taskRunDao.createAttempt(attempt);
        }

        // Process
        // 3. fetch list
        List<TaskAttemptProps> attempts = taskRunDao.fetchAttemptsPropByTaskRunId(sampleTaskRun.getId());
        Optional<TaskAttempt> attemptOptional = taskRunDao.fetchAttemptById(3L);

        // Validate
        assertThat(attempts.size(), is(4));

        assertTrue(attemptOptional.isPresent());
        TaskAttempt attempt = attemptOptional.get();
        TaskAttempt baselineModel = MockTaskRunFactory.createTaskAttempt(3L, sampleTaskRun, 3);
        assertThat(attempt, samePropertyValuesAs(baselineModel, "startAt", "endAt", "taskRun"));
        // TaskRun instance should be nested inside
        assertThat(attempt.getTaskRun(), notNullValue());
        assertThat(attempt.getTaskRun(), sameBeanAs(sampleTaskRun)
                .ignoring(startsWith("createdAt"))
                .ignoring(startsWith("updatedAt"))
        );
        // And Task model object should be nested inside that TaskRun object
        assertThat(attempt.getTaskRun().getTask(), notNullValue());
        assertThat(attempt.getTaskRun().getTask(), sameBeanAs(task));
    }

    public void fetchTaskAttemptStatus_ok() {
        // prepare
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt();
        TaskRun taskRun = taskAttempt.getTaskRun();
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(taskAttempt);
        taskDao.create(taskRun.getTask());

        // process
        Optional<TaskRunStatus> result = taskRunDao.fetchTaskAttemptStatus(taskAttempt.getId());

        // verify
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(taskAttempt.getStatus()));
    }

    @Test
    public void fetchTaskAttemptStatus_not_found() {
        // process
        Optional<TaskRunStatus> result = taskRunDao.fetchTaskAttemptStatus(-1L);

        // verify
        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void updateTaskAttemptLogPath() {
        // prepare
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt();
        TaskRun taskRun = taskAttempt.getTaskRun();
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(taskAttempt);
        taskDao.create(taskRun.getTask());

        // process
        String logPath = "file:/path";
        taskRunDao.updateTaskAttemptLogPath(taskAttempt.getId(), logPath);

        // verify
        TaskAttempt result = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();
        assertThat(result.getLogPath(), is(logPath));
    }

    @Test
    public void updateTaskAttemptStatus_status_only() {
        // prepare
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt();
        TaskRun taskRun = taskAttempt.getTaskRun();
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(taskAttempt);
        taskDao.create(taskRun.getTask());

        TaskRun runRec = taskRunDao.fetchTaskRunById(taskRun.getId()).get();
        TaskAttemptProps attemptRec = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());

        assertThat(runRec.getStatus(), is(nullValue()));
        assertThat(runRec.getStartAt(), is(nullValue()));
        assertThat(runRec.getEndAt(), is(nullValue()));

        assertThat(attemptRec.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(attemptRec.getStartAt(), is(nullValue()));
        assertThat(attemptRec.getEndAt(), is(nullValue()));

        // process
        TaskRunStatus prev = taskRunDao.updateTaskAttemptStatus(taskAttempt.getId(), TaskRunStatus.RUNNING).get();

        // verify
        runRec = taskRunDao.fetchTaskRunById(taskRun.getId()).get();
        attemptRec = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());

        assertThat(prev, is(TaskRunStatus.CREATED));
        assertThat(runRec.getStatus(), is(TaskRunStatus.RUNNING));
        assertThat(attemptRec.getStatus(), is(TaskRunStatus.RUNNING));
    }

    @Test
    public void updateTaskAttemptStatus_status_with_both_start_at_and_end_at() {
        // prepare
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt();
        TaskRun taskRun = taskAttempt.getTaskRun();
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(taskAttempt);
        taskDao.create(taskRun.getTask());

        TaskRun runRec = taskRunDao.fetchTaskRunById(taskRun.getId()).get();
        TaskAttemptProps attemptRec = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());

        assertThat(runRec.getStatus(), is(nullValue()));
        assertThat(runRec.getStartAt(), is(nullValue()));
        assertThat(runRec.getEndAt(), is(nullValue()));

        assertThat(attemptRec.getStatus(), is(TaskRunStatus.CREATED));
        assertThat(attemptRec.getStartAt(), is(nullValue()));
        assertThat(attemptRec.getEndAt(), is(nullValue()));

        // process
        OffsetDateTime startAt = OffsetDateTime.of(2020, 5, 1, 0, 0, 0, 0, DateTimeUtils.systemDefaultOffset());
        OffsetDateTime endAt = OffsetDateTime.of(2020, 5, 1, 12, 0, 0, 0, DateTimeUtils.systemDefaultOffset());
        TaskRunStatus prev = taskRunDao.updateTaskAttemptStatus(taskAttempt.getId(),
                TaskRunStatus.RUNNING, startAt, endAt).get();

        // verify
        runRec = taskRunDao.fetchTaskRunById(taskRun.getId()).get();
        attemptRec = taskRunDao.fetchLatestTaskAttempt(taskRun.getId());

        assertThat(prev, is(TaskRunStatus.CREATED));

        assertThat(runRec.getStatus(), is(TaskRunStatus.RUNNING));
        assertThat(runRec.getStartAt(), is(startAt));
        assertThat(runRec.getEndAt(), is(endAt));

        assertThat(attemptRec.getStatus(), is(TaskRunStatus.RUNNING));
        assertThat(attemptRec.getStartAt(), is(startAt));
        assertThat(attemptRec.getEndAt(), is(endAt));
    }

    @Test
    public void fetchUpstreamAndDownstreamTaskRuns_WithDistance_shouldWork() {
        // Prepare
        // 1. create task runs
        prepareTaskRunsWithDependencyRelations();

        // Process
        // 2. Fetch upstream tasks of task run D
        List<TaskRun> upstreamRunsOfD = taskRunDao.fetchUpstreamTaskRunsById(4L, 1, false);
        List<TaskRun> allUpstreamRunsOfD = taskRunDao.fetchUpstreamTaskRunsById(4L, 10, false);
        List<TaskRun> allUpstreamRunsOfDIncludeItself = taskRunDao.fetchUpstreamTaskRunsById(4L, 10, true);
        List<TaskRun> downstreamRunsOfD = taskRunDao.fetchDownstreamTaskRunsById(4L, 100, false);

        // Validate
        assertThat(upstreamRunsOfD.size(), is(2));
        assertThat(allUpstreamRunsOfD.size(), is(3));
        assertThat(allUpstreamRunsOfDIncludeItself.size(), is(4));
        assertThat(downstreamRunsOfD.size(), is(0));
    }

    @Test
    public void fetchUpstreamAndDownstreamTaskRuns_onInvalidCases_shouldThrowExceptions() {
        // Prepare
        // 1. create task runs
        prepareTaskRunsWithDependencyRelations();

        try {
            // 2. distance should be positive
            taskRunDao.fetchUpstreamTaskRunsById(4L, 0, false);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }

        try {
            // 3. source task run id should exists
            taskRunDao.fetchUpstreamTaskRunsById(5L, 1, false);
        } catch (Exception e) {
            assertThat(e, instanceOf(EntityNotFoundException.class));
        }
    }

    @Test
    public void fetchTaskRunsByFilter_withEmptyFilter_shouldReturnTaskRuns() {
        // prepare
        prepareTaskRunsWithDependencyRelations();

        // process
        List<TaskRun> allTaskRuns = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter.newBuilder().build());

        // validate
        assertThat(allTaskRuns.size(), is(4));
    }

    @Test
    @Ignore
    // This test case is no longer effective since we have changed the indicator to create time
    public void fetchTaskRunsByFilter_withDateRangeFilter_shouldReturnFilterTaskRuns() {
        // prepare
        DateTimeUtils.setClock(getMockClock());
        prepareTaskRunsWithDependencyRelations();

        // process
        List<TaskRun> runsStarted2HoursLater = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withDateFrom(DateTimeUtils.now().plusHours(2))
                .build());

        List<TaskRun> runsEnded2HoursLater = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withDateTo(DateTimeUtils.now().plusHours(2))
                .build());

        List<TaskRun> runsWithinDateRange = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withDateFrom(DateTimeUtils.now().plusHours(1))
                .withDateTo(DateTimeUtils.now().plusHours(6))
                .build());

        // validate
        assertThat(runsStarted2HoursLater.size(), is(2));
        assertThat(runsEnded2HoursLater.size(), is(1));
        assertThat(runsWithinDateRange.size(), is(1));
    }

    @Test
    public void fetchTaskRunsByFilter_withStatusFilter_shouldReturnFilteredTaskRuns() {
        // prepare
        DateTimeUtils.setClock(getMockClock());
        prepareTaskRunsWithDependencyRelations();

        // process
        List<TaskRun> runsWithRunningStatus = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withStatus(Sets.newHashSet(TaskRunStatus.RUNNING))
                .build());
        List<TaskRun> runsWithFailedStatus = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withStatus(Sets.newHashSet(TaskRunStatus.FAILED))
                .build());

        // validate
        assertThat(runsWithRunningStatus.size(), is(2));
        assertThat(runsWithFailedStatus.size(), is(0));
    }

    @Test
    public void fetchTaskRunsByFilter_withIdsFilter_shouldReturnFilteredTaskRuns() {
        // Prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4, "0>>1;0>>2;1>>3;2>>3");
        for (Task task : taskList) {
            taskDao.create(task);
        }
        prepareTaskRunsWithDependencyRelations(taskList);

        // Process
        List<TaskRun> filteredTaskRuns = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withTaskIds(taskList.stream().map(Task::getId).collect(Collectors.toList()))
                .build());

        List<TaskRun> filteredTaskRunsWithEmptyTaskIds = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withTaskIds(Lists.newArrayList())
                .build());

        List<TaskRun> filteredTaskRunsWithNonExistTaskIds = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withTaskIds(Lists.newArrayList(1234L, 2345L))
                .build());

        // Validate
        assertThat(filteredTaskRuns.size(), is(4));
        // if no task id is in the filter, perform full match query
        assertThat(filteredTaskRunsWithEmptyTaskIds.size(), is(4));
        assertThat(filteredTaskRunsWithNonExistTaskIds.size(), is(0));
    }

    @Test
    public void fetchTaskRunsByFilter_withTaskTags_shouldReturnFilteredTaskRuns() {

        // Prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4, "0>>1;0>>2;1>>3;2>>3");
        List<Task> tagTaskList = new ArrayList<>();
        for (Task task : taskList) {
            // Prepare
            Task tagTask = task
                    .cloneBuilder()
                    .withTags(Lists.newArrayList(
                            new Tag("version", "1.0"),
                            new Tag("owner", "foo")
                    )).build();
            taskDao.create(tagTask);
            tagTaskList.add(tagTask);
        }
        prepareTaskRunsWithDependencyRelations(tagTaskList);

        List<TaskRun> filteredTaskRunsWithSingleTag = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withTags(Lists.newArrayList(
                        new Tag("version", "1.0")
                ))
                .build());

        List<TaskRun> filteredTaskRunsWithMultiTags = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withTags(Lists.newArrayList(
                        new Tag("version", "1.0"),
                        new Tag("owner", "foo")
                ))
                .build());

        List<TaskRun> filteredTaskRunsWithNonExistMultiTags = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withTags(Lists.newArrayList(
                        new Tag("version", "1.0"),
                        new Tag("owner", "bar")
                ))
                .build());

        // Validate
        assertThat(filteredTaskRunsWithSingleTag.size(), is(4));
        assertThat(filteredTaskRunsWithMultiTags.size(), is(4));
        assertThat(filteredTaskRunsWithNonExistMultiTags.size(), is(0));
    }

    @Test
    public void fetchTaskRunsByFilter_withSorter_shouldSortAsExpected() {
        // Prepare

        // Prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4, "0>>1;0>>2;1>>3;2>>3");
        List<Task> tagTaskList = new ArrayList<>();
        for (Task task : taskList) {
            // Prepare
            Task tagTask = task
                    .cloneBuilder()
                    .withTags(Lists.newArrayList(
                            new Tag("version", "1.0"),
                            new Tag("owner", "foo")
                    )).build();
            taskDao.create(tagTask);
            tagTaskList.add(tagTask);
        }

        prepareTaskRunsWithDependencyRelations(tagTaskList);


        // Process
        List<TaskRun> filteredTaskRunsWithStartTimeSorter = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withSortKey("startAt")
                .withSortOrder("ASC")
                .build());

        List<TaskRun> filteredTaskRunsWithIdSorter = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter.newBuilder()
                .withSortKey("id")
                .withSortOrder("DESC")
                .build());

        // Validate
        assertArrayEquals(
                new Long[]{4L, 1L, 2L, 3L},
                filteredTaskRunsWithStartTimeSorter.stream().map(TaskRun::getId).toArray());
        assertArrayEquals(
                new Long[]{4L, 3L, 2L, 1L},
                filteredTaskRunsWithIdSorter.stream().map(TaskRun::getId).toArray());
    }

    @Test
    public void fetchTaskRunsByFilter_withIncludeStartedOnlyFlag_shouldFilterOutNonStarted() {

        // Prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4, "0>>1;0>>2;1>>3;2>>3");
        List<Task> tagTaskList = new ArrayList<>();
        for (Task task : taskList) {
            // Prepare
            Task tagTask = task
                    .cloneBuilder()
                    .withTags(Lists.newArrayList(
                            new Tag("version", "1.0"),
                            new Tag("owner", "foo")
                    )).build();
            taskDao.create(tagTask);
            tagTaskList.add(tagTask);
        }

        prepareTaskRunsWithDependencyRelations(tagTaskList);


        // Process
        List<TaskRun> filteredTaskRunsWithIncludeStartedOnlyFlag =
                taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                        .newBuilder()
                        .withIncludeStartedOnly(true)
                        .withSortKey("id")
                        .withSortOrder("ASC")
                        .build());

        // Validate
        assertArrayEquals(
                new Long[]{1L, 2L, 3L},
                filteredTaskRunsWithIncludeStartedOnlyFlag.stream().map(TaskRun::getId).toArray());
    }


    @Test
    public void fetchTaskRunByTaskAndTick() {
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        taskRunDao.createTaskRuns(Arrays.asList(taskRun));
        Tick tick = new Tick(OffsetDateTime.of(
                2020, 5, 1, 0, 0, 0, 0, ZoneOffset.of("+08:00")
        ));
        TaskRun taskRunSaved = taskRunDao.fetchTaskRunByTaskAndTick(task.getId(), tick);
        assertEquals(taskRun.getId(), taskRunSaved.getId());

    }

    @Test
    public void fetchUnStartedTaskRunList() {
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        taskRunDao.createTaskRuns(Arrays.asList(taskRun));
        List<Long> taskRunIdList = taskRunDao.fetchTaskRunListWithoutAttempt()
                .stream().map(TaskRun::getId).collect(Collectors.toList());
        assertEquals(taskRun.getId(), taskRunIdList.get(0));

    }

    @Test
    public void fetchOldSatisfyTaskRunId() {
        Tick tick = new Tick(DateTimeUtils.now());
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);
        TaskRun newTaskRun = MockTaskRunFactory.createTaskRunWithTick(task, tick);
        TaskRun oldTaskRun = MockTaskRunFactory.createTaskRun();
        taskRunDao.createTaskRuns(Arrays.asList(newTaskRun, oldTaskRun));
        List<Long> taskRunIds = taskRunDao.fetchAllSatisfyTaskRunId();
        assertThat(taskRunIds, hasSize(1));
        assertThat(taskRunIds.get(0), is(newTaskRun.getId()));
    }

    @Test
    public void fetchUnStartedTaskRunListWithDependency() {
        Tick tick = new Tick(DateTimeUtils.now());

        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelationsAndTick(taskList, "0>>1", tick);
        for (Task task : taskList) {
            taskDao.create(task);
        }
        taskRunDao.createTaskRuns(taskRunList);
        List<TaskRun> recoverTaskRunList = taskRunDao.fetchTaskRunListWithoutAttempt();
        TaskRun taskRun1 = recoverTaskRunList.get(0);
        TaskRun taskRun2 = recoverTaskRunList.get(1);
        assertThat(taskRun1.getDependentTaskRunIds(), Matchers.hasSize(0));
        assertThat(taskRun2.getDependentTaskRunIds(), Matchers.hasSize(1));
        assertThat(taskRun2.getDependentTaskRunIds(), Matchers.containsInAnyOrder(taskRun1.getId()));

    }
}
