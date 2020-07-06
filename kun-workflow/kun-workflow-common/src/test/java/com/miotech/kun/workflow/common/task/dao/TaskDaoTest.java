package com.miotech.kun.workflow.common.task.dao;

import com.google.common.collect.Lists;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.common.task.dependency.TaskDependencyFunctionProvider;
import com.miotech.kun.workflow.common.task.filter.TaskSearchFilter;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskDependency;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.junit.After;
import org.junit.Test;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.*;

public class TaskDaoTest extends DatabaseTestBase {
    @Inject
    DatabaseOperator dbOperator;

    @Inject
    TaskDao taskDao;

    @Inject
    TaskDependencyFunctionProvider dependencyFunctionProvider;

    private Clock getMockClock() {
        return Clock.fixed(Instant.parse("2020-01-01T00:00:00.00Z"), ZoneId.of("UTC"));
    }

    @After
    public void resetClock() {
        // reset global clock after each test
        DateTimeUtils.resetClock();
    }

    private List<Task> insertSampleData() {
        List<Task> created = new ArrayList<>();

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
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .build();

        created.add(taskExample);
        taskDao.create(taskExample);

        // insert more tasks
        for (int i = 0; i < 10; i += 1) {
            Task task = MockTaskFactory.createTask().cloneBuilder()
                    .withName("demo_task_" + i)
                    .withTags(Lists.newArrayList(
                            new Tag("version", String.valueOf(i % 2 + 1))
                    ))
                    .build();
            created.add(task);
            taskDao.create(task);
        }

        return created;
    }

    /**
     * @return a list of 4 mock tasks: A, B, C, D
     * Task B, Task C has upstream dependency Task A
     * Task D has upstream dependencies (Task B, Task C)
     */
    private List<Task> getSampleTasksWithDependencies() {
        Task taskA = MockTaskFactory.createTask()
                .cloneBuilder().withId(1L).withName("task-A").withDependencies(new ArrayList<>()).build();
        Task taskB = MockTaskFactory.createTask()
                .cloneBuilder().withId(2L).withName("task-B").withDependencies(Lists.newArrayList(
                        new TaskDependency(1L, 2L, dependencyFunctionProvider.from("latestTaskRun"))
                )).build();
        Task taskC = MockTaskFactory.createTask()
                .cloneBuilder().withId(3L).withName("task-C").withDependencies(Lists.newArrayList(
                        new TaskDependency(1L, 3L, dependencyFunctionProvider.from("latestTaskRun"))
                )).build();
        Task taskD = MockTaskFactory.createTask()
                .cloneBuilder().withId(4L).withName("task-D").withDependencies(Lists.newArrayList(
                        new TaskDependency(2L, 4L, dependencyFunctionProvider.from("latestTaskRun")),
                        new TaskDependency(3L, 4L, dependencyFunctionProvider.from("latestTaskRun"))
                )).build();
        return Lists.newArrayList(taskA, taskB, taskC, taskD);
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
    public void fetchByIds_empty() {
        // Prepare
        List<Task> tasks = insertSampleData();

        // process
        Map<Long, Optional<Task>> result = taskDao.fetchByIds(Lists.newArrayList());

        // verify
        assertThat(result.size(), is(0));
    }

    @Test
    public void fetchByIds_success() {
        // Prepare
        List<Task> tasks = insertSampleData();
        Task task1 = tasks.get(0);
        Task task2 = tasks.get(1);
        Task task5 = tasks.get(4);
        Task task7 = tasks.get(6);

        // process
        Map<Long, Optional<Task>> result = taskDao.fetchByIds(Lists.newArrayList(
                task1.getId(),
                task2.getId(),
                task5.getId(),
                task7.getId(),
                -1L
        ));

        // verify
        assertThat(result.size(), is(5));
        assertThat(result.get(task1.getId()).get(), sameBeanAs(task1));
        assertThat(result.get(task2.getId()).get(), sameBeanAs(task2));
        assertThat(result.get(task5.getId()).get(), sameBeanAs(task5));
        assertThat(result.get(task7.getId()).get(), sameBeanAs(task7));
        assertThat(result.get(-1L).isPresent(), is(false));
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
                .withDependencies(new ArrayList<>())
                .withTags(Lists.newArrayList(
                        new Tag("owner", "foo"),
                        new Tag("version", "1")
                ))
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
                .withDependencies(new ArrayList<>())
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .withTags(new ArrayList<>())
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
                .withDependencies(new ArrayList<>())
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .withTags(new ArrayList<>())
                .build();
        Task duplicatedTask = insertTask.cloneBuilder().build();

        // Process
        taskDao.create(insertTask);
        taskDao.create(duplicatedTask);

        // Expect exception thrown
    }

    @Test
    public void create_taskWithDuplicatedTagKeys_shouldFail() {
        // Prepare
        Task insertTask = Task.newBuilder()
                .withId(1L)
                .withName("foo")
                .withDescription("foo desc")
                .withOperatorId(1L)
                .withArguments(new ArrayList<>())
                .withVariableDefs(new ArrayList<>())
                .withDependencies(new ArrayList<>())
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                // duplicated tags
                .withTags(Lists.newArrayList(
                        new Tag("foo", "bar"),
                        new Tag("foo", "rab")
                ))
                .build();
        // Process
        try {
            taskDao.create(insertTask);
            fail();
        } catch (Exception e) {
            // Validate
            assertThat(e, instanceOf(RuntimeException.class));
            Optional<Task> taskOptional = taskDao.fetchById(1L);
            assertFalse(taskOptional.isPresent());
        }
    }

    @Test
    public void create_withValidTaskDependencies_shouldSuccess() {
        // Prepare
        List<Task> tasks = getSampleTasksWithDependencies();
        // Process
        tasks.forEach(task -> taskDao.create(task));
        // Validate
        Optional<Task> taskDOptional = taskDao.fetchById(4L);
        assertTrue(taskDOptional.isPresent());
        Task taskD = taskDOptional.get();
        assertThat(taskD.getDependencies().size(), is(2));
        assertThat(taskD.getDependencies().get(0).getUpstreamTaskId(), is(2L));
        assertThat(taskD.getDependencies().get(1).getUpstreamTaskId(), is(3L));
    }

    @Test
    public void fetchWithFilter_tasksHaveDependencies_shouldIncludeDependenciesProperly() {
        // Prepare
        List<Task> tasks = getSampleTasksWithDependencies();
        // Process
        tasks.forEach(task -> taskDao.create(task));
        List<Task> fetchedTasks = taskDao.fetchWithFilters(
                TaskSearchFilter.newBuilder().withPageNum(1).withPageSize(10).build());
        fetchedTasks.forEach(task -> {
            List<TaskDependency> deps = task.getDependencies();
            switch (task.getId().intValue()) {
                case 1:
                    assertThat(deps.size(), is(0));
                    break;
                case 2:
                case 3:
                    assertThat(deps.size(), is(1));
                    assertThat(deps.get(0).getUpstreamTaskId(), is(1L));
                    break;
                case 4:
                    assertThat(deps.size(), is(2));
                    assertThat(deps.get(0).getUpstreamTaskId(), is(2L));
                    assertThat(deps.get(1).getUpstreamTaskId(), is(3L));
                    break;
                default:
                    fail();
            }
        });
    }

    @Test
    public void fetchWithFilter_filterByTaskName_shouldReturnFilteredResultsProperly() {
        // Prepare
        List<Task> tasks = getSampleTasksWithDependencies();
        tasks.forEach(task -> taskDao.create(task));

        // Process
        List<Task> fetchedTasks = taskDao.fetchWithFilters(
                TaskSearchFilter.newBuilder().withName("task").withPageNum(1).withPageSize(10).build());

        List<Task> noMatchResults = taskDao.fetchWithFilters(
                TaskSearchFilter.newBuilder().withName("RANDOM_STRING_TO_SEARCH").withPageNum(1).withPageSize(10).build());

        // Validate
        assertThat(fetchedTasks.size(), is(4));
        assertThat(noMatchResults.size(), is(0));
    }

    @Test
    public void update_WithProperId_shouldSuccess() {
        // Prepare
        insertSampleData();

        Task task = taskDao.fetchById(1L).get();
        assertThat(task.getName(), is("example1"));

        // Process
        Task taskToBeUpdated = task.cloneBuilder()
                .withName("changedTaskName")
                .withTags(Lists.newArrayList(
                        new Tag("foo", "bar"),
                        new Tag("a", "b")
                ))
                .build();
        taskDao.update(taskToBeUpdated);

        // Validate
        Task updatedTask = taskDao.fetchById(1L).get();
        // Validate properties except tags, since ordinals are not guaranteed to be same
        assertThat(updatedTask, samePropertyValuesAs(taskToBeUpdated, "tags"));
        // Convert tags to map and compare
        assertThat(updatedTask.getTagsMap(), sameBeanAs(taskToBeUpdated.getTagsMap()));
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
    public void delete_TaskWithDependencies_ShouldDeleteRelations() {
        // Prepare
        List<Task> tasks = getSampleTasksWithDependencies();
        tasks.forEach(task -> taskDao.create(task));

        // Process
        taskDao.deleteById(3L);

        // Validate
        Optional<Task> taskCOptional = taskDao.fetchById(3L);
        assertFalse(taskCOptional.isPresent());

        Task taskD = taskDao.fetchById(4L).get();
        // Originally task D has 2 dependencies. After deletion, dependency of task C should be removed
        assertThat(taskD.getDependencies().size(), is(1));
        assertThat(taskD.getDependencies().get(0).getUpstreamTaskId(), is(2L));
    }

    @Test
    public void fetchScheduledTaskAtTick_ShouldWork() {
        // Prepare
        DateTimeUtils.setClock(getMockClock());

        // 1. A prepared task has a scheduled execution on 10:15 everyday.
        insertSampleData();

        // 2. We prepare 3 ticks: on 8:00 am, on 10:15, on 11:00.
        OffsetDateTime mockNow = DateTimeUtils.now();

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
        DateTimeUtils.setClock(getMockClock());

        // 1. Create a task with scheduled execution on 10:15 everyday.
        insertSampleData();

        OffsetDateTime mockNow = DateTimeUtils.now();
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
        taskDao.update(taskToUpdate);

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
        DateTimeUtils.setClock(getMockClock());

        // 1. Create a task with scheduled execution on 10:15 everyday.
        insertSampleData();

        OffsetDateTime mockNow = DateTimeUtils.now();
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

    @Test
    public void fetchUpstreamTasks_withGivenDistance_shouldWork() {
        // Prepare
        List<Task> tasks = getSampleTasksWithDependencies();
        tasks.forEach(task -> taskDao.create(task));

        Task taskA = tasks.get(0);
        Task taskD = tasks.get(3);

        // Process
        List<Task> upstreamTasksOfTaskAInDistance1 = taskDao.fetchUpstreamTasks(taskA);
        List<Task> upstreamTasksOfTaskDInDistance1 = taskDao.fetchUpstreamTasks(taskD);
        List<Task> upstreamTasksOfTaskDInDistance2 = taskDao.fetchUpstreamTasks(taskD, 2);
        List<Task> upstreamTasksOfTaskDInDistance2AndItself = taskDao.fetchUpstreamTasks(taskD, 2, true);
        List<Task> upstreamTasksOfTaskDInDistance3AndItself = taskDao.fetchUpstreamTasks(taskD, 3, true);

        // Validate
        assertThat(upstreamTasksOfTaskAInDistance1.size(), is(0)); // A is the root of DAG
        assertThat(upstreamTasksOfTaskDInDistance1.size(), is(2)); // Task B and Task C
        assertThat(upstreamTasksOfTaskDInDistance2.size(), is(3)); // Task A, Task B and Task C
        assertThat(upstreamTasksOfTaskDInDistance2AndItself.size(), is(4));  // Task A, Task B, Task C and Task D
        assertThat(upstreamTasksOfTaskDInDistance3AndItself.size(), is(4));  // Task A, Task B, Task C and Task D
    }

    @Test
    public void fetchDownstreamTasks_withGivenDistance_shouldWork() {
        // Prepare
        List<Task> tasks = getSampleTasksWithDependencies();
        tasks.forEach(task -> taskDao.create(task));

        Task taskA = tasks.get(0);
        Task taskD = tasks.get(3);

        // Process
        List<Task> downstreamTasksOfTaskDInDistance1 = taskDao.fetchDownstreamTasks(taskD);
        List<Task> downstreamTasksOfTaskAInDistance1 = taskDao.fetchDownstreamTasks(taskA);
        List<Task> downstreamTasksOfTaskAInDistance2 = taskDao.fetchDownstreamTasks(taskA, 2);
        List<Task> downstreamTasksOfTaskAInDistance2AndItself = taskDao.fetchDownstreamTasks(taskA, 2, true);

        // Validate
        assertThat(downstreamTasksOfTaskDInDistance1.size(), is(0)); // A is the root of DAG
        assertThat(downstreamTasksOfTaskAInDistance1.size(), is(2)); // Task B and Task C
        assertThat(downstreamTasksOfTaskAInDistance2.size(), is(3)); // Task B, Task C and Task D
        assertThat(downstreamTasksOfTaskAInDistance2AndItself.size(), is(4));  // Task A, Task B, Task C and Task D
    }

    @Test
    public void fetchUpOrDownstreamTasks_withIllegalArgument_shouldThrowExceptions() {
        // Prepare
        List<Task> tasks = getSampleTasksWithDependencies();
        tasks.forEach(task -> taskDao.create(task));
        Task taskD = tasks.get(3);

        // Process & Validate
        // 1. should throw NullPointerException when source task is null
        try {
            taskDao.fetchUpstreamTasks(null);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }

        // 2. should throw IllegalArgumentException when distance is 0 or negative
        try {
            taskDao.fetchUpstreamTasks(taskD, 0);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }

        try {
            taskDao.fetchUpstreamTasks(taskD, -1);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void fetchTotalCount_shouldReturnTotalRecordsNum() {
        // Prepare
        List<Task> insertedTasks = insertSampleData();
        // Process
        int count = taskDao.fetchTotalCount();
        // Validate
        assertEquals(insertedTasks.size(), count);
    }

    @Test
    public void fetchTotalCountWithFilters_shouldReturnTotalRecordsNum() {
        // Prepare
        List<Task> insertedTasks = insertSampleData();
        // Process
        TaskSearchFilter testFilter1 = TaskSearchFilter.newBuilder()
                .withPageNum(1)
                // pagination should not affect
                .withPageSize(1)
                .withName("demo_task")
                .build();
        TaskSearchFilter testFilter2 = TaskSearchFilter.newBuilder()
                .withPageNum(1)
                // pagination should not affect
                .withPageSize(2)
                .withTags(Lists.newArrayList(
                        new Tag("version", "1")
                ))
                .build();

        int count1 = taskDao.fetchTotalCountWithFilters(testFilter1);
        int count2 = taskDao.fetchTotalCountWithFilters(testFilter2);

        assertEquals(insertedTasks.size() - 1, count1);
        assertEquals((insertedTasks.size() - 1) / 2, count2);
    }
}
