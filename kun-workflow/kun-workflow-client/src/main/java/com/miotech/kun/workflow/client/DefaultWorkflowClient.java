package com.miotech.kun.workflow.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.miotech.kun.workflow.client.model.*;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.common.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultWorkflowClient implements WorkflowClient {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWorkflowClient.class);
    private final WorkflowApi wfApi;

    public DefaultWorkflowClient(WorkflowApi wfApi) {
        this.wfApi = wfApi;
    }

    public DefaultWorkflowClient(String baseUrl) {
        wfApi = new WorkflowApi(baseUrl);
    }

    @Override
    public Operator saveOperator(String name, Operator operator) {
        Optional<Operator> optionalOperator = getOperator(name);
        Operator newOperator;
        if (optionalOperator.isPresent()) {
            long opId = optionalOperator.get().getId();
            newOperator = wfApi.updateOperator(opId, operator);
        } else {
            newOperator = wfApi.createOperator(operator);
        }
        return newOperator;
    }

    @Override
    public void updateOperatorJar(String name, File jarFile) {
        Optional<Operator> optionalOperator = getOperator(name);
        if (optionalOperator.isPresent()) {
            wfApi.uploadJar(optionalOperator.get().getId(), jarFile);
        } else {
            throw new IllegalArgumentException("Operator not found with name " + name);
        }
    }

    @Override
    public Optional<Operator> getOperator(String name) {
        List<Operator> operators = wfApi.getOperators(
                OperatorSearchRequest.newBuilder()
                        .withName(name)
                        .build())
                .getRecords();
        return operators.stream()
                .filter(x -> x.getName().equals(name))
                .findAny();
    }

    @Override
    public Task createTask(Task task) {
        return wfApi.createTask(task);
    }

    @Override
    public Task getTask(Long taskId) {
        return wfApi.getTask(taskId);
    }

    @Override
    public Optional<Task> getTask(String name) {
        List<Task> tasks = wfApi.getTasks(TaskSearchRequest.newBuilder().withName(name).build());
        return tasks.stream().filter(x -> x.getName().equals(name)).findAny();
    }

    @Override
    public Task saveTask(Task task, List<Tag> filterTags) {
        if (filterTags != null && !filterTags.isEmpty()) {
            String tags = filterTags.stream()
                    .map(Tag::toString)
                    .collect(Collectors.joining(","));
            TaskSearchRequest request = TaskSearchRequest.newBuilder()
                    .withTags(filterTags)
                    .build();
            PaginationResult<Task> taskPage = wfApi.searchTasks(request);
            if (taskPage.getTotalCount() == 0) {
                logger.debug("do not found task \"{}\" with tag \"{}\", create new one", task.getName(), tags);
                return createTask(task);
            }
            List<Task> tasks = taskPage.getRecords();
            Preconditions.checkArgument(tasks.size() == 1, "Multiple tasks found for tags: " + filterTags);
            logger.debug("Update task \"{}\" with tag \"{}\"", task.getName(), tags);
            return wfApi.updateTask(tasks.get(0).getId(), task);
        } else if (task.getId() > 0) {
            logger.debug("Update task \"{}\" with id \"{}\"", task.getName() , task.getId());
            return wfApi.updateTask(task.getId(), task);
        } else {
            logger.debug("Create new task \"{}\"", task.getName());
            return createTask(task);
        }
    }

    @Override
    public void deleteTask(Long taskId) {
        wfApi.deleteTask(taskId);
    }

    @Override
    public TaskRun executeTask(Task task, Map<String, Object> taskConfig) {
        Task saved = wfApi.createTask(task);
        return executeTask(saved.getId(), taskConfig);
    }

    @Override
    public TaskRun executeTask(Long taskId, Map<String, Object> taskConfig) {
        RunTaskRequest request = new RunTaskRequest();
        request.addTaskVariable(taskId, taskConfig != null ? taskConfig : Maps.newHashMap());
        List<Long> taskRunIds = wfApi.runTasks(request);
        if (taskRunIds.isEmpty()) {
            throw new WorkflowClientException("No task run found after execution for task: " + taskId);
        }

        return wfApi.getTaskRun(taskRunIds.get(0));
    }

    @Override
    public List<TaskRun> executeTasks(RunTaskRequest request) {
        List<Long> taskRunIds = wfApi.runTasks(request);

        TaskRunSearchRequest taskRunSearchRequest = TaskRunSearchRequest
                .newBuilder()
                .withTaskRunIds(taskRunIds)
                .build();
        return wfApi.searchTaskRuns(taskRunSearchRequest)
                .getRecords();
    }

    @Override
    public TaskDAG getTaskDAG(Long taskId, int upstreamLevel, int downstreamLevel) {
        return wfApi.getTaskDAG(taskId, upstreamLevel, downstreamLevel);
    }

    @Override
    public TaskRun getTaskRun(Long taskRunId) {
        return wfApi.getTaskRun(taskRunId);
    }

    @Override
    public PaginationResult<TaskRun> searchTaskRun(TaskRunSearchRequest request) {
        return wfApi.searchTaskRuns(request);
    }

    @Override
    public TaskRunState getTaskRunState(Long taskRunId) {
        return wfApi.getTaskRunStatus(taskRunId);
    }

    @Override
    public TaskRunLog getLatestRunLog(Long taskRunId) {
        TaskRunLogRequest request = TaskRunLogRequest.newBuilder()
                .withTaskRunId(taskRunId)
                .withAttempt(-1)
                .build();
        return wfApi.getTaskRunLog(request);
    }

    @Override
    public TaskRunDAG getTaskRunDAG(Long taskRunId, int upstreamLevel, int downstreamLevel) {
        return wfApi.getTaskRunDAG(taskRunId, upstreamLevel, downstreamLevel);
    }

    @Override
    public TaskRunLog getTaskRunLog(TaskRunLogRequest logRequest) {
        return wfApi.getTaskRunLog(logRequest);
    }

    @Override
    public TaskRun stopTaskRun(Long taskRunId) {
        wfApi.stopTaskRun(taskRunId);
        return wfApi.getTaskRun(taskRunId);
    }
}
