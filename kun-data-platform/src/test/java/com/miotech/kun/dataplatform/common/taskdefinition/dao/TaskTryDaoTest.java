package com.miotech.kun.dataplatform.common.taskdefinition.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskTry;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertThat;

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
}