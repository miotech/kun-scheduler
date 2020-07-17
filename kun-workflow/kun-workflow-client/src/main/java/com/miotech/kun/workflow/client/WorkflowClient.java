package com.miotech.kun.workflow.client;

import com.miotech.kun.workflow.client.model.*;
import com.miotech.kun.workflow.core.model.common.Tag;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WorkflowClient {

    /**
     * Save or update operator by name
     * @param name: unique identifier
     * @param operator
     * @return
     */
    Operator saveOperator(String name, Operator operator);

    /**
     * Get operator by unique name
     * @param name
     * @return
     */
    Optional<Operator> getOperator(String name);

    /**
     * save a task
     * @return
     */
    Task createTask(Task task);

    /**
     * get a task
     * @return
     */
    Task getTask(Long taskId);

    /**
     * save or update a task, with specific tags
     * @return
     */
    Task saveTask(Task task, List<Tag> filterTags);

    /**
     * delete a task
     * @return
     */
    void deleteTask(Long taskId);

    /**
     * create and launch task then forget
     * @param task
     */
    TaskRun executeTask(Task task, Map<String, String> taskConfig);


    /**
     * execute an existing task
     * @param taskId
     * @param taskConfig
     * @return
     */
    TaskRun executeTask(Long taskId, Map<String, String> taskConfig);


    /**
     * launch existed tasks
     * @param request
     */
    List<TaskRun> executeTasks(RunTaskRequest request);

    /**
     * fetch task run
     * @param taskRunId
     * @return
     */
    TaskRun getTaskRun(Long taskRunId);

    /**
     * fetch task run
     * @param request
     * @return
     */
    PaginationResult<TaskRun> searchTaskRun(TaskRunSearchRequest request);

    /**
     * fetch taskRun state
     * @param taskRunId
     * @return
     */
    TaskRunState getTaskRunState(Long taskRunId);

    /**
     * get task run log
     * @param taskRunId
     * @return
     */
    TaskRunLog getLatestRunLog(Long taskRunId);
}
