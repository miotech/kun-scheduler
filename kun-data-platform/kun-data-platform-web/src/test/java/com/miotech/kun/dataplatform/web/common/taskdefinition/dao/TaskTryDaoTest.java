package com.miotech.kun.dataplatform.web.common.taskdefinition.dao;

import com.miotech.kun.dataplatform.AppTestBase;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskTry;
import com.miotech.kun.dataplatform.mocking.MockTaskDefinitionFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.*;
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

    @Test
    public void fetchByIds_success() {
        TaskTry taskTry1 = MockTaskDefinitionFactory.createTaskTry();
        TaskTry taskTry2 = MockTaskDefinitionFactory.createTaskTry();
        taskTryDao.create(taskTry1);
        taskTryDao.create(taskTry2);
        List<Long> taskTryIdList = Arrays.asList(taskTry1.getId(), taskTry2.getId());
        List<TaskTry> taskTryList = taskTryDao.fetchByIds(taskTryIdList);
        assertThat(taskTryList.size(), is(2));
        assertThat(new HashSet<>(taskTryIdList),
                is(new HashSet<>(taskTryList.stream().map(TaskTry::getId).collect(Collectors.toList()))));

    }

    @Test
    public void fetchByIds_notFound() {
        TaskTry taskTry1 = MockTaskDefinitionFactory.createTaskTry();
        TaskTry taskTry2 = MockTaskDefinitionFactory.createTaskTry();

        List<Long> taskTryIdList = Arrays.asList(taskTry1.getId(), taskTry2.getId());
        List<TaskTry> taskTryList = taskTryDao.fetchByIds(taskTryIdList);
        assertThat(taskTryList.size(), is(0));

    }

}