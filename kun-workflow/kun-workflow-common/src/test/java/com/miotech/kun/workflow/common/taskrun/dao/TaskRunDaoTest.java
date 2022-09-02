package com.miotech.kun.workflow.common.taskrun.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.bo.TaskRunProps;
import com.miotech.kun.workflow.common.taskrun.filter.TaskRunSearchFilter;
import com.miotech.kun.workflow.core.model.common.Condition;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.BlockType;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.*;
import com.miotech.kun.workflow.testing.factory.MockDataStoreFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TaskRunDaoTest extends DatabaseTestBase {
    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskDao taskDao;


    private Clock getMockClock() {
        return Clock.fixed(Instant.parse("2020-01-01T00:00:00.00Z"), ZoneId.of("UTC"));
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

    @AfterEach
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
        assertThat(persistedTaskRun.getDependentTaskRunIds(), is(sampleTaskRun.getDependentTaskRunIds()));
        assertThat(persistedTaskRun.getTask().getId(), is(sampleTaskRun.getTask().getId()));
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
    public void updateTaskAttempt() {
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(taskAttempt);
        TaskAttempt newAttempt = taskAttempt.cloneBuilder().withRetryTimes(1).build();
        taskRunDao.updateAttempt(newAttempt);
        TaskAttempt saved = taskRunDao.fetchAttemptById(taskAttempt.getId()).get();
        assertThat(saved.getId(), is(newAttempt.getId()));
        assertThat(saved.getTaskId(), is(newAttempt.getTaskId()));
        assertThat(saved.getTaskRun().getId(), is(newAttempt.getTaskRun().getId()));
        assertThat(saved.getRetryTimes(), is(newAttempt.getRetryTimes()));

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

        assertThat(runRec.getStatus(), is(TaskRunStatus.CREATED));
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

        assertThat(runRec.getStatus(), is(TaskRunStatus.CREATED));
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
    public void fetchTaskRunByFilter_withEmptyFilter_shouldReturnTaskRun() {
        //prepare
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);

        // process
        List<TaskRun> allTaskRuns = taskRunDao.fetchTaskRunsByFilterWithoutPagination(TaskRunSearchFilter.newBuilder().build());

        // validate
        assertThat(allTaskRuns.size(), is(1));
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
    public void fetchTaskRunsByFilter_withDateRangeFilter_shouldReturnFilterTaskRuns() {
        // prepare
        DateTimeUtils.setClock(getMockClock());
        prepareTaskRunsWithDependencyRelations();

        // process
        List<TaskRun> runsWithinCreationRange = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withDateFrom(DateTimeUtils.now().plusHours(0))
                .withDateTo(DateTimeUtils.now().plusHours(1))
                .build());

        List<TaskRun> runsWithinQueueRange = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withQueueFrom(DateTimeUtils.now().plusHours(0))
                .withQueueTo(DateTimeUtils.now().plusHours(1))
                .build());

        List<TaskRun> runsWithinStartRange = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withStartFrom(DateTimeUtils.now().plusHours(0))
                .withStartTo(DateTimeUtils.now().plusHours(4))
                .build());

        List<TaskRun> runsWithinTerminationRange = taskRunDao.fetchTaskRunsByFilter(TaskRunSearchFilter
                .newBuilder()
                .withEndAfter(DateTimeUtils.now().plusHours(2))
                .withEndBefore(DateTimeUtils.now().plusHours(3))
                .build());
        // validate
        assertThat(runsWithinCreationRange.size(), is(4));
        assertThat(runsWithinQueueRange.size(), is(4));
        assertThat(runsWithinStartRange.size(), is(2));
        //we use term_at to check termination time which is used only in database
        //test assertion is not supported in code
        //assertThat(runsWithinTerminationRange.size(), is(1));
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
                new Long[]{1L, 2L, 3L, 4L},
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
    public void fetchTaskRunsByFilter_withQueueName() {
        //prepare
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task).cloneBuilder()
                .withQueueName("queue1").build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task).cloneBuilder()
                .withQueueName("queue2").build();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task).cloneBuilder()
                .withQueueName("queue3").build();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);

        //process
        TaskRunSearchFilter filter1 = TaskRunSearchFilter.newBuilder()
                .withQueueName(Arrays.asList("queue1", "queue2")).build();
        List<TaskRun> result1 = taskRunDao.fetchTaskRunsByFilter(filter1);
        assertThat(result1.size(), is(2));
        assertThat(new HashSet<>(Arrays.asList(taskRun1.getId(), taskRun2.getId())),
                is(result1.stream().map(TaskRun::getId).collect(Collectors.toSet())));

        TaskRunSearchFilter filter2 = TaskRunSearchFilter.newBuilder()
                .withQueueName(Arrays.asList("queue3")).build();
        List<TaskRun> result2 = taskRunDao.fetchTaskRunsByFilter(filter2);

        assertThat(result2.size(), is(1));
        assertThat(taskRun3.getId(), is(result2.get(0).getId()));
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

    @Test
    public void fetchTaskRunListWithoutAttempt() {
        //prepare
        Task task1 = MockTaskFactory.createTask();
        taskDao.create(task1);
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskAttempt taskAttempt1 = MockTaskAttemptFactory.createTaskAttempt(taskRun1);
        Task task2 = MockTaskFactory.createTask();
        taskDao.create(task2);
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task1);
        taskRunDao.createTaskRuns(Arrays.asList(taskRun1, taskRun2));
        taskRunDao.createAttempt(taskAttempt1);


        List<TaskRun> taskRunList = taskRunDao.fetchTaskRunListWithoutAttempt();

        //verify
        assertThat(taskRunList.size(), is(1));
        TaskRun fetchedTaskRun = taskRunList.get(0);
        assertThat(taskRun2.getId(), is(fetchedTaskRun.getId()));
        assertThat(taskRun2.getStatus(), is(TaskRunStatus.CREATED));

    }

    @Test
    public void TaskAttemptCreated3daysAgo_shouldNotExecute() {
        //prepare attempt
        OffsetDateTime currentDate = DateTimeUtils.freeze();
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(taskAttempt);
        //3 days passed
        OffsetDateTime threeDaysAfter = currentDate.plusDays(3).plusHours(1);
        DateTimeUtils.freezeAt(threeDaysAfter.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        List<TaskAttempt> readyAttemptList = taskRunDao.fetchAllSatisfyTaskAttempt();
        assertThat(readyAttemptList, hasSize(0));

        DateTimeUtils.resetClock();

    }

    @Test
    public void testNoReadyTaskRunFetchAllSatisfyTaskAttempt_shouldReturnEmptyList() {
        List<TaskAttempt> readyAttemptList = taskRunDao.fetchAllSatisfyTaskAttempt();
        assertThat(readyAttemptList, hasSize(0));
    }

    @Test
    public void testFetchAllSatisfyTaskAttempt_shouldSuccess() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        for (TaskRun taskRun : taskRunList) {
            taskDao.create(taskRun.getTask());
            taskRunDao.createTaskRun(taskRun);
            taskRunDao.createAttempt(MockTaskAttemptFactory.createTaskAttempt(taskRun));
        }
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);

        List<Long> satisfiedTaskRuns = taskRunDao.fetchAllSatisfyTaskRunId();

        assertThat(satisfiedTaskRuns.size(), is(1));
        assertThat(satisfiedTaskRuns.get(0), is(taskRun1.getId()));

        taskRunDao.updateTaskRunStatusByTaskRunId(Collections.singletonList(taskRun1.getId()), TaskRunStatus.SUCCESS);
        taskRunDao.updateConditionsWithTaskRuns(Collections.singletonList(taskRun1.getId()), TaskRunStatus.SUCCESS);

        satisfiedTaskRuns = taskRunDao.fetchAllSatisfyTaskRunId();
        assertThat(satisfiedTaskRuns.size(), is(1));
        assertThat(satisfiedTaskRuns.get(0), is(taskRun2.getId()));

    }

    @Test
    public void testFetchTaskAttemptListForRecover() {
        //prepare
        TaskRunStatus[] taskRunStatuses = TaskRunStatus.values();
        List<Task> taskList = MockTaskFactory.createTasks(taskRunStatuses.length);
        for (int i = 0; i < taskRunStatuses.length; i++) {
            TaskRun taskRun = MockTaskRunFactory.createTaskRunWithStatus(taskList.get(i), taskRunStatuses[i]);
            TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
            taskDao.create(taskList.get(i));
            taskRunDao.createTaskRun(taskRun);
            taskRunDao.createAttempt(taskAttempt);
        }

        List<TaskRunStatus> taskRunStatusConditions = Arrays.asList(TaskRunStatus.CREATED, TaskRunStatus.QUEUED, TaskRunStatus.ERROR);
        List<TaskAttempt> selectedAttempt = taskRunDao.fetchTaskAttemptListForRecover(taskRunStatusConditions);
        List<TaskRunStatus> selectedStatus = selectedAttempt.stream().map(TaskAttempt::getStatus).collect(Collectors.toList());

        //verify
        assertThat(selectedAttempt, hasSize(3));
        assertThat(selectedStatus, containsInAnyOrder(TaskRunStatus.CREATED, TaskRunStatus.ERROR, TaskRunStatus.QUEUED));

    }

    @Test
    public void fetchFailedUpstreamTaskRuns_ShouldSuccess() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        Task task1 = taskList.get(0);
        Task task2 = taskList.get(1);
        taskList.forEach(task -> taskDao.create(task));
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1)
                .cloneBuilder().withStatus(TaskRunStatus.FAILED).build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2)
                .cloneBuilder().withFailedUpstreamTaskRunIds(Arrays.asList(taskRun1.getId())).build();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);

        assertThat(taskRunDao.fetchFailedUpstreamTaskRuns(taskRun2.getId()).get(0).getId(), is(taskRun1.getId()));
    }

    @Test
    public void updateTaskRunWithFailedUpstream() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>1;1>>2");
        Task task1 = taskList.get(0);
        Task task2 = taskList.get(1);
        Task task3 = taskList.get(2);
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task2);
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task3);
        taskList.forEach(task -> taskDao.create(task));
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);


        taskRun1 = taskRun1.cloneBuilder().withStatus(TaskRunStatus.FAILED).build();
        List<Long> downstreamTaskRunIds = Arrays.asList(taskRun2.getId(), taskRun3.getId());
        taskRunDao.updateTaskRunWithFailedUpstream(taskRun1.getId(), downstreamTaskRunIds, TaskRunStatus.UPSTREAM_FAILED);

        assertThat(taskRunDao.fetchTaskRunById(taskRun2.getId()).get().getFailedUpstreamTaskRunIds().get(0),
                is(taskRun1.getId()));
        assertThat(taskRunDao.fetchTaskRunById(taskRun3.getId()).get().getFailedUpstreamTaskRunIds().get(0),
                is(taskRun1.getId()));

        taskRun1 = taskRun1.cloneBuilder().withStatus(TaskRunStatus.CREATED).build();
        taskRunDao.updateTaskRunWithFailedUpstream(taskRun1.getId(), downstreamTaskRunIds, TaskRunStatus.CREATED);
        assertThat(taskRunDao.fetchTaskRunById(taskRun2.getId()).get().getFailedUpstreamTaskRunIds(),
                is(empty()));
        assertThat(taskRunDao.fetchTaskRunById(taskRun3.getId()).get().getFailedUpstreamTaskRunIds(),
                is(empty()));


    }

    @Test
    public void getTickByTaskRunId_withTick_shouldSuccess() {
        String tick = "202110131111";
        Task taskWithUTC = MockTaskFactory.createTask();
        taskDao.create(taskWithUTC);
        TaskRun taskRunWithUTC = MockTaskRunFactory.createTaskRun(1L, taskWithUTC)
                .cloneBuilder()
                .withScheduledTick(new Tick(tick))
                .build();
        taskRunDao.createTaskRun(taskRunWithUTC);
        assertTrue(tick.equals(taskRunDao.getTickByTaskRunId(1L)));
    }

    @Test
    public void getScheduleTimeByTaskRunId_withTick_shouldSuccess() {
        String scheduleTime = "202206231000";
        Task taskWithUTC = MockTaskFactory.createTask();
        taskDao.create(taskWithUTC);
        TaskRun taskRunWithUTC = MockTaskRunFactory.createTaskRun(1L, taskWithUTC)
                .cloneBuilder()
                .withScheduleTime(new Tick(scheduleTime))
                .build();
        taskRunDao.createTaskRun(taskRunWithUTC);
        assertTrue(scheduleTime.equals(taskRunDao.getScheduleTimeByTaskRunId(1L)));
    }

    @Test
    public void fetchTaskRunConditionsById_shouldSuccess() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        List<TaskRun> taskRuns = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        for (TaskRun taskRun : taskRuns) {
            taskDao.create(taskRun.getTask());
            taskRunDao.createTaskRun(taskRun);
        }

        TaskRun taskRun1 = taskRuns.get(0);
        TaskRun taskRun2 = taskRuns.get(1);

        List<TaskRunCondition> taskRunConditions = taskRunDao.fetchTaskRunConditionsById(taskRun2.getId());

        assertThat(taskRunConditions.size(), is(1));
        TaskRunCondition taskRunCondition = taskRunConditions.get(0);
        assertThat(taskRunCondition.getCondition(), is(new Condition(Collections.singletonMap("taskRunId", taskRun1.getId().toString()))));

    }

    @Test
    public void updateTaskRunStatusByTaskRunId_shouldSuccess() {
        Task task1 = MockTaskFactory.createTask();
        Task task2 = MockTaskFactory.createTask();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task1);
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task1);
        taskDao.create(task1);
        taskDao.create(task2);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createAttempt(MockTaskAttemptFactory.createTaskAttempt(taskRun1));
        taskRunDao.createAttempt(MockTaskAttemptFactory.createTaskAttempt(taskRun2));

        taskRunDao.updateTaskRunStatusByTaskRunId(Collections.singletonList(taskRun1.getId()), TaskRunStatus.BLOCKED);
        taskRunDao.updateTaskRunStatusByTaskRunId(Collections.singletonList(taskRun2.getId()), TaskRunStatus.UPSTREAM_FAILED);

        TaskRun tr1 = taskRunDao.fetchTaskRunById(taskRun1.getId()).get();
        TaskRun tr2 = taskRunDao.fetchTaskRunById(taskRun2.getId()).get();

        assertThat(tr1.getStatus(), is(TaskRunStatus.BLOCKED));
        assertThat(tr2.getStatus(), is(TaskRunStatus.UPSTREAM_FAILED));
    }

    //scenario: task1>>task2  task1->task3 which means task1 is task2's upstream and task1 is task3's predecessor
    // when task1 success change task2 and task3's condition result
    @Test
    public void updateConditionsWithTaskRun_statusIsSuccess_shouldSuccess() {
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        Task task1 = taskList.get(0);
        Task task2 = taskList.get(1);
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);
        Task task3 = MockTaskFactory.createTask();
        TaskRunCondition taskRunCondition3 = TaskRunCondition.newBuilder()
                .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun1.getId().toString())))
                .withResult(false)
                .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH)
                .build();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task3).cloneBuilder()
                .withTaskRunConditions(Collections.singletonList(taskRunCondition3))
                .build();
        taskDao.create(task1);
        taskDao.create(task2);
        taskDao.create(task3);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);

        //taskRun1 success
        taskRunDao.updateConditionsWithTaskRuns(Collections.singletonList(taskRun1.getId()), TaskRunStatus.SUCCESS);

        //check taskRun 2&3's condition result
        TaskRunCondition tc2 = taskRunDao.fetchTaskRunConditionsById(taskRun2.getId()).get(0);
        TaskRunCondition tc3 = taskRunDao.fetchTaskRunConditionsById(taskRun3.getId()).get(0);
        assertThat(tc2.getCondition(), is(new Condition(Collections.singletonMap("taskRunId", taskRun1.getId().toString()))));
        assertThat(tc3.getCondition(), is(new Condition(Collections.singletonMap("taskRunId", taskRun1.getId().toString()))));
        assertThat(tc2.getType(), is(ConditionType.TASKRUN_DEPENDENCY_SUCCESS));
        assertThat(tc3.getType(), is(ConditionType.TASKRUN_PREDECESSOR_FINISH));
        assertThat(tc2.getResult(), is(true));
        assertThat(tc3.getResult(), is(true));
    }

    @Test
    public void fetchRestrictedTaskRunIdsFromCondition_shouldSuccess() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>1;0>>2");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;0>>2");
        for (TaskRun taskRun : taskRunList) {
            taskDao.create(taskRun.getTask());
            taskRunDao.createTaskRun(taskRun);
        }
        TaskRun taskRun0 = taskRunList.get(0);
        TaskRun taskRun1 = taskRunList.get(1);
        TaskRun taskRun2 = taskRunList.get(2);
        Condition condition = new Condition(Collections.singletonMap("taskRunId", taskRun0.getId().toString()));
        List<Long> restrictedTaskRunIds = taskRunDao.fetchRestrictedTaskRunIdsFromConditions(Collections.singletonList(condition));

        assertThat(restrictedTaskRunIds.size(), is(2));
        assertThat(new HashSet<>(restrictedTaskRunIds), is(new HashSet<>(Arrays.asList(taskRun1.getId(), taskRun2.getId()))));
    }

    //scenario: 0,1,2 are all 3,4's upstream
    // make 0, 1, 2 success in order, check function result
    @Test
    public void fetchRestrictedTaskRunIdsWithConditionType_shouldSuccess() {
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(5, "0>>3;1>>3;2>>3;0>>4;1>>4;2>>4");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>3;1>>3;2>>3;0>>4;1>>4;2>>4");
        for (TaskRun taskRun : taskRunList) {
            taskDao.create(taskRun.getTask());
            taskRunDao.createTaskRun(taskRun);
        }
        TaskRun taskRun0 = taskRunList.get(0);
        TaskRun taskRun1 = taskRunList.get(1);
        TaskRun taskRun2 = taskRunList.get(2);
        TaskRun taskRun3 = taskRunList.get(3);
        TaskRun taskRun4 = taskRunList.get(4);
        // 0 success
        taskRunDao.updateConditionsWithTaskRuns(Collections.singletonList(taskRun0.getId()), TaskRunStatus.SUCCESS);
        Condition condition = new Condition(Collections.singletonMap("taskRunId", taskRun0.getId().toString()));
        List<Long> restrictedTaskRunIds = taskRunDao.fetchRestrictedTaskRunIdsFromConditions(Collections.singletonList(condition));
        List<Long> taskRunIdsWithUpstreamFailed = taskRunDao.fetchRestrictedTaskRunIdsWithConditionType(restrictedTaskRunIds, ConditionType.TASKRUN_DEPENDENCY_SUCCESS);
        //check 0 success result
        assertThat(taskRunIdsWithUpstreamFailed.size(), is(2));
        assertThat(new HashSet<>(taskRunIdsWithUpstreamFailed),
                is(new HashSet<>(Arrays.asList(taskRun3.getId(), taskRun4.getId()))));

        //1 success
        taskRunDao.updateConditionsWithTaskRuns(Collections.singletonList(taskRun1.getId()), TaskRunStatus.SUCCESS);
        condition = new Condition(Collections.singletonMap("taskRunId", taskRun1.getId().toString()));
        restrictedTaskRunIds = taskRunDao.fetchRestrictedTaskRunIdsFromConditions(Collections.singletonList(condition));
        taskRunIdsWithUpstreamFailed = taskRunDao.fetchRestrictedTaskRunIdsWithConditionType(restrictedTaskRunIds, ConditionType.TASKRUN_DEPENDENCY_SUCCESS);

        //check 1 success result
        assertThat(taskRunIdsWithUpstreamFailed.size(), is(2));
        assertThat(new HashSet<>(taskRunIdsWithUpstreamFailed),
                is(new HashSet<>(Arrays.asList(taskRun3.getId(), taskRun4.getId()))));

        // 2 success
        taskRunDao.updateConditionsWithTaskRuns(Collections.singletonList(taskRun2.getId()), TaskRunStatus.SUCCESS);
        condition = new Condition(Collections.singletonMap("taskRunId", taskRun2.getId().toString()));
        restrictedTaskRunIds = taskRunDao.fetchRestrictedTaskRunIdsFromConditions(Collections.singletonList(condition));
        taskRunIdsWithUpstreamFailed = taskRunDao.fetchRestrictedTaskRunIdsWithConditionType(restrictedTaskRunIds, ConditionType.TASKRUN_DEPENDENCY_SUCCESS);

        //check 2 success result
        assertThat(taskRunIdsWithUpstreamFailed.size(), is(0));

    }

    //scenario: task run 0 is 1's predecessor
    // when 0 is running, 1 is blocked
    @Test
    public void fetchTaskRunIdsWithBlockType_shouldSuccess() {
        Task task = MockTaskFactory.createTask().cloneBuilder()
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 * * ? * * *",
                        ZoneOffset.UTC.getId(), BlockType.WAIT_PREDECESSOR))
                .build();
        taskDao.create(task);
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task).cloneBuilder()
                .withStatus(TaskRunStatus.RUNNING).build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task).cloneBuilder()
                .withStatus(TaskRunStatus.BLOCKED)
                .withTaskRunConditions(Collections.singletonList(TaskRunCondition.newBuilder()
                        .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun1.getId().toString())))
                        .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH).withResult(false).build()))
                .build();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        List<Long> result = taskRunDao.fetchTaskRunIdsWithBlockType(Collections.singletonList(taskRun2.getId()));

        assertThat(result.get(0), is(taskRun2.getId()));
    }

    @Test
    public void fetchDirectUpstreamTaskRun_should_only_contains_direct_upstream() {
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4, "0>>1;1>>3;2>>3");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;1>>3;2>>3");
        for (Task task : taskList) {
            taskDao.create(task);
        }
        for (TaskRun taskRun : taskRunList) {
            taskRunDao.createTaskRun(taskRun);
        }
        TaskRun srcTaskRun = taskRunList.get(3);

        List<TaskRunProps> directUpstream = taskRunDao.fetchUpstreamTaskRunsById(srcTaskRun.getId());

        //verify
        assertThat(directUpstream, hasSize(2));
        List<Long> fetchedIds = directUpstream.stream().map(TaskRunProps::getId).collect(Collectors.toList());
        TaskRun taskRun2 = taskRunList.get(1);
        TaskRun taskRun3 = taskRunList.get(2);
        assertThat(fetchedIds, containsInAnyOrder(taskRun2.getId(), taskRun3.getId()));
    }

    @Test
    public void updateAttemptStatusByTaskRunIds_with_filter_should_only_act_on_filter() {
        //prepare
        //init taskRun1 status = UPSTREAM_FAILED, other taskRun status = FAILED
        List<TaskRunStatus> filterStatus = Lists.newArrayList(TaskRunStatus.UPSTREAM_FAILED);
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4, "0>>1;1>>3;2>>3");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;1>>3;2>>3");
        for (Task task : taskList) {
            taskDao.create(task);
        }
        TaskRun taskRun1 = taskRunList.get(0);
        TaskAttempt taskAttempt1 = MockTaskAttemptFactory.createTaskAttempt(taskRun1);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createAttempt(taskAttempt1);
        taskRunDao.updateTaskAttemptStatus(taskAttempt1.getId(),TaskRunStatus.UPSTREAM_FAILED);
        for (int i = 1; i < taskRunList.size(); i++) {
            TaskRun taskRun = taskRunList.get(i);
            TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt(taskRun);
            taskRunDao.createTaskRun(taskRun);
            taskRunDao.createAttempt(taskAttempt);
            taskRunDao.updateTaskAttemptStatus(taskAttempt.getId(),TaskRunStatus.FAILED);
        }
        List<Long> taskRunIds = taskRunList.stream().map(TaskRun::getId).collect(Collectors.toList());

        taskRunDao.updateAttemptStatusByTaskRunIds(taskRunIds, TaskRunStatus.CREATED, DateTimeUtils.now(), filterStatus);

        //verify
        TaskRun saved1 = taskRunDao.fetchTaskRunById(taskRun1.getId()).get();
        TaskRun saved2 = taskRunDao.fetchTaskRunById(taskRunIds.get(1)).get();
        TaskRun saved3 = taskRunDao.fetchTaskRunById(taskRunIds.get(2)).get();
        TaskRun saved4 = taskRunDao.fetchTaskRunById(taskRunIds.get(3)).get();
        assertThat(saved1.getStatus(),is(TaskRunStatus.CREATED));
        assertThat(saved2.getStatus(),is(TaskRunStatus.FAILED));
        assertThat(saved3.getStatus(),is(TaskRunStatus.FAILED));
        assertThat(saved4.getStatus(),is(TaskRunStatus.FAILED));

    }

    @Test
    public void updateAndFetchTaskRunStat_zeroPrevTaskRun_valueShouldBeZero() {
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun = MockTaskRunFactory.createTaskRun(task);
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.updateTaskRunStat(taskRun.getId(), 0L, 0L);

        List<TaskRunStat> taskRunStatusList = taskRunDao.fetchTaskRunStat(Collections.singletonList(taskRun.getId()));
        assertThat(taskRunStatusList.get(0).getAverageRunningTime(), is(0L));
        assertThat(taskRunStatusList.get(0).getAverageQueuingTime(), is(0L));
    }

    @Test
    public void updateAndFetchTaskRunStat_singlePrevTaskRun_shouldSuccess() {
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task).cloneBuilder()
                .withQueuedAt(DateTimeUtils.now().minusMinutes(1))
                .withStartAt(DateTimeUtils.now())
                .withEndAt(DateTimeUtils.now().plusMinutes(10))
                .withStatus(TaskRunStatus.SUCCESS)
                .withScheduleType(ScheduleType.SCHEDULED)
                .build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task);
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.updateTaskRunStat(taskRun2.getId(), 600L, 60L);

        List<TaskRunStat> taskRunStatusList = taskRunDao.fetchTaskRunStat(Collections.singletonList(taskRun2.getId()));
        assertThat(taskRunStatusList.get(0).getAverageRunningTime(), is(600L));
        assertThat(taskRunStatusList.get(0).getAverageQueuingTime(), is(60L));
    }

    @Test
    public void updateAndFetchTaskRunStat_multiPrevTaskRun_shouldSuccess() {
        Task task = MockTaskFactory.createTask();
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task).cloneBuilder()
                .withQueuedAt(DateTimeUtils.now().minusMinutes(1))
                .withStartAt(DateTimeUtils.now())
                .withEndAt(DateTimeUtils.now().plusMinutes(10))
                .withStatus(TaskRunStatus.SUCCESS)
                .withScheduleType(ScheduleType.SCHEDULED)
                .build();
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task).cloneBuilder()
                .withQueuedAt(DateTimeUtils.now().minusMinutes(3))
                .withStartAt(DateTimeUtils.now())
                .withEndAt(DateTimeUtils.now().plusMinutes(8))
                .withStatus(TaskRunStatus.SUCCESS)
                .withScheduleType(ScheduleType.SCHEDULED)
                .build();
        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task);
        taskDao.create(task);
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        taskRunDao.updateTaskRunStat(taskRun3.getId(), 540L, 120L);

        List<TaskRunStat> taskRunStatusList = taskRunDao.fetchTaskRunStat(Collections.singletonList(taskRun3.getId()));
        assertThat(taskRunStatusList.get(0).getAverageRunningTime(), is(540L));
        assertThat(taskRunStatusList.get(0).getAverageQueuingTime(), is(120L));
    }

    @Test
    public void fetchUpstreamTaskRunIds_shouldSuccess() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>2;1>>2");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>2;1>>2");
        for(TaskRun taskRun : taskRunList) {
            taskDao.create(taskRun.getTask());
            taskRunDao.createTaskRun(taskRun);
        }

        List<Long> fetchedUpstreamTaskRunIds = taskRunDao.fetchUpStreamTaskRunIds(Collections.singletonList(taskRunList.get(2).getId()));

        assertThat(new HashSet<>(fetchedUpstreamTaskRunIds), is(new HashSet<>(Arrays.asList(taskRunList.get(0).getId(), taskRunList.get(1).getId()))));
    }

    @Test
    public void fetchUpStreamTaskRunIdsRecursive_shouldSuccess() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>1;1>>2");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1;1>>2");
        for(TaskRun taskRun : taskRunList) {
            taskDao.create(taskRun.getTask());
            taskRunDao.createTaskRun(taskRun);
        }

        List<Long> fetchedUpstreamTaskRunIds = taskRunDao.fetchUpStreamTaskRunIdsRecursive(taskRunList.get(2).getId());

        assertThat(new HashSet<>(fetchedUpstreamTaskRunIds), is(new HashSet<>(Arrays.asList(taskRunList.get(0).getId(), taskRunList.get(1).getId()))));
    }

    @Test
    public void fetchTaskRunsByIds_shouldSuccess() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(2, "0>>1");
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>1");
        for(TaskRun taskRun : taskRunList) {
            taskDao.create(taskRun.getTask());
            taskRunDao.createTaskRun(taskRun);
        }
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1);

        List<TaskRun> fetchedTaskRunList = taskRunDao.fetchTaskRunsByIds(Arrays.asList(taskRun1.getId(), taskRun2.getId()))
                .stream().map(Optional::get).collect(Collectors.toList());

        Assertions.assertEquals(new HashSet<>(Arrays.asList(taskRun1.getId(), taskRun2.getId())),
                new HashSet<>(fetchedTaskRunList.stream().map(TaskRun::getId).collect(Collectors.toList())));
        Assertions.assertEquals(taskRun1.getId(), fetchedTaskRunList.get(1).getDependentTaskRunIds().get(0));
    }

    @Test
    public void resetTaskRunTimestampToNull_shouldSuccess() {
        TaskAttempt taskAttempt = MockTaskAttemptFactory.createTaskAttempt();
        TaskRun taskRun = taskAttempt.getTaskRun();
        taskRunDao.createTaskRun(taskRun);
        taskRunDao.createAttempt(taskAttempt);
        taskDao.create(taskRun.getTask());
        OffsetDateTime now = DateTimeUtils.now();
        taskRunDao.updateAttemptStatusByTaskRunIds(Collections.singletonList(taskRun.getId()), TaskRunStatus.UPSTREAM_FAILED,
                now, Collections.singletonList(TaskRunStatus.CREATED));

        TaskRun fetchedTaskRun = taskRunDao.fetchTaskRunById(taskRun.getId()).get();

        assertThat(fetchedTaskRun.getTermAt(), is(now));

        taskRunDao.updateAttemptStatusByTaskRunIds(Collections.singletonList(taskRun.getId()), TaskRunStatus.CREATED,
                null, Collections.singletonList(TaskRunStatus.UPSTREAM_FAILED));

        taskRunDao.resetTaskRunTimestampToNull(Collections.singletonList(taskRun.getId()), "term_at");
        fetchedTaskRun = taskRunDao.fetchTaskRunById(taskRun.getId()).get();
        assertNull(fetchedTaskRun.getTermAt());
    }

    @Test
    public void taskRunShouldBeCreatedTest(){
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(4,"0>>2;0>>3;1>>2");
        for (Task task : taskList){
            taskDao.create(task);
        }
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList,"0>>2;0>>3;1>>2");
        TaskRun taskRun1 = taskRunList.get(0);
        TaskRun taskRun2 = taskRunList.get(1).cloneBuilder().withStatus(TaskRunStatus.FAILED).build();
        TaskRun taskRun3 = taskRunList.get(2).cloneBuilder().withStatus(TaskRunStatus.UPSTREAM_FAILED).build();
        TaskRun taskRun4 = taskRunList.get(3).cloneBuilder().withStatus(TaskRunStatus.UPSTREAM_FAILED)
                .withFailedUpstreamTaskRunIds(Arrays.asList(taskRun2.getId())).build();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        taskRunDao.createTaskRun(taskRun4);

        DataStore dataStore = MockDataStoreFactory.getMockDataStore(DataStoreType.HIVE_TABLE,"aa","bb");
        taskRunDao.updateTaskRunInletsOutlets(taskRun1.getId(),Arrays.asList(dataStore),new ArrayList<>());

        List<Long> taskRunShouldBeCreated = taskRunDao.taskRunShouldBeCreated(Arrays.asList(taskRun3.getId(),taskRun4.getId()));

        //verify
        assertThat(taskRunShouldBeCreated,hasSize(1));
        assertThat(taskRunShouldBeCreated.get(0),is(taskRun3.getId()));
    }

    @Test
    public void removeFailedUpstreamTaskRunIds_shouldSuccess() {
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(5, "0>>3;1>>3;2>>3;3>>4");
        taskList.forEach(x -> taskDao.create(x));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>3;1>>3;2>>3;3>>4");
        TaskRun taskRun1 = taskRunList.get(0).cloneBuilder().withStatus(TaskRunStatus.FAILED).build();
        TaskRun taskRun2 = taskRunList.get(1).cloneBuilder().withStatus(TaskRunStatus.FAILED).build();
        TaskRun taskRun3 = taskRunList.get(2).cloneBuilder().withStatus(TaskRunStatus.FAILED).build();
        TaskRun taskRun4 = taskRunList.get(3).cloneBuilder().withStatus(TaskRunStatus.UPSTREAM_FAILED)
                .withFailedUpstreamTaskRunIds(Arrays.asList(taskRun1.getId(), taskRun2.getId(), taskRun3.getId())).build();
        TaskRun taskRun5 = taskRunList.get(4).cloneBuilder().withStatus(TaskRunStatus.UPSTREAM_FAILED)
                .withFailedUpstreamTaskRunIds(Arrays.asList(taskRun1.getId(), taskRun2.getId(), taskRun3.getId())).build();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        taskRunDao.createTaskRun(taskRun4);
        taskRunDao.createTaskRun(taskRun5);

        taskRunDao.removeFailedUpstreamTaskRunIds(Arrays.asList(taskRun4.getId(), taskRun5.getId()), Arrays.asList(taskRun1.getId(), taskRun2.getId()));

        TaskRun savedTaskRun4 = taskRunDao.fetchTaskRunById(taskRun4.getId()).get();
        TaskRun savedTaskRun5 = taskRunDao.fetchTaskRunById(taskRun5.getId()).get();
        assertThat(savedTaskRun4.getFailedUpstreamTaskRunIds().size(), is(1));
        assertThat(savedTaskRun5.getFailedUpstreamTaskRunIds().size(), is(1));
        assertThat(savedTaskRun4.getFailedUpstreamTaskRunIds().get(0), is(taskRun3.getId()));
        assertThat(savedTaskRun5.getFailedUpstreamTaskRunIds().get(0), is(taskRun3.getId()));
    }

    @Test
    public void lossUpdateConditionTaskRunsTest(){
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>2;1>>2;");
        taskList.forEach(x -> taskDao.create(x));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>2;1>>2;");
        TaskRun taskRun1 = taskRunList.get(0).cloneBuilder().withStatus(TaskRunStatus.SUCCESS).build();
        TaskRun taskRun2 = taskRunList.get(1).cloneBuilder().withStatus(TaskRunStatus.CREATED).build();
        TaskRun taskRun3 = taskRunList.get(2).cloneBuilder().withStatus(TaskRunStatus.CREATED).build();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);
        //update

        List<Long> lossUpdateTaskRuns = taskRunDao.lossUpdateConditionTaskRuns();

        assertThat(lossUpdateTaskRuns,hasSize(1));
        assertThat(lossUpdateTaskRuns,containsInAnyOrder(taskRun1.getId()));
    }

    @Test
    public void fixConditionWithTaskRunIdTest(){
        //prepare
        List<Task> taskList = MockTaskFactory.createTasksWithRelations(3, "0>>2;1>>2;");
        taskList.forEach(x -> taskDao.create(x));
        List<TaskRun> taskRunList = MockTaskRunFactory.createTaskRunsWithRelations(taskList, "0>>2;1>>2;");
        TaskRun taskRun1 = taskRunList.get(0).cloneBuilder().withStatus(TaskRunStatus.SUCCESS).build();
        TaskRun taskRun2 = taskRunList.get(1).cloneBuilder().withStatus(TaskRunStatus.SUCCESS).build();
        TaskRun taskRun3 = taskRunList.get(2).cloneBuilder().withStatus(TaskRunStatus.CREATED).build();
        taskRunDao.createTaskRun(taskRun1);
        taskRunDao.createTaskRun(taskRun2);
        taskRunDao.createTaskRun(taskRun3);

        taskRunDao.fixConditionWithTaskRunId(taskRun1.getId());

        List<Long> lossUpdateTaskRuns = taskRunDao.lossUpdateConditionTaskRuns();

        assertThat(lossUpdateTaskRuns,hasSize(1));
        assertThat(lossUpdateTaskRuns,containsInAnyOrder(taskRun2.getId()));
    }
}
