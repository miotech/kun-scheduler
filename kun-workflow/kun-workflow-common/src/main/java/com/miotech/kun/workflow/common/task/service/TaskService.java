package com.miotech.kun.workflow.common.task.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.graph.DirectTaskGraph;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.task.dependency.TaskDependencyFunctionProvider;
import com.miotech.kun.workflow.common.task.filter.TaskSearchFilter;
import com.miotech.kun.workflow.common.task.vo.*;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskDependency;
import com.miotech.kun.workflow.core.model.task.TaskRunEnv;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class TaskService {
    private static final String TASK_ID_SHOULD_NOT_BE_NULL = "Invalid argument `taskId`: null";

    @Inject
    private TaskDao taskDao;

    @Inject
    private OperatorDao operatorDao;

    @Inject
    private TaskDependencyFunctionProvider dependencyFunctionProvider;

    @Inject
    private Scheduler scheduler;

    /* ---------------------------------------- */
    /* ----------- public methods ------------ */
    /* ---------------------------------------- */

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
            throw new IllegalArgumentException(String.format("Cannot create task with duplicated name: \"%s\"", vo.getName()));
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
        Preconditions.checkNotNull(taskId, TASK_ID_SHOULD_NOT_BE_NULL);
        Preconditions.checkNotNull(vo, "Cannot perform update with vo: null");

        Task task = find(taskId);
        Task taskToUpdate = task.cloneBuilder()
                .withId(taskId)
                .withName(StringUtils.isEmpty(vo.getName()) ? task.getName() : vo.getName())
                .withDescription(StringUtils.isEmpty(vo.getDescription()) ? task.getDescription() : vo.getDescription())
                .withOperatorId(Objects.isNull(vo.getOperatorId()) ? task.getOperatorId() : vo.getOperatorId())
                .withConfig(Objects.isNull(vo.getConfig()) ? task.getConfig() : vo.getConfig())
                .withScheduleConf(Objects.isNull(vo.getScheduleConf()) ? task.getScheduleConf() : vo.getScheduleConf())
                .withDependencies(vo.getDependencies() == null ? task.getDependencies() : parseDependencyVO(vo.getDependencies()))
                .withTags(vo.getTags() == null ? task.getTags() : vo.getTags())
                .build();

        // 4. perform update
        return fullUpdateTask(taskToUpdate);
    }

    public Task fullUpdateTaskById(Long taskId, TaskPropsVO vo) {
        Preconditions.checkNotNull(taskId, TASK_ID_SHOULD_NOT_BE_NULL);
        Preconditions.checkNotNull(vo, "Cannot perform update with vo: null");

        Task task = find(taskId).cloneBuilder()
                .withId(taskId)
                .withName(vo.getName())
                .withScheduleConf(vo.getScheduleConf())
                .withDependencies(parseDependencyVO(vo.getDependencies()))
                .withConfig(vo.getConfig())
                .withDescription(vo.getDescription())
                .withOperatorId(vo.getOperatorId())
                .withTags(vo.getTags())
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

    public TaskDAGVO getNeighbors(Long taskId, int upstreamLevel, int downstreamLevel) {
        Preconditions.checkArgument(0 <= upstreamLevel && upstreamLevel <= 5 , "upstreamLevel should be non negative and no greater than 5");
        Preconditions.checkArgument(0 <= downstreamLevel&& downstreamLevel <= 5, "downstreamLevel should be non negative and no greater than 5");

        Task task = find(taskId);
        List<Task> result = new ArrayList<>();
        result.add(task);
        if (upstreamLevel > 0) {
            result.addAll(taskDao.fetchUpstreamTasks(task, upstreamLevel));
        }
        if (downstreamLevel > 0) {
            result.addAll(taskDao.fetchDownstreamTasks(task, downstreamLevel));
        }

        List<TaskDependency> edges = result.stream()
                .flatMap(x -> x.getDependencies().stream())
                .collect(Collectors.toList());
        return new TaskDAGVO(result, edges);
    }

    /**
     * Fetch task page with filters
     * @param filters
     * @return
     */
    public PaginationVO<Task> fetchTasksByFilters(TaskSearchFilter filters) {
        Preconditions.checkNotNull(filters, "Invalid argument `filters`: null");
        return PaginationVO.<Task>newBuilder()
                .withPageNumber(filters.getPageNum())
                .withPageSize(filters.getPageSize())
                .withRecords(taskDao.fetchWithFilters(filters))
                .withTotalCount(taskDao.fetchTotalCountWithFilters(filters))
                .build();
    }

    /**
     * Fetch tasks of tasks that matches filter constraints
     * @param filters filter instance
     * @return total number of records that matches filter constraints
     */
    public Integer fetchTaskTotalCount(TaskSearchFilter filters) {
        Preconditions.checkNotNull(filters, "Invalid argument `filters`: null");
        return taskDao.fetchTotalCountWithFilters(filters);
    }

    /**
     * Fetch single task by id
     * @param taskId Task id
     * @return task
     */
    public Task find(Long taskId) {
        Preconditions.checkNotNull(taskId, TASK_ID_SHOULD_NOT_BE_NULL);
        return taskDao.fetchById(taskId)
                .orElseThrow(() ->
                new EntityNotFoundException(String.format("Cannot find task with id: %s", taskId))
        );
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
        Map<Long, RunTaskVO> rtvMap = runTaskVOs.stream()
                .collect(Collectors.toMap(RunTaskVO::getTaskId, Function.identity()));

        // fetch tasks
        Map<Long, Optional<Task>> fetched = taskDao.fetchByIds(rtvMap.keySet());
        List<Task> tasks = fetched.entrySet().stream()
                .map(e -> e.getValue()
                        .orElseThrow(() -> new IllegalArgumentException("Task does not exist for id: " + e.getKey()))
                )
                .collect(Collectors.toList());

        // create graph
        DirectTaskGraph graph = new DirectTaskGraph(tasks);

        // build taskRunEnv
        TaskRunEnv.Builder envBuilder = TaskRunEnv.newBuilder();
        for (Task t : tasks) {
            Long taskId = t.getId();
            Map<String, Object> config = rtvMap.get(taskId).getConfig();
            if (config == null) {
                config = Collections.emptyMap();
            }
            envBuilder.addConfig(taskId, config);
        }

        // run graph
        List<TaskRun> taskRuns = scheduler.run(graph, envBuilder.build());

        return taskRuns.stream().map(TaskRun::getId).collect(Collectors.toList());
    }

    /* ---------------------------------------- */
    /* ----------- private methods ------------ */
    /* ---------------------------------------- */

    private void validateTaskPropsVONotNull(TaskPropsVO vo) {
        Preconditions.checkNotNull(vo, "Invalid TaskPropsVO argument: null");
    }

    private List<TaskDependency> parseDependencyVO(List<TaskDependencyVO> vo) {
        return vo.stream().map( x -> new TaskDependency(x.getUpstreamTaskId(),
                x.getDownstreamTaskId(),dependencyFunctionProvider.from(
                        x.getDependencyFunction())))
                .collect(Collectors.toList());
    }

    private Task convertTaskPropsVoToTask(TaskPropsVO vo) {
        return Task.newBuilder()
                .withName(vo.getName())
                .withDescription(vo.getDescription())
                .withOperatorId(vo.getOperatorId())
                .withScheduleConf(vo.getScheduleConf())
                .withConfig(vo.getConfig())
                .withDependencies(parseDependencyVO(vo.getDependencies()))
                .withTags(vo.getTags())
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
        Preconditions.checkArgument(Objects.nonNull(vo.getScheduleConf()), "Invalid task property object with property `scheduleConf`: null");
        Preconditions.checkArgument(Objects.nonNull(vo.getConfig()), "Invalid task property object with property `config`: null");
        Preconditions.checkArgument(Objects.nonNull(vo.getDependencies()), "Invalid task property object with property `dependencies`: null");
        Preconditions.checkArgument(Objects.nonNull(vo.getTags()), "Invalid task property object with property `tags`: null");
        // Validate tags property of task VO
        Set<String> tagKeys = new HashSet<>();
        vo.getTags().forEach(tag -> {
            if (tagKeys.contains(tag.getKey())) {
                throw new IllegalArgumentException(String.format("Found key conflict: \"%s\"", tag.getKey()));
            }
            // else
            tagKeys.add(tag.getKey());
        });
    }
}
