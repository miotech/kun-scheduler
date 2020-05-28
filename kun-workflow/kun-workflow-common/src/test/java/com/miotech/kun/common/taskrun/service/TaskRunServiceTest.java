package com.miotech.kun.common.taskrun.service;

import com.google.inject.Inject;
import com.miotech.kun.common.CommonTestBase;
import com.miotech.kun.common.task.dao.TaskDao;
import com.miotech.kun.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.vo.TaskRunVO;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class TaskRunServiceTest extends CommonTestBase {

    @Inject
    private TaskRunService taskRunService;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskDao taskDao;

    @Test
    public void testGetTaskRunDetail() {
        TaskRun taskRun = prepareData();
        TaskRunVO existedRun = taskRunService.getTaskRunDetail(taskRun.getId());
        assertNotNull(existedRun);
        assertNotNull(existedRun.getTask());

        taskRunDao.deleteTaskRun(taskRun.getId());
    }

    private TaskRun prepareData() {
        long testId = 1L;

        Task task = Task.newBuilder().withId(22L)
                .withName("test task")
                .withDescription("")
                .withOperatorId(1L)
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .withArguments(Collections.emptyList())
                .withVariableDefs(Collections.emptyList())
                .build();
        taskDao.create(task);

        TaskRun taskRun = TaskRun.newBuilder()
                .withId(testId)
                .withTask(task)
                .withStatus(TaskRunStatus.CREATED)
                .withVariables(Collections.emptyList())
                .withDependencies(Collections.emptyList())
                .withInlets(Collections.emptyList())
                .withOutlets(Collections.emptyList())
                .withDependencies(Collections.singletonList(2L))
                .withScheduledTick(new Tick(""))
                .build();
        taskRun = taskRunDao.createTaskRun(taskRun);
        return taskRun;
    }
}