package com.miotech.kun.workflow.common.task.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.task.filter.TaskSearchFilter;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.model.bo.RunTaskInfo;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class TaskService {
    @Inject
    private TaskDao taskDao;

    @Inject
    private OperatorDao operatorDao;

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
                .build();
    }

    private void validateTaskIntegrity(Task task) {
        Preconditions.checkArgument(Objects.nonNull(task.getId()), "Invalid task with property `id`: null");
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
    }

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

        // 3. convert value object to task object and assign a new task id
        Task taskWithProps = convertTaskPropsVoToTask(vo);
        Task task = taskWithProps.cloneBuilder()
                .withId(WorkflowIdGenerator.nextTaskId())
                .build();

        // 4. persist with DAO and return
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
        return taskDao.fetchById(task.getId()).orElse(null);
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
        boolean entityDeleted = taskDao.deleteById(task.getId());
        if (!entityDeleted) {
            throw new EntityNotFoundException(String.format("Cannot delete non-exist task with id: %d", task.getId()));
        }
    }

    public Task runTasks(List<RunTaskInfo> runTaskInfos) {
        // TODO: implement this
        return Task.newBuilder().build();
    }
}
