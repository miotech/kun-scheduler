package com.miotech.kun.workflow.client;

import com.miotech.kun.workflow.client.model.*;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import com.miotech.kun.workflow.core.model.lineage.DatasetLineageInfo;
import com.miotech.kun.workflow.core.model.lineage.EdgeInfo;
import com.miotech.kun.workflow.core.model.lineage.node.DatasetInfo;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.core.model.taskrun.TimeType;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
     * @param taskConfig
     */
    TaskRun executeTask(Task task, Map<String, Object> taskConfig);

    /**
     * create and launch task then forget
     * @param task
     * @param taskConfig
     * @param targetId
     */
    TaskRun executeTask(Task task, Map<String, Object> taskConfig,Long targetId);

    /**
     * execute an existing task
     * @param taskId
     * @param taskConfig
     * @return
     */
    TaskRun executeTask(Long taskId, Map<String, Object> taskConfig);

    /**
     * execute an existing task
     * @param taskId
     * @param taskConfig
     * @param targetId
     * @return
     */
    TaskRun executeTask(Long taskId, Map<String, Object> taskConfig,Long targetId);

    /**
     * launch existed tasks
     * @param request
     * @return key:taskId,value:taskRun
     */
    Map<Long,TaskRun> executeTasks(RunTaskRequest request);

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
     * Fetch task run count by filter
     * @param request
     * @return
     */
    Integer countTaskRun(TaskRunSearchRequest request);

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
     * get latest task run full log
     * @param taskRunId
     * @return
     */
    TaskRunLog getLatestRunLog(Long taskRunId, Integer attempt);

    /**
     * Fetch log of target task run's latest attempt
     * @param taskRunId
     * @param start
     * @param end
     * @return
     */
    TaskRunLog getLatestRunLog(Long taskRunId, Integer start, Integer end);


    /**
     * get latest task run full log
     * @param taskRunId
     * @return
     */
    TaskRunLog getLatestRunLog(Long taskRunId, Integer start, Integer end, Integer attempt);

    /**
     * get task run DAG
     * @param taskRunId: centered taskrun id
     * @param upstreamLevel: downstream levels
     * @param downstreamLevel: upstream levels
     * @return
     */
    TaskRunDAG getTaskRunDAG(Long taskRunId, int upstreamLevel, int downstreamLevel);

    /**
     * get gantt chart info of task run
     * @param taskRunId specified task run
     * @return
     */
    TaskRunGanttChart getTaskRunGantt(Long taskRunId);

    /**
     * get global gantt chart info within specified time range
     * @param startTime
     * @param endTime
     * @return
     */
    TaskRunGanttChart getGlobalTaskRunGantt(OffsetDateTime startTime, OffsetDateTime endTime, TimeType timeType);

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

    /**
     * Restart a taskRun immediately
     * @param taskRunId id of target task run
     * @return
     */
    TaskRun restartTaskRun(Long taskRunId);

    /**
     * stop taskRuns
     * @param taskRunIds
     * * @return
     */
    void stopTaskRuns(List<Long> taskRunIds);

    /**
     * Get latest N task runs of given task id with filter taskRun status
     * @param taskId id of task
     * @param filterStatus task run status used to filter
     * @param limit size of the task run list for given task
     * @return
     */
    List<TaskRun> getLatestTaskRuns(Long taskId, List<TaskRunStatus> filterStatus, int limit);

    /**
     * Get latest N task runs of given task ids.
     * @param taskIds ids of the tasks for the query
     * @param limit size of the task run list for each task
     * @return Mapped results in dictionary, where task id is the key, and its latest runs in a list as value
     */
    Map<Long, List<TaskRun>> getLatestTaskRuns(List<Long> taskIds, int limit);


    /**
     * Get latest N task runs of given task ids.
     * @param taskIds ids of the tasks for the query
     * @param limit size of the task run list for each task
     * @param containsAttempt true:taskrun contains all taskAttempt info
     * @return
     */
    Map<Long, List<TaskRun>> getLatestTaskRuns(List<Long> taskIds, int limit , boolean containsAttempt);

    /**
     * Get neighbouring upstream / downstream lineage dataset nodes of specific dataset
     * @param datasetGid global id of dataset
     * @param direction upstream, downstream or both
     * @param depth query depth
     * @return dataset lineage info
     */
    DatasetLineageInfo getLineageNeighbors(Long datasetGid, LineageQueryDirection direction, int depth);

    /**
     * Get lineage edge info
     * @param upstreamDatasetGid global id of upstream dataset
     * @param downstreamDatasetGid global id of downstream dataset
     * @return edge info object
     */
    EdgeInfo getLineageEdgeInfo(Long upstreamDatasetGid, Long downstreamDatasetGid);

    /**
     * Get all the tables produced by task
     * @param taskId
     * @return
     */
    Set<DatasetInfo> fetchOutletNodes(Long taskId);

    /**
     * Fetch all defined variables in workflow
     * @return list of variables defined in system
     */
    List<VariableVO> getAllVariables();

    /**
     * Create a variable with provided value object
     * @param variableVO value object of variable to create
     * @return persisted variable value object
     */
    VariableVO createVariable(VariableVO variableVO);

    /**
     * Update a variable with provided value. It will update the variable with the same namespace and key.
     * @param variableVO value object of variable to create.
     * @return persisted variable value object
     */
    VariableVO updateVariable(VariableVO variableVO);

    /**
     * Remove a variable by its namespace and key
     * @param namespace namespace of the variable to delete
     * @param key key of the variable to delete
     * @return {true} if success, {false} if not existing
     */
    Boolean deleteVariable(String namespace, String key);

    /**
     * change taskRun priority by taskRunId
     * @param taskRunId id of taskRun
     * @param priority priority of taskRun,Priorities range from 1 to Integer.MAX_VALUE,
     * The higher the value, the higher the priority,default priority is 16
     * @return
     */
    Boolean changeTaskRunPriority(Long taskRunId,Integer priority);

    /**
     * get all target
     * @return list of targets
     */
    List<ExecuteTarget> getTargetList();

    /**
     * remove dependency of the current taskRun
     * does not affect the next schedule taskRun
     * @param taskRunId
     * @param upstreamTaskRunIds
     */
    void removeTaskRunDependency(Long taskRunId, List<Long> upstreamTaskRunIds);

}
