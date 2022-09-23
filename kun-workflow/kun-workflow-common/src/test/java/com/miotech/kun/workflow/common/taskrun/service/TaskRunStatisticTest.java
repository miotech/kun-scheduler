package com.miotech.kun.workflow.common.taskrun.service;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.miotech.kun.workflow.common.CommonTestBase;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.TaskRunCreatedEvent;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStat;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskRunFactory;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TaskRunStatisticTest extends CommonTestBase {

    private TaskRunStatistic taskRunStatistic;

    @Inject
    private TaskDao taskDao;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private EventBus eventBus;

    @Override
    protected void configuration() {
        super.configuration();
    }

    @BeforeEach
    public void setUp() throws IOException {
        this.taskRunDao = spy(this.taskRunDao);
        this.taskRunStatistic = spy(new TaskRunStatistic(taskRunDao, eventBus));
        taskRunStatistic.start();
    }

    @Test
    public void updateTaskRunStat_withZeroPrevTaskRun_shouldSuccess() throws InterruptedException {
        //prepare
        OffsetDateTime baseTime = DateTimeUtils.now();
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task);
        taskRunDao.createTaskRun(taskRun1);

        //process
        eventBus.post(new TaskRunCreatedEvent(taskRun1.getTask().getId(), taskRun1.getId()));
        Thread.sleep(3000);


        //verify
        TaskRunStat taskRunStat = taskRunDao.fetchTaskRunStat(Collections.singletonList(taskRun1.getId())).get(0);

        assertThat(taskRunStat.getId(), is(taskRun1.getId()));
        assertThat(taskRunStat.getAverageRunningTime(), is(0L));
        assertThat(taskRunStat.getAverageQueuingTime(), is(0L));
    }

    @Test
    public void updateTaskRunStat_withMultiPrevTaskRun_shouldSuccess() throws InterruptedException {
        //prepare
        OffsetDateTime baseTime = DateTimeUtils.now();
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);
        TaskRun taskRun1 = MockTaskRunFactory.createTaskRun(task).cloneBuilder().
                withCreatedAt(baseTime.minusDays(2).plusHours(1)).build();
        taskRunDao.createTaskRun(taskRun1);
        taskRun1 = taskRun1.cloneBuilder()
                .withStatus(TaskRunStatus.SUCCESS)
                .withQueuedAt(baseTime.minusDays(2).plusHours(2))
                .withStartAt(baseTime.minusDays(2).plusHours(3))
                .withEndAt(baseTime.minusDays(2).plusHours(4))
                .build();
        taskRunDao.updateTaskRun(taskRun1);
        TaskRun taskRun2 = MockTaskRunFactory.createTaskRun(task).cloneBuilder().
                withCreatedAt(baseTime.minusDays(1).plusHours(1)).build();
        taskRunDao.createTaskRun(taskRun2);
        taskRun2 = taskRun2.cloneBuilder()
                .withStatus(TaskRunStatus.SUCCESS)
                .withQueuedAt(baseTime.minusDays(1).plusHours(2))
                .withStartAt(baseTime.minusDays(1).plusHours(3))
                .withEndAt(baseTime.minusDays(1).plusHours(4))
                .build();
        taskRunDao.updateTaskRun(taskRun2);

        TaskRun taskRun3 = MockTaskRunFactory.createTaskRun(task);
        taskRunDao.createTaskRun(taskRun3);

        //process
        eventBus.post(new TaskRunCreatedEvent(taskRun3.getTask().getId(), taskRun3.getId()));
        Thread.sleep(3000);

        //verify
        TaskRunStat taskRunStat = taskRunDao.fetchTaskRunStat(Collections.singletonList(taskRun3.getId())).get(0);

        assertThat(taskRunStat.getId(), is(taskRun3.getId()));
        assertThat(taskRunStat.getAverageRunningTime(), is(3600L));
        assertThat(taskRunStat.getAverageQueuingTime(), is(3600L));
    }

}