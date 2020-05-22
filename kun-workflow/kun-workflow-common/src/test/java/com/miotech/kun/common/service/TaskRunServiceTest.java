package com.miotech.kun.common.service;

import com.google.inject.Inject;
import com.miotech.kun.common.CommonTestBase;
import com.miotech.kun.common.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.vo.TaskRunVO;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class TaskRunServiceTest extends CommonTestBase {

    @Inject
    private TaskRunService taskRunService;

    @Inject
    private TaskRunDao taskRunDao;

    @Test
    public void testGetTaskRunDetail() {
        TaskRun taskRun = prepareData();
        TaskRunVO existedRun = taskRunService.getTaskRunDetail(taskRun.getId());
        assertNotNull(existedRun);
    }

    private TaskRun prepareData() {
        long testId = 1L;

        Task task = Task.newBuilder().id("")
                .params(Collections.emptyList())
                .variables(Collections.emptyList())
                .build();
        TaskRun taskRun = TaskRun.newBuilder()
                .withId(testId)
                .withTask(task)
                .withVariables(Collections.emptyList())
                .withDependencies(Collections.emptyList())
                .withInlets(Collections.emptyList())
                .withOutlets(Collections.emptyList())
                .withScheduledTick(new Tick(""))
                .build();
        taskRun = taskRunDao.save(taskRun);
        return taskRun;
    }
}