package com.miotech.kun.workflow.common.task.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.exception.NameConflictException;
import com.miotech.kun.workflow.common.graph.DirectTaskGraph;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.task.filter.TaskSearchFilter;
import com.miotech.kun.workflow.common.task.vo.RunTaskVO;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.task.RunTaskContext;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class TaskService {
    @Inject
    private TaskDao taskDao;

    @Inject
    private OperatorDao operatorDao;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private Scheduler scheduler;

    /* ----------- public methods ------------ */

    /**
     * Create a task by given task properties value object, will throw exception if required properties or binding operator not found
     * @param vo a task properties value object
     * @return created task
     */
    public Task createTask(TaskPropsVO vo) {
        // 1. Validate integrity of TaskPropsVO
        validateTaskPropsVOIntegrity(vo);

        // 2. Binding operator should exists. If not, throw exception.
        if (!operatorDao.fetchById(vo.getOperatorId()).isPresent()) {
            throw new EntityNotFoundException(String.format("Cannot create task with operator id: %d, target operator not found.", vo.getOperatorId()));
        }

        // 3. Task name should not conflict
        if (taskDao.fetchByName(vo.getName()).isPresent()) {
            throw new NameConflictException(String.format("Cannot create task with duplicated name: \"%s\"", vo.getName()));
        }

        // 4. convert value object to task object and assign a new task id
        Task taskWithProps = convertTaskPropsVoToTask(vo);
        Task task = taskWithProps.cloneBuilder()
                .withId(WorkflowIdGenerator.nextTaskId())
                .build();

        // 5. persist with DAO and return
        taskDao.create(task);
        return task;
    }

    public Task partialUpdateTask(Long taskId, TaskPropsVO vo) {
        // 1. Validate arguments
        Preconditions.checkNotNull(taskId, "Invalid argument `taskId`: null");
        Preconditions.checkNotNull(vo, "Cannot perform update with vo: null");

        // 2. If task not exists, throw exception
        Optional<Task> taskOptional = taskDao.fetchById(taskId);
        if (!taskOptional.isPresent()) {
            throw new EntityNotFoundException(String.format("Cannot perform update on non-exist task with id: %d", taskId));
        }

        // 3. Produce a "partially" updated task from existing one as template
        Task task = taskOptional.get();
        Task taskToUpdate = task.cloneBuilder()
                .withId(taskId)
                .withName(StringUtils.isEmpty(vo.getName()) ? task.getName() : vo.getName())
                .withDescription(StringUtils.isEmpty(vo.getDescription()) ? task.getDescription() : vo.getDescription())
                .withOperatorId(Objects.isNull(vo.getOperatorId()) ? task.getOperatorId() : vo.getOperatorId())
                .withVariableDefs(CollectionUtils.isEmpty(vo.getVariableDefs()) ? task.getVariableDefs() : vo.getVariableDefs())
                .withArguments(CollectionUtils.isEmpty(vo.getArguments()) ? task.getArguments() : vo.getArguments())
                .build();

        // 4. perform update
        return fullUpdateTask(taskToUpdate);
    }

    public Task fullUpdateTaskById(Long taskId, TaskPropsVO vo) {
        Task task = Task.newBuilder()
                .withId(taskId)
                .withName(vo.getName())
                .withScheduleConf(vo.getScheduleConf())
                .withDependencies(vo.getDependencies())
                .withVariableDefs(vo.getVariableDefs())
                .withDescription(vo.getDescription())
                .withArguments(vo.getArguments())
                .withOperatorId(vo.getOperatorId())
                .build();
        return fullUpdateTask(task);
    }

    public Task fullUpdateTask(Task task) {
        // 1. Validate task object integrity
        validateTaskIntegrity(task);

        // 2. Binding operator should exists. If not, throw exception.
        if (!operatorDao.fetchById(task.getOperatorId()).isPresent()) {
            throw new EntityNotFoundException(String.format("Cannot create task with operator id: %d, target operator not found.", task.getOperatorId()));
        }

        // 3. Update target task. If there is no task affected (task not exists), throw exception
        boolean taskUpdated = taskDao.update(task);
        if (!taskUpdated) {
            throw new EntityNotFoundException(String.format("Cannot perform update on non-exist task with id: %d", task.getId()));
        }

        // 4. Fetch updated task
        return taskDao.fetchById(task.getId()).orElseThrow(IllegalStateException::new);
    }

    /**
     * Fetch tasks with filters
     * @param filters
     * @return
     */
    public List<Task> fetchTasksByFilters(TaskSearchFilter filters) {
        Preconditions.checkNotNull(filters, "Invalid argument `filters`: null");
        return taskDao.fetchWithFilters(filters);
    }

    /**
     * Fetch single task by id, returns optional
     * @param taskId Task id
     * @return task optional
     */
    public Optional<Task> fetchTaskById(Long taskId) {
        Preconditions.checkNotNull(taskId, "Invalid argument `taskId`: null");
        return taskDao.fetchById(taskId);
    }

    public void deleteTask(Task task) {
        // TODO: recheck this
        validateTaskIntegrity(task);
        this.deleteTaskById(task.getId());
    }

    public void deleteTaskById(Long taskId) {
        boolean entityDeleted = taskDao.deleteById(taskId);
        if (!entityDeleted) {
            throw new EntityNotFoundException(String.format("Cannot delete non-exist task with id: %d", taskId));
        }
    }

    public List<Long> runTasks(List<RunTaskVO> runTaskVOs) {
        List<Long> taskIds = new ArrayList<>();
        RunTaskContext.Builder contextBuilder = RunTaskContext.newBuilder();

        for (RunTaskVO vo : runTaskVOs) {
            Long taskId = vo.getTaskId();
            taskIds.add(taskId);
            contextBuilder.addVariables(taskId, vo.getVariables());
        }

        // fetch tasks
        Map<Long, Optional<Task>> fetched = taskDao.fetchByIds(taskIds);
        List<Task> tasks = fetched.entrySet().stream()
                .map(e -> e.getValue()
                        .orElseThrow(() -> new IllegalArgumentException("Task does not exist for id: " + e.getKey()))
                )
                .collect(Collectors.toList());

        // create graph
        DirectTaskGraph graph = new DirectTaskGraph(tasks);

        // run graph
        List<TaskRun> taskRuns = scheduler.run(graph, contextBuilder.build());

        return taskRuns.stream().map(TaskRun::getId).collect(Collectors.toList());
    }

    /* ----------- private methods ------------ */

    private void validateTaskPropsVONotNull(TaskPropsVO vo) {
        Preconditions.checkNotNull(vo, "Invalid TaskPropsVO argument: null");
    }

    private Task convertTaskPropsVoToTask(TaskPropsVO vo) {
        return Task.newBuilder()
                .withName(vo.getName())
                .withDescription(vo.getDescription())
                .withOperatorId(vo.getOperatorId())
                .withArguments(vo.getArguments())
                .withScheduleConf(vo.getScheduleConf())
                .withVariableDefs(vo.getVariableDefs())
                .withDependencies(vo.getDependencies())
                .build();
    }

    private void validateTaskIntegrity(Task task) {
        Preconditions.checkNotNull(task.getId(), "Invalid task with property `id`: null");
        TaskPropsVO vo = TaskPropsVO.from(task);
        validateTaskPropsVOIntegrity(vo);
    }

    private void validateTaskPropsVOIntegrity(TaskPropsVO vo) {
        validateTaskPropsVONotNull(vo);
        Preconditions.checkArgument(Objects.nonNull(vo.getName()), "Invalid task property object with property `name`: null");
        Preconditions.checkArgument(Objects.nonNull(vo.getDescription()),"Invalid task property object with property `description`: null" );
        Preconditions.checkArgument(Objects.nonNull(vo.getOperatorId()), "Invalid task property object with property `operatorId`: null");
        Preconditions.checkArgument(Objects.nonNull(vo.getArguments()), "Invalid task property object with property `arguments`: null");
        Preconditions.checkArgument(Objects.nonNull(vo.getScheduleConf()), "Invalid task property object with property `scheduleConf`: null");
        Preconditions.checkArgument(Objects.nonNull(vo.getVariableDefs()), "Invalid task property object with property `variableDefs`: null");
        Preconditions.checkArgument(Objects.nonNull(vo.getDependencies()), "Invalid task property object with property `dependencies`: null");
    }
}
