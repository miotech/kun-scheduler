package com.miotech.kun.common.task.service;

import com.google.inject.Inject;
import com.miotech.kun.common.DatabaseTestBase;
import com.miotech.kun.common.exception.EntityNotFoundException;
import com.miotech.kun.common.operator.dao.MockOperatorFactory;
import com.miotech.kun.common.operator.dao.OperatorDao;
import com.miotech.kun.common.task.dao.MockTaskFactory;
import com.miotech.kun.common.task.filter.TaskSearchFilter;
import com.miotech.kun.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class TaskServiceTest extends DatabaseTestBase {
    @Inject
    private TaskService taskService;

    @Inject
    private OperatorDao operatorDao;

    private List<Operator> insertSampleOperators() {
        List<Operator> operators = MockOperatorFactory.createOperators(10);
        operators.forEach(operator -> {
            operatorDao.create(operator);
        });
        return operators;
    }

    private Pair<Task, List<Operator>> mockOperatorsAndCreateSingleTask() {
        List<Operator> availableOperators = insertSampleOperators();
        TaskPropsVO vo = MockTaskFactory.createMockTaskPropsVO().cloneBuilder()
                .withOperatorId(availableOperators.get(0).getId())
                .build();
        Task createdTask = taskService.createTask(vo);
        return Pair.of(createdTask, availableOperators);
    }

    private Pair<List<Task>, List<Operator>> mockOperatorsAndCreateMultipleTasks(int taskNum) {
        List<Operator> availableOperators = insertSampleOperators();
        List<Task> createdTasks = new ArrayList<>();
        for (int i = 0; i < taskNum; i += 1) {
            TaskPropsVO vo = MockTaskFactory.createMockTaskPropsVO().cloneBuilder()
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
        TaskPropsVO vo = MockTaskFactory.createMockTaskPropsVO().cloneBuilder()
                .withOperatorId(preparedOperators.get(0).getId())
                .build();

        // Process
        // 2. create through service
        Task createdTask = taskService.createTask(vo);

        // Validate
        // 3. task should persist
        Optional<Task> persistedTask = taskService.fetchTaskById(createdTask.getId());
        assertTrue(persistedTask.isPresent());
    }

    @Test
    public void createTask_withNonExistOperatorId_shouldThrowException() {
        // Prepare
        // 1. generate a non-exist operator id
        Long nonExistOperatorId = WorkflowIdGenerator.nextOperatorId();

        // 2. create a valid task value object
        TaskPropsVO vo = MockTaskFactory.createMockTaskPropsVO().cloneBuilder()
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
        Optional<Task> updatedTaskOptional = taskService.fetchTaskById(createdTask.getId());
        assertTrue(updatedTaskOptional.isPresent());
        Task updatedTask = updatedTaskOptional.get();

        assertThat(updatedTask, samePropertyValuesAs(taskToUpdate));
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
        Optional<Task> taskOptional = taskService.fetchTaskById(createdTask.getId());
        assertTrue(taskOptional.isPresent());
        Task persistedTask = taskOptional.get();
        assertThat(persistedTask, samePropertyValuesAs(createdTask));
    }

    @Test
    public void partialUpdateTask_withValidProps_shouldSuccess() {
        // Prepare
        // 1. create a valid task value object
        List<Operator> existOperators = insertSampleOperators();
        TaskPropsVO vo = MockTaskFactory.createMockTaskPropsVO().cloneBuilder()
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
        Task updatedTask = taskService.fetchTaskById(createdTask.getId()).orElse(null);
        // 5. all properties except `name` should remain unchanged
        assertThat(updatedTask, samePropertyValuesAs(createdTask, "name"));
        // 6. and `name` property should be updated
        assertThat(updatedTask.getName(), is("Updated Task Name"));
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
        Optional<Task> taskOptional = taskService.fetchTaskById(createdTask.getId());
        assertTrue(taskOptional.isPresent());
        Task persistedTask = taskOptional.get();
        assertThat(persistedTask, samePropertyValuesAs(createdTask));
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
        List<Task> filteredTasks = taskService.fetchTasksByFilters(nameFilter);

        // Validate
        assertEquals(10, filteredTasks.size());
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
        Optional<Task> removedTask = taskService.fetchTaskById(createdTask.getId());
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
}
