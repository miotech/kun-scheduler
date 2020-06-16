package com.miotech.kun.workflow.common.taskrun.dao;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.junit.After;
import org.junit.Test;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.*;

public class TaskRunDaoTest extends DatabaseTestBase {
    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskDao taskDao;

    private Clock getMockClock() {
        return Clock.fixed(Instant.parse("2020-01-01T00:00:00.00Z"), ZoneId.of("UTC"));
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

        Task task = MockTaskFactory.createTask();
        taskDao.create(task);

        TaskRun sampleTaskRun = MockTaskRunFactory.createTaskRun(1L, task)
                .cloneBuilder()
                .withDependentTaskRunIds(Lists.newArrayList(Long.valueOf(1L)))
                .build();

        // Process
        taskRunDao.createTaskRun(sampleTaskRun);

        // Validate
        Optional<TaskRun> persistedTaskRunOptional = taskRunDao.fetchById(1L);
        assertTrue(persistedTaskRunOptional.isPresent());

        TaskRun persistedTaskRun = persistedTaskRunOptional.get();
        assertThat(persistedTaskRun, samePropertyValuesAs(sampleTaskRun, "startAt", "endAt"));
    }

    @Test
    public void createTaskRun_withInvalidProperties_shouldThrowException() {
        // Prepare
        Clock mockClock = getMockClock();

        // 1. if task is null, should throw NullPointerException
        TaskRun sampleTaskRun = MockTaskRunFactory.createTaskRun(1L, null);

        try {
            taskRunDao.createTaskRun(sampleTaskRun);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }

        // TODO: add more cases
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
                .build();
        taskRunDao.updateTaskRun(taskRunWithUpdatedProps);

        // Validate
        // 4. fetch and validate
        Optional<TaskRun> persistedTaskRunOptional = taskRunDao.fetchById(1L);
        assertTrue(persistedTaskRunOptional.isPresent());
        TaskRun persistedTaskRun = persistedTaskRunOptional.get();
        assertThat(persistedTaskRun, samePropertyValuesAs(taskRunWithUpdatedProps, "startAt", "endAt"));
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
        Optional<TaskRun> persistedTaskRunOptional = taskRunDao.fetchById(1L);
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
        assertThat(attempt.getTaskRun(), samePropertyValuesAs(sampleTaskRun, "startAt", "endAt"));
        // And Task model object should be nested inside that TaskRun object
        assertThat(attempt.getTaskRun().getTask(), notNullValue());
        assertThat(attempt.getTaskRun().getTask(), samePropertyValuesAs(task));
    }
}
