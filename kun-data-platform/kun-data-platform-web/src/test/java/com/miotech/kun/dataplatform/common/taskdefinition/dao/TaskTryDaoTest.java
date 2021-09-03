package com.miotech.kun.dataplatform.common.taskdefinition.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskTry;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.*;

public class TaskTryDaoTest extends AppTestBase {

    @Autowired
    private TaskTryDao taskTryDao;

    @Test
    public void testCreate_TaskTry_ok() {
        TaskTry taskTry = MockTaskDefinitionFactory.createTaskTry();
        taskTryDao.create(taskTry);

        TaskTry fetched = taskTryDao.fetchById(taskTry.getId()).get();
        assertThat(fetched, sameBeanAs(taskTry));
    }

    @Test
    public void testFetch_byTaskRunId_ok() {
        TaskTry taskTry = MockTaskDefinitionFactory.createTaskTry();
        taskTryDao.create(taskTry);

        Optional<TaskTry> taskTryOpt = taskTryDao.fetchByTaskRunId(taskTry.getWorkflowTaskRunId());
        assertTrue(taskTryOpt.isPresent());
        TaskTry fetched = taskTryOpt.get();
        assertThat(fetched, sameBeanAs(taskTry));
    }

    @Test
    public void testFetch_byTaskRunId_notFound() {
        TaskTry taskTry = MockTaskDefinitionFactory.createTaskTry();

        Optional<TaskTry> taskTryOpt = taskTryDao.fetchByTaskRunId(taskTry.getWorkflowTaskRunId());
        assertFalse(taskTryOpt.isPresent());
    }

}