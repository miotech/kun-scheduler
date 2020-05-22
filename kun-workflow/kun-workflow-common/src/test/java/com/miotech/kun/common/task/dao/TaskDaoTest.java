package com.miotech.kun.common.task.dao;

import com.miotech.kun.common.DatabaseTestBase;
import com.miotech.kun.common.task.filter.TaskSearchFilter;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TaskDaoTest extends DatabaseTestBase {
    @Inject
    DatabaseOperator dbOperator;

    @Inject
    TaskDao taskDao;

    private void insertSampleData() {
        dbOperator.batch("INSERT INTO kun_wf_task (id, name, description, operator_id, arguments, variable_defs, schedule) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);",
                new Object[][]{
                        {1L, "example1", "example1_desc", 1L, "[]", "[{\"key\": \"PATH\", \"defaultValue\": \"/usr/bin\"}]", "{}"},
                        {2L, "example2", "example2_desc", 1L, "[]", "[]", "{}"},
                        {3L, "example3", "example3_desc", 1L, "[]", "[]", "{}"}
                }
        );

    }

    @Test
    public void search_withProperFilter_shouldSuccess() {
        insertSampleData();

        List<Task> results = taskDao.getList(TaskSearchFilter
                .newBuilder()
                .withPageNum(1)
                .withPageSize(10)
                .build());

        // should return all of 3 records
        assertThat(results.size(), is(3));
    }

    @Test
    public void getById_withProperId_shouldSuccess() {
        // Prepare
        insertSampleData();

        // Process
        Optional<Task> taskOptional = taskDao.getById(1L);

        // Validate
        assertTrue(taskOptional.isPresent());
        Task task = taskOptional.get();
        assertThat(task.getName(), is("example1"));
        assertThat(task.getVariableDefs().get(0).getKey(), is("PATH"));
        assertThat(task.getVariableDefs().get(0).getDefaultValue(), is("/usr/bin"));
    }

    @Test
    public void create_WithProperId_shouldSuccess() {
        // Prepare
        Long id = WorkflowIdGenerator.nextTaskId();
        Task insertTask = Task.newBuilder()
                .withId(id)
                .withName("foo")
                .withDescription("foo desc")
                .withOperatorId(1L)
                .withArguments(new ArrayList<>())
                .withVariableDefs(new ArrayList<>())
                .build();

        // Process
        taskDao.create(insertTask);

        // Validate
        Optional<Task> persistedTaskOptional = taskDao.getById(id);
        assertTrue(persistedTaskOptional.isPresent());

        Task persistedTask = persistedTaskOptional.get();
        assertThat(persistedTask, samePropertyValuesAs(insertTask));
    }

    @Test(expected = RuntimeException.class)
    public void create_withEmptyId_shouldThrowException() {
        // Prepare
        Task insertTask = Task.newBuilder()
                .withName("foo")
                .withDescription("foo desc")
                .withOperatorId(1L)
                .withArguments(new ArrayList<>())
                .withVariableDefs(new ArrayList<>())
                .build();

        // Process
        taskDao.create(insertTask);

        // Expect exception thrown
    }

    @Test(expected = RuntimeException.class)
    public void create_withDuplicatedId_shouldThrowException() {
        // Prepare
        Task insertTask = Task.newBuilder()
                .withId(1L)
                .withName("foo")
                .withDescription("foo desc")
                .withOperatorId(1L)
                .withArguments(new ArrayList<>())
                .withVariableDefs(new ArrayList<>())
                .build();
        Task duplicatedTask = insertTask.cloneBuilder().build();

        // Process
        taskDao.create(insertTask);
        taskDao.create(duplicatedTask);

        // Expect exception thrown
    }

    @Test
    public void update_WithProperId_shouldSuccess() {
        // Prepare
        insertSampleData();

        Task task = taskDao.getById(1L).get();
        assertThat(task.getName(), is("example1"));

        // Process
        Task taskToBeUpdated = task.cloneBuilder().withName("changedTaskName").build();
        taskDao.updateById(1L, taskToBeUpdated);

        // Validate
        Task updatedTask = taskDao.getById(1L).get();
        assertThat(updatedTask, samePropertyValuesAs(taskToBeUpdated));
    }

    @Test
    public void delete_WithExistedId_ShouldWork() {
        // Prepare
        insertSampleData();
        Optional<Task> taskToBeDeleteOptional = taskDao.getById(3L);
        assertTrue(taskToBeDeleteOptional.isPresent());

        // Process
        taskDao.deleteById(3L);

        // Validate
        Optional<Task> taskDeletedOptional = taskDao.getById(3L);
        assertFalse(taskDeletedOptional.isPresent());
    }
}
