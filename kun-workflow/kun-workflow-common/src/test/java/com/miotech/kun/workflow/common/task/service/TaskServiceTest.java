package com.miotech.kun.workflow.common.task.service;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.graph.DirectTaskGraph;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.task.filter.TaskSearchFilter;
import com.miotech.kun.workflow.common.task.vo.PaginationVO;
import com.miotech.kun.workflow.common.task.vo.RunTaskVO;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.TaskRunEnv;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskFactory;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class TaskServiceTest extends DatabaseTestBase {
    @Inject
    private TaskService taskService;

    @Inject
    private TaskDao taskDao;

    @Inject
    private OperatorDao operatorDao;

    private Scheduler scheduler = mock(Scheduler.class);

    private List<Operator> insertSampleOperators() {
        List<Operator> operators = MockOperatorFactory.createOperators(10);
        operators.forEach(operator -> {
            operatorDao.create(operator);
        });
        return operators;
    }

    private Pair<Task, List<Operator>> mockOperatorsAndCreateSingleTask() {
        List<Operator> availableOperators = insertSampleOperators();
        TaskPropsVO vo = MockTaskFactory.createTaskPropsVO().cloneBuilder()
                .withOperatorId(availableOperators.get(0).getId())
                .build();
        Task createdTask = taskService.createTask(vo);
        return Pair.of(createdTask, availableOperators);
    }

    private Pair<List<Task>, List<Operator>> mockOperatorsAndCreateMultipleTasks(int taskNum) {
        List<Operator> availableOperators = insertSampleOperators();
        List<Task> createdTasks = new ArrayList<>();
        for (int i = 0; i < taskNum; i += 1) {
            TaskPropsVO vo = MockTaskFactory.createTaskPropsVO().cloneBuilder()
                    .withOperatorId(availableOperators.get(i % availableOperators.size()).getId())
                    .build();
            Task createdTask = taskService.createTask(vo);
            createdTasks.add(createdTask);
        }
        return Pair.of(createdTasks, availableOperators);
    }

    @Test
    public void createTask_withValidVOAndOperators_shouldSuccess() {
        // Prepare
        List<Operator> preparedOperators = insertSampleOperators();

        // 1. create a valid task value object
        TaskPropsVO vo = MockTaskFactory.createTaskPropsVO().cloneBuilder()
                .withOperatorId(preparedOperators.get(0).getId())
                .build();

        // Process
        // 2. create through service
        Task createdTask = taskService.createTask(vo);

        // Validate
        // 3. task should persist
        Task persistedTask = taskService.find(createdTask.getId());
        assertTrue(persistedTask.getId() > 0);
    }

    @Test
    public void createTask_withNonExistOperatorId_shouldThrowException() {
        // Prepare
        // 1. generate a non-exist operator id
        Long nonExistOperatorId = WorkflowIdGenerator.nextOperatorId();

        // 2. create a valid task value object
        TaskPropsVO vo = MockTaskFactory.createTaskPropsVO().cloneBuilder()
                .withOperatorId(nonExistOperatorId)
                .build();

        // Process & Validate
        try {
            Task createdTask = taskService.createTask(vo);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(EntityNotFoundException.class));
        }
    }

    @Test
    public void updateTask_withValidProps_shouldSuccess() {
        // Prepare
        // 1. create a valid task value object
        Pair<Task, List<Operator>> preparedEntities = mockOperatorsAndCreateSingleTask();
        Task createdTask = preparedEntities.getLeft();
        List<Operator> availableOperators = preparedEntities.getRight();

        // 2. produce task vo with overwritten properties
        Task taskToUpdate = createdTask.cloneBuilder()
                .withName("Updated Task Name")
                .withDescription("Lorem ipsum dolor sit amet")
                .withOperatorId(availableOperators.get(1).getId())
                .build();

        // Process
        // 3. perform full update
        taskService.fullUpdateTask(taskToUpdate);

        // Validate
        // 4. fetch updated entity and check
        Task updatedTask = taskService.find(createdTask.getId());
        assertThat(updatedTask, sameBeanAs(taskToUpdate));
    }

    @Test
    public void updateTask_withInvalidProps_shouldThrowException() {
        // Prepare
        Pair<Task, List<Operator>> preparedEntities = mockOperatorsAndCreateSingleTask();
        Task createdTask = preparedEntities.getLeft();

        // Case 1: should throw exception when update with empty object
        try {
            taskService.fullUpdateTask(null);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }

        // Case 2: should throw exception when update with non-exist task id
        try {
            taskService.fullUpdateTask(createdTask
                    .cloneBuilder()
                    // Generate a non-exist task id
                    .withId(WorkflowIdGenerator.nextTaskId())
                    .build()
            );
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(EntityNotFoundException.class));
        }

        // Case 3: should throw exception when update with non-exist operator id
        try {
            taskService.fullUpdateTask(createdTask
                    .cloneBuilder()
                    // Generate a non-exist operator id
                    .withOperatorId(WorkflowIdGenerator.nextOperatorId())
                    .build()
            );
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(EntityNotFoundException.class));
        }

        // After all invalid operations, persisted task entity should remain unchanged
        Task persistedTask = taskService.find(createdTask.getId());
        assertThat(persistedTask, sameBeanAs(createdTask));
    }

    @Test
    public void partialUpdateTask_withValidProps_shouldSuccess() {
        // Prepare
        // 1. create a valid task value object
        List<Operator> existOperators = insertSampleOperators();
        TaskPropsVO vo = MockTaskFactory.createTaskPropsVO().cloneBuilder()
                .withOperatorId(existOperators.get(0).getId())
                .build();
        Task createdTask = taskService.createTask(vo);

        // 2. produce a task value object with properties initialized partially
        TaskPropsVO updateVo = TaskPropsVO.newBuilder()
                .withName("Updated Task Name")
                .build();

        // Process
        // 3. update task partially
        taskService.partialUpdateTask(createdTask.getId(), updateVo);

        // Validate
        // 4. Fetch updated task
        Task updatedTask = taskService.find(createdTask.getId());
        // 5. and `name` property should be updated
        assertThat(updatedTask, samePropertyValuesAs(createdTask,"config", "name"));
        assertThat(updatedTask.getConfig().size(), is(createdTask.getConfig().size()));
        // 6. and `name` property should be updated
        assertThat(updatedTask.getName(), is("Updated Task Name"));
        // 6. all properties except `name` should remain unchanged
        // TODO: improve `sameBeanAs()` to accept ignored fields
        createdTask = createdTask.cloneBuilder().withName(updatedTask.getName()).build();
        assertThat(updatedTask, sameBeanAs(createdTask));
    }

    @Test
    public void partialIUpdateTask_withInvalidProps_shouldThrowException() {
        // Prepare
        Pair<Task, List<Operator>> preparedEntities = mockOperatorsAndCreateSingleTask();
        Task createdTask = preparedEntities.getLeft();

        // Case 1: should throw exception when update with empty object
        try {
            taskService.partialUpdateTask(createdTask.getId(), null);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(NullPointerException.class));
        }

        // Case 2: should throw exception when update with non-exist operator id
        TaskPropsVO voWithNonExistOperatorId =TaskPropsVO.newBuilder()
                .withOperatorId(1234L)
                .build();
        try {
            taskService.partialUpdateTask(createdTask.getId(), voWithNonExistOperatorId);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(EntityNotFoundException.class));
        }

        // After all invalid operations, persisted task entity should remain unchanged
        Task persistedTask = taskService.find(createdTask.getId());
        assertThat(persistedTask, sameBeanAs(createdTask));
    }

    @Test
    public void fetchTasksByFilters_withNameFilter_shouldReturnFilteredTasks() {
        // Prepare
        // 1. create a list of 100 tasks and persist
        Pair<List<Task>, List<Operator>> preparedEntities = mockOperatorsAndCreateMultipleTasks(100);
        List<Task> preparedTasks = preparedEntities.getLeft();
        // 2. Update 10 task entities with specific name prefix
        for (int i = 5; i < 15; i += 1) {
            Task taskToBeUpdate = preparedTasks.get(i);
            taskService.fullUpdateTask(taskToBeUpdate.cloneBuilder()
                    .withName("name_prefix_" + i)
                    .build()
            );
        }
        // 3. create a name filter
        TaskSearchFilter nameFilter = TaskSearchFilter.newBuilder()
                .withName("name_prefix")
                .withPageNum(1)
                .withPageSize(100)
                .build();

        // Process
        // 4. fetch all tasks by name filter
        PaginationVO<Task> filteredTasks = taskService.fetchTasksByFilters(nameFilter);

        // Validate
        assertEquals(10, filteredTasks.getRecords().size());
    }

    @Test
    public void fetchTasksByFilters_withTagsFilter_shouldReturnFilteredTasks() {
        // Prepare
        // 1. create a list of 100 tasks and persist
        Pair<List<Task>, List<Operator>> preparedEntities = mockOperatorsAndCreateMultipleTasks(100);
        List<Task> preparedTasks = preparedEntities.getLeft();
        // 2. Update 10 task entities with specific name prefix
        for (int i = 5; i < 15; i += 1) {
            Task taskToBeUpdate = preparedTasks.get(i);
            taskService.fullUpdateTask(taskToBeUpdate.cloneBuilder()
                    .withName("name_prefix_" + i)
                    .withTags(Lists.newArrayList(
                            new Tag("version", String.valueOf(i % 2 + 1)),
                            new Tag("priority", String.valueOf(i % 3 + 1)),
                            new Tag("owner", "foo")
                    ))
                    .build()
            );
        }
        // 3. create a tag filter
        TaskSearchFilter versionTagFilter = TaskSearchFilter.newBuilder()
                .withTags(Lists.newArrayList(
                        new Tag("version", "1")
                ))
                .withPageNum(1).withPageSize(100).build();

        TaskSearchFilter ownerTagFilter = TaskSearchFilter.newBuilder()
                .withTags(Lists.newArrayList(
                        new Tag("owner", "foo")
                ))
                .withPageNum(1).withPageSize(100).build();

        TaskSearchFilter multipleTagsFilter = TaskSearchFilter.newBuilder()
                .withTags(Lists.newArrayList(
                        new Tag("version", "2"),
                        new Tag("priority", "3"),
                        new Tag("owner", "foo")
                ))
                .withPageNum(1).withPageSize(100).build();

        TaskSearchFilter TagAndNameFilter = TaskSearchFilter.newBuilder()
                .withName("name_prefix_10")
                .withTags(Lists.newArrayList(
                        new Tag("owner", "foo")
                ))
                .withPageNum(1).withPageSize(100).build();


        // Process
        // 4. fetch all tasks by tag filter
        PaginationVO<Task> filteredTasksWithVersionTag = taskService.fetchTasksByFilters(versionTagFilter);
        PaginationVO<Task> filteredTasksWithOwnerTag = taskService.fetchTasksByFilters(ownerTagFilter);
        PaginationVO<Task> filteredTasksWithMultipleTags = taskService.fetchTasksByFilters(multipleTagsFilter);
        PaginationVO<Task> filteredTasksWithJointConditions = taskService.fetchTasksByFilters(TagAndNameFilter);

        // Validate
        assertEquals(5, filteredTasksWithVersionTag.getRecords().size());
        assertEquals(10, filteredTasksWithOwnerTag.getRecords().size());
        assertEquals(2, filteredTasksWithMultipleTags.getRecords().size());
        assertEquals(1, filteredTasksWithJointConditions.getRecords().size());
    }

    @Test
    public void fetchTasksByFilters_withInvalidPageNumOrPageSize_shouldThrowInvalidArgumentException() {
        // Prepare
        // 1. create a list of 100 tasks and persist
        mockOperatorsAndCreateMultipleTasks(100);

        // 2. create a filter with pageNum and pageSize not initialized
        TaskSearchFilter invalidFilter = TaskSearchFilter.newBuilder()
                .build();

        try {
            taskService.fetchTasksByFilters(invalidFilter);
            fail();
        } catch (Exception e) {
            // 3. should throw IllegalArgumentException
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void deleteTask_withExistTask_shouldSuccess() {
        // Prepare
        Pair<Task, List<Operator>> preparedEntities = mockOperatorsAndCreateSingleTask();
        Task createdTask = preparedEntities.getLeft();

        // Process
        taskService.deleteTask(createdTask);

        // Validate
        Optional<Task> removedTask = taskDao.fetchById(createdTask.getId());
        assertFalse(removedTask.isPresent());
    }

    @Test
    public void deleteTask_withNonExistTask_shouldThrowEntityNotFoundException() {
        // Prepare
        Pair<Task, List<Operator>> preparedEntities = mockOperatorsAndCreateSingleTask();
        Task createdTask = preparedEntities.getLeft();

        // Process
        taskService.deleteTask(createdTask);

        try {
            taskService.deleteTask(createdTask);
            fail();
        } catch (Exception e) {
            // Validate
            assertThat(e, instanceOf(EntityNotFoundException.class));
        }
    }

    @Test
    public void runTask_task_does_not_exist() {
        RunTaskVO vo = new RunTaskVO();
        vo.setTaskId(1L);

        // process
        try {
            taskService.runTasks(Lists.newArrayList(vo));
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void runTask_single_task_no_variables() {
        // prepare
        Task task = MockTaskFactory.createTask();
        taskDao.create(task);

        RunTaskVO vo = new RunTaskVO();
        vo.setTaskId(task.getId());

        ArgumentCaptor<TaskGraph> captor1 = ArgumentCaptor.forClass(TaskGraph.class);
        ArgumentCaptor<TaskRunEnv> captor2 = ArgumentCaptor.forClass(TaskRunEnv.class);

        // process
        taskService.runTasks(Lists.newArrayList(vo));
        verify(scheduler, times(1))
                .run(captor1.capture(), captor2.capture());
        DirectTaskGraph graph = (DirectTaskGraph) captor1.getValue();
        TaskRunEnv context = captor2.getValue();

        // verify
        assertThat(graph.getTasks().get(0), sameBeanAs(task));
        assertThat(context.getConfig(task.getId()).size(), is(0));
    }

    @Test
    public void runTask_single_task_with_variables() {

    }

    @Test
    public void runTask_multiple_tasks() {

    }
}
