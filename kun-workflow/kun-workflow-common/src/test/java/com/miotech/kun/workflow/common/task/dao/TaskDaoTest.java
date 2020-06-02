package com.miotech.kun.workflow.common.task.dao;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.common.task.filter.TaskSearchFilter;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.junit.Test;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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

    private Clock getMockClock() {
        return Clock.fixed(Instant.parse("2020-01-01T00:00:00.00Z"), ZoneId.of("UTC"));
    }

    private void insertSampleData() {
        // mock up a system clock
        Clock clock = getMockClock();

        // insert task with specific id
        List<Param> args = Lists.newArrayList(
                Param.newBuilder().withName("VERSION").withValue("1.0").build()
        );

        List<Variable> variableDefs = Lists.newArrayList(
                Variable.newBuilder().withKey("PATH").withDefaultValue("/usr/bin").build()
        );

        Task taskExample = Task.newBuilder()
                .withId(1L)
                .withName("example1")
                .withDescription("example1_desc")
                .withArguments(args)
                .withVariableDefs(variableDefs)
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 15 10 * * ?"))
                .withOperatorId(1L)
                .build();

        taskDao.create(taskExample, clock);

        // insert more tasks
        for (int i = 0; i < 10; i += 1) {
            Task task = MockTaskFactory.createTask();
            taskDao.create(task);
        }
    }

    @Test
    public void fetch_withProperFilter_shouldSuccess() {
        insertSampleData();

        // 11 records in total
        List<Task> results = taskDao.fetchWithFilters(TaskSearchFilter
                .newBuilder()
                .withPageNum(2)
                .withPageSize(6)
                .build());

        // should return all of 3 records
        assertThat(results.size(), is(5));
    }

    @Test
    public void fetchById_withProperId_shouldSuccess() {
        // Prepare
        insertSampleData();

        // Process
        Optional<Task> taskOptional = taskDao.fetchById(1L);

        // Validate
        assertTrue(taskOptional.isPresent());
        Task task = taskOptional.get();
        assertThat(task.getName(), is("example1"));
        assertThat(task.getVariableDefs().get(0).getKey(), is("PATH"));
        assertThat(task.getVariableDefs().get(0).getDefaultValue(), is("/usr/bin"));
        assertThat(task.getScheduleConf().getCronExpr(), is("0 15 10 * * ?"));
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
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .build();

        // Process
        taskDao.create(insertTask);

        // Validate
        Optional<Task> persistedTaskOptional = taskDao.fetchById(id);
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

        Task task = taskDao.fetchById(1L).get();
        assertThat(task.getName(), is("example1"));

        // Process
        Task taskToBeUpdated = task.cloneBuilder().withName("changedTaskName").build();
        taskDao.update(taskToBeUpdated);

        // Validate
        Task updatedTask = taskDao.fetchById(1L).get();
        assertThat(updatedTask, samePropertyValuesAs(taskToBeUpdated));
    }

    @Test
    public void delete_WithExistedId_ShouldWork() {
        // Prepare
        insertSampleData();
        Optional<Task> taskToBeDeleteOptional = taskDao.fetchById(1L);
        assertTrue(taskToBeDeleteOptional.isPresent());

        // Process
        taskDao.deleteById(1L);

        // Validate
        Optional<Task> taskDeletedOptional = taskDao.fetchById(1L);
        assertFalse(taskDeletedOptional.isPresent());
    }

    @Test
    public void fetchScheduledTaskAtTick_ShouldWork() {
        // Prepare
        // 1. A prepared task has a scheduled execution on 10:15 everyday.
        insertSampleData();

        // 2. We prepare 3 ticks: on 8:00 am, on 10:15, on 11:00.
        OffsetDateTime mockNow = OffsetDateTime.now(getMockClock());

        OffsetDateTime time0800 = mockNow.withHour(8).withMinute(0).withSecond(0);
        OffsetDateTime time1015 = mockNow.withHour(10).withMinute(15).withSecond(0);
        OffsetDateTime time1100 = mockNow.withHour(11).withMinute(0).withSecond(0);

        Tick preExecutionTick = new Tick(time0800);
        Tick onExecutionTick = new Tick(time1015);
        Tick postExecutionTick = new Tick(time1100);

        // Process
        // 3. fetch list of tasks from dao that scheduled before 08:00 (expect to be empty)
        List<Task> tasksToExecuteOn0800 = taskDao.fetchScheduledTaskAtTick(preExecutionTick);

        // 4. fetch list of tasks from dao that scheduled on 10:15
        List<Task> tasksToExecuteOn1015 = taskDao.fetchScheduledTaskAtTick(onExecutionTick);

        // 5.  fetch list of tasks from dao that scheduled on 10:15
        List<Task> tasksToExecuteOn1100 = taskDao.fetchScheduledTaskAtTick(postExecutionTick);

        // Validate
        assertThat(tasksToExecuteOn0800.size(), is(0));
        assertThat(tasksToExecuteOn1015.size(), is(1));
        assertThat(tasksToExecuteOn1015.get(0).getId(), is(1L));
        assertThat(tasksToExecuteOn1100.size(), is(1));
        assertThat(tasksToExecuteOn1100.get(0).getId(), is(1L));
    }

    @Test
    public void updateTask_withScheduledConfUpdated_shouldReinsertTickMapping() {
        // Prepare
        // 1. Create a task with scheduled execution on 10:15 everyday.
        insertSampleData();

        OffsetDateTime mockNow = OffsetDateTime.now(getMockClock());
        OffsetDateTime time1015 = mockNow.withHour(10).withMinute(15).withSecond(0);
        OffsetDateTime time1115 = mockNow.withHour(11).withMinute(15).withSecond(0);

        List<Task> tasksToExecuteOn1015 = taskDao.fetchScheduledTaskAtTick(new Tick(time1015));
        List<Task> tasksToExecuteOn1115 = taskDao.fetchScheduledTaskAtTick(new Tick(time1115));

        assertThat(tasksToExecuteOn1015.size(), is(1));
        assertThat(tasksToExecuteOn1015.get(0).getId(), is(1L));
        assertThat(tasksToExecuteOn1115.size(), is(1));
        assertThat(tasksToExecuteOn1115.get(0).getId(), is(1L));

        // Process
        // 2. We update the task to execute on 11:00 instead of 10:15 everyday
        Task taskToUpdate = tasksToExecuteOn1015.get(0).cloneBuilder().withScheduleConf(
                new ScheduleConf(ScheduleType.SCHEDULED, "0 0 11 * * ?")
        ).build();
        taskDao.update(taskToUpdate, getMockClock());

        // Validate
        List<Task> tasksToExecuteOn1015AfterUpdate = taskDao.fetchScheduledTaskAtTick(new Tick(time1015));
        List<Task> tasksToExecuteOn1115AfterUpdate = taskDao.fetchScheduledTaskAtTick(new Tick(time1115));

        assertThat(tasksToExecuteOn1015AfterUpdate.size(), is(0));
        assertThat(tasksToExecuteOn1115AfterUpdate.size(), is(1));
        assertThat(tasksToExecuteOn1115AfterUpdate.get(0).getId(), is(1L));
    }

    @Test
    public void deleteTaskById_withScheduledConfAssigned_shouldRemoveTickMapping() {
        // Prepare
        // 1. Create a task with scheduled execution on 10:15 everyday.
        insertSampleData();

        OffsetDateTime mockNow = OffsetDateTime.now(getMockClock());
        OffsetDateTime time1015 = mockNow.withHour(10).withMinute(15).withSecond(0);
        List<Task> tasksToExecuteOn1015 = taskDao.fetchScheduledTaskAtTick(new Tick(time1015));

        assertThat(tasksToExecuteOn1015.size(), is(1));
        assertThat(tasksToExecuteOn1015.get(0).getId(), is(1L));

        // Process
        taskDao.deleteById(1L);

        // Validate
        List<Task> tasksToExecuteOn1015AfterDelete = taskDao.fetchScheduledTaskAtTick(new Tick(time1015));
        assertThat(tasksToExecuteOn1015AfterDelete.size(), is(0));
    }
}
