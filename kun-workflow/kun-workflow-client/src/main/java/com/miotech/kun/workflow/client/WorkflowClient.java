package com.miotech.kun.workflow.client;

import com.miotech.kun.workflow.client.model.*;
import com.miotech.kun.workflow.core.model.common.Tag;

import java.io.File;
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
     * update operator by id
     * @param operatorId
     * @param operator
     * @return
     */
    Operator updateOperator(Long operatorId, Operator operator);

    /**
     * delete operator by id
     * @param operatorId
     */
    void deleteOperator(Long operatorId);

    /**
     * upload operator jar by id
     * @param id
     * @param jarFile
     */
    void uploadOperatorJar(Long id, File jarFile);

    void updateOperatorJar(String name, File jarFile);

    /**
     * Get operator by unique name
     * @param name
     * @return
     */
    Optional<Operator> getOperator(String name);

    List<Operator> getExistOperators();

    Operator getOperator(Long id);

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
     * get task by unique name
     */
    Optional<Task> getTask(String name);

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
    TaskRun executeTask(Task task, Map<String, Object> taskConfig);

    /**
     * execute an existing task
     * @param taskId
     * @param taskConfig
     * @return
     */
    TaskRun executeTask(Long taskId, Map<String, Object> taskConfig);

    /**
     * launch existed tasks
     * @param request
     */
    List<TaskRun> executeTasks(RunTaskRequest request);

    /**
     * get task DAG
     * @param taskId: centered task id
     * @param upstreamLevel: downstream levels
     * @param downstreamLevel: upstream levels
     * @return
     */
    TaskDAG getTaskDAG(Long taskId, int upstreamLevel, int downstreamLevel);

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
     * get latest task run full log
     * @param taskRunId
     * @return
     */
    TaskRunLog getLatestRunLog(Long taskRunId);

    /**
     * get task run DAG
     * @param taskRunId: centered taskrun id
     * @param upstreamLevel: downstream levels
     * @param downstreamLevel: upstream levels
     * @return
     */
    TaskRunDAG getTaskRunDAG(Long taskRunId, int upstreamLevel, int downstreamLevel);

    /**
     * get task run log
     * @param logRequest
     * @return
     */
    TaskRunLog getTaskRunLog(TaskRunLogRequest logRequest);

    /**
     * stop taskRun state
     * @param taskRunId
     * @return
     */
    TaskRun stopTaskRun(Long taskRunId);
}
