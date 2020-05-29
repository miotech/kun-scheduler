package com.miotech.kun.workflow.common.taskrun.dao;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import org.junit.Test;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
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

    @Test
    public void createTaskRun_withValidProperties_shouldSuccess() {
        // Prepare
        Task task = MockTaskFactory.createMockTask();
        Clock mockClock = getMockClock();
        taskDao.create(task);

        TaskRun sampleTaskRun = MockTaskRunFactory.generateSampleTaskRun(1L, task, mockClock);

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
        TaskRun sampleTaskRun = MockTaskRunFactory.generateSampleTaskRun(1L, null, mockClock);

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

        // 1. create task runs
        Task task = MockTaskFactory.createMockTask();
        Clock mockClock = getMockClock();
        taskDao.create(task);

        TaskRun[] sampleTaskRuns = {
                MockTaskRunFactory.generateSampleTaskRun(1L, task, mockClock),
                MockTaskRunFactory.generateSampleTaskRun(2L, task, mockClock),
                MockTaskRunFactory.generateSampleTaskRun(3L, task, mockClock)
        };

        taskRunDao.createTaskRun(sampleTaskRuns[0]);
        taskRunDao.createTaskRun(sampleTaskRuns[1]);
        taskRunDao.createTaskRun(sampleTaskRuns[2]);

        // 2. create run attempts (12 attempts in total, 4 attempts each run)
        for (int i = 0; i < 12; i += 1) {
            TaskAttempt attempt = MockTaskRunFactory.generateTaskAttempt((long) i + 1, sampleTaskRuns[i / 4], (i % 4) + 1, mockClock);
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
}
