package com.miotech.kun.workflow.common.taskrun.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.bo.TaskRunDailyStatisticInfo;
import com.miotech.kun.workflow.common.taskrun.filter.TaskRunSearchFilter;
import com.miotech.kun.workflow.common.tick.TickDao;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.SpecialTick;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.*;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunDependency;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.miotech.kun.commons.db.sql.SQLUtils.column;
import static com.miotech.kun.commons.utils.StringUtils.repeatJoin;
import static com.miotech.kun.commons.utils.StringUtils.toNullableString;

@Singleton
public class TaskRunDao {
    private static final Logger logger = LoggerFactory.getLogger(TaskRunDao.class);
    protected static final String TASK_RUN_MODEL_NAME = "taskrun";
    protected static final String TASK_RUN_TABLE_NAME = "kun_wf_task_run";
    private static final List<String> taskRunCols = ImmutableList.of("id", "task_id", "scheduled_tick", "status", "schedule_type", "start_at", "end_at", "config", "inlets", "outlets", "created_at", "updated_at", "queue_name", "priority");

    private static final String TASK_ATTEMPT_MODEL_NAME = "taskattempt";
    private static final String TASK_ATTEMPT_TABLE_NAME = "kun_wf_task_attempt";
    private static final List<String> taskAttemptCols = ImmutableList.of("id", "task_run_id", "attempt", "status", "start_at", "end_at", "log_path", "queue_name", "priority");

    private static final String RELATION_TABLE_NAME = "kun_wf_task_run_relations";
    private static final String RELATION_MODEL_NAME = "task_run_relations";
    private static final List<String> taskRunRelationCols = ImmutableList.of("upstream_task_run_id", "downstream_task_run_id", "dependency_level", "dependency_status");
    private static final Map<String, String> sortKeyToFieldMapper = new HashMap<>();

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        sortKeyToFieldMapper.put("id", TASK_RUN_MODEL_NAME + "_id");
        sortKeyToFieldMapper.put("startAt", TASK_RUN_MODEL_NAME + "_start_at");
        sortKeyToFieldMapper.put("endAt", TASK_RUN_MODEL_NAME + "_end_at");
        sortKeyToFieldMapper.put("status", TASK_RUN_MODEL_NAME + "_status");
        sortKeyToFieldMapper.put("createdAt", TASK_RUN_MODEL_NAME + "_created_at");
        sortKeyToFieldMapper.put("updatedAt", TASK_RUN_MODEL_NAME + "_updated_at");
    }

    @Inject
    private TaskDao taskDao;

    @Inject
    private DatabaseOperator dbOperator;

    @Inject
    private TaskRunMapper taskRunMapperInstance;

    @Inject
    private TickDao tickDao;

    @Inject
    public TaskRunDao(TaskDao taskDao, DatabaseOperator dbOperator
            , TaskRunMapper taskRunMapperInstance) {
        this.taskDao = taskDao;
        this.dbOperator = dbOperator;
        this.taskRunMapperInstance = taskRunMapperInstance;
    }

    private enum DependencyDirection {
        UPSTREAM,
        DOWNSTREAM
    }

    private SQLBuilder getTaskRunSQLBuilderWithDefaultConfig() {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_RUN_MODEL_NAME, taskRunCols);
        columnsMap.put(TaskDao.TASK_MODEL_NAME, TaskDao.getTaskCols());

        return DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_RUN_TABLE_NAME, TASK_RUN_MODEL_NAME)
                .join("LEFT", TaskDao.TASK_TABLE_NAME, TaskDao.TASK_MODEL_NAME)
                .on(TASK_RUN_MODEL_NAME + ".task_id = " + TaskDao.TASK_MODEL_NAME + ".id")
                .autoAliasColumns();
    }

    private SQLBuilder getTaskRunSQLBuilderForLatestTaskRuns() {
        List<String> taskRunColsSelectAliasSubStrings = new ArrayList<>();
        List<String> taskColsSelectAliasSubStrings = new ArrayList<>();
        for (String taskRunCol : taskRunCols) {
            taskRunColsSelectAliasSubStrings.add(TASK_RUN_MODEL_NAME + "." + taskRunCol + " AS " + TASK_RUN_MODEL_NAME + "_" + taskRunCol);
        }
        for (String taskCol : TaskDao.getTaskCols()) {
            taskColsSelectAliasSubStrings.add(TaskDao.TASK_MODEL_NAME + "." + taskCol + " AS " + TaskDao.TASK_MODEL_NAME + "_" + taskCol);
        }
        String taskRunColsSelectAlias = String.join(", ", taskRunColsSelectAliasSubStrings.toArray(new String[0]));
        String taskColsSelectAlias = String.join(", ", taskColsSelectAliasSubStrings.toArray(new String[0]));
        String rowNumSelect = "row_number() OVER (PARTITION BY " + TASK_RUN_MODEL_NAME + ".task_id ORDER BY " + TASK_RUN_MODEL_NAME + ".id DESC) as row_num";

        return DefaultSQLBuilder.newBuilder()
                .select(taskRunColsSelectAlias + ", " + taskColsSelectAlias + ", " + rowNumSelect)
                .from(TASK_RUN_TABLE_NAME, TASK_RUN_MODEL_NAME)
                .join("LEFT", TaskDao.TASK_TABLE_NAME, TaskDao.TASK_MODEL_NAME)
                .on(TASK_RUN_MODEL_NAME + ".task_id = " + TaskDao.TASK_MODEL_NAME + ".id");
    }

    private String getSelectSQL(String whereClause) {
        return getTaskRunSQLBuilderWithDefaultConfig().where(whereClause).getSQL();
    }

    private List<Optional<TaskRun>> fetchTaskRunsByIds(Collection<Long> taskRunIds) {
        List<Optional<TaskRun>> runs = new ArrayList<>();
        for (Long id : taskRunIds) {
            runs.add(fetchTaskRunById(id));
        }
        return runs;
    }

    /**
     * Internal use only. Retrieve task run ids within the maximum distance from the given source task run.
     *
     * @param srcTaskRunId
     * @param distance
     * @param direction
     * @param includeSelf
     * @return
     */
    private Set<Long> retrieveTaskRunIdsWithinDependencyDistance(Long srcTaskRunId, int distance, DependencyDirection direction, boolean includeSelf) {
        Preconditions.checkNotNull(srcTaskRunId, "Invalid argument `srcTaskRunId`: null");
        Preconditions.checkArgument(distance > 0, "Argument `distance` should be positive, but found: %d", distance);

        int remainingIteration = distance;
        Set<Long> resultTaskRunIds = new HashSet<>();
        List<Long> inClauseIdSet = Lists.newArrayList(srcTaskRunId);
        String selectCol = (direction == DependencyDirection.UPSTREAM) ? "upstream_task_run_id" : "downstream_task_run_id";
        String whereCol = (direction == DependencyDirection.UPSTREAM) ? "downstream_task_run_id" : "upstream_task_run_id";
        if (includeSelf) {
            resultTaskRunIds.add(srcTaskRunId);
        }

        while ((remainingIteration > 0) && (!inClauseIdSet.isEmpty())) {
            String idsFieldsPlaceholder = "(" + inClauseIdSet.stream().map(id -> "?")
                    .collect(Collectors.joining(", ")) + ")";
            String sql = DefaultSQLBuilder.newBuilder()
                    .select(selectCol)
                    .from(RELATION_TABLE_NAME)
                    .where(whereCol + " IN " + idsFieldsPlaceholder)
                    .getSQL();
            List<Long> results = dbOperator.fetchAll(sql, r -> r.getLong(selectCol), inClauseIdSet.toArray());
            resultTaskRunIds.addAll(results);
            inClauseIdSet = new ArrayList<>(results);
            remainingIteration -= 1;
        }

        return resultTaskRunIds;
    }

    /**
     * Internal use only. Retrieve upstream/downstream task runs by given source task run id, direction and iteration times.
     *
     * @param taskRunId
     * @param distance
     * @param direction
     * @param includeSelf
     * @return
     */
    private List<TaskRun> fetchDependentTaskRunsById(Long taskRunId, int distance, DependencyDirection direction, boolean includeSelf) {
        Preconditions.checkNotNull(taskRunId);
        Optional<TaskRun> taskRunOptional = fetchTaskRunById(taskRunId);
        if (!taskRunOptional.isPresent()) {
            throw new EntityNotFoundException(String.format("Cannot find task run with id: %s", taskRunId));
        }

        Set<Long> upstreamTaskIds = retrieveTaskRunIdsWithinDependencyDistance(taskRunId, distance, direction, includeSelf);
        List<Optional<TaskRun>> upstreamRuns = fetchTaskRunsByIds(upstreamTaskIds);
        return upstreamRuns.stream()
                .map(r -> r.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Pair<String, List<Object>> generateWhereClauseAndParamsByFilter(TaskRunSearchFilter filter) {
        List<String> whereConditions = new ArrayList<>();
        List<Object> sqlArgs = new ArrayList<>();
        if (Objects.nonNull(filter.getTaskIds()) && (!filter.getTaskIds().isEmpty())) {
            String idsFieldsPlaceholder = "(" + filter.getTaskIds().stream().map(id -> "?")
                    .collect(Collectors.joining(", ")) + ")";
            sqlArgs.addAll(filter.getTaskIds());
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".task_id IN " + idsFieldsPlaceholder + " )");
        }
        // if include started only
        if (Objects.equals(filter.getIncludeStartedOnly(), true)) {
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".start_at IS NOT NULL)");
        }

        // Search task created in time range
        // DON'T USE BETWEEN, see: https://wiki.postgresql.org/wiki/Don't_Do_This#Don.27t_use_BETWEEN_.28especially_with_timestamps.29
        if (Objects.nonNull(filter.getDateFrom())) {
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".created_at >= ? )");
            sqlArgs.add(filter.getDateFrom());
        }
        if (Objects.nonNull(filter.getDateTo())) {
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".created_at <= ? )");
            sqlArgs.add(filter.getDateTo());
        }

        // Search by status
        if (Objects.nonNull(filter.getStatus()) && (!filter.getStatus().isEmpty())) {
            StringBuilder statusSubWhereClauseBuilder = new StringBuilder("( ");
            for (TaskRunStatus status : filter.getStatus()) {
                statusSubWhereClauseBuilder.append("(" + TASK_RUN_MODEL_NAME + ".status = ? ) OR ");
                sqlArgs.add(status.name());
            }
            statusSubWhereClauseBuilder.append(" (1 = 0) )");
            whereConditions.add(statusSubWhereClauseBuilder.toString());
        }

        // Search by tags, if any
        if (Objects.nonNull(filter.getTags()) && (!filter.getTags().isEmpty())) {
            Pair<String, List<Object>> filterByTagsSqlAndParams = taskDao.generateFilterByTagSQLClause(filter.getTags());
            String filterByTagSQL = filterByTagsSqlAndParams.getLeft();
            List<Object> filterByTagSQLParams = filterByTagsSqlAndParams.getRight();

            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".task_id IN (" + filterByTagSQL + " ))");
            sqlArgs.addAll(filterByTagSQLParams);
        }

        // Search by scheduleType
        if (Objects.nonNull(filter.getScheduleTypes()) && (!filter.getScheduleTypes().isEmpty())) {
            List<String> scheduleTypes = filter.getScheduleTypes();
            if (scheduleTypes.size() == 1) {
                whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".schedule_type = ?)");
                sqlArgs.add(scheduleTypes.get(0));
            } else {
                String filterScheduleTypes = scheduleTypes.stream().map(x -> "?").collect(Collectors.joining(","));
                whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".schedule_type in (" + filterScheduleTypes + "))");
                sqlArgs.addAll(scheduleTypes);
            }
        }

        //Search by taskRunIds
        if (Objects.nonNull(filter.getTaskRunIds()) && (!filter.getTaskRunIds().isEmpty())) {
            List<Long> taskRunIds = filter.getTaskRunIds();
            if (taskRunIds.size() == 1) {
                whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".id = ?)");
                sqlArgs.add(taskRunIds.get(0));
            } else {
                String filterTaskRunIds = taskRunIds.stream().map(x -> "?").collect(Collectors.joining(","));
                whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".id in (" + filterTaskRunIds + "))");
                sqlArgs.addAll(taskRunIds);
            }
        }

        String whereClause;
        if (whereConditions.isEmpty()) {
            whereClause = "1 = 1";
        } else {
            whereClause = String.join(" AND ", whereConditions.toArray(new String[0]));
        }

        return Pair.of(whereClause, sqlArgs);
    }

    /**
     * Fetch TaskRun instance with given id
     *
     * @param id id of target task run instance
     * @return the query result of TaskRun instance, empty represents not found
     */
    public Optional<TaskRun> fetchTaskRunById(Long id) {
        TaskRun taskRun = dbOperator.fetchOne(getSelectSQL(TASK_RUN_MODEL_NAME + ".id = ?"), taskRunMapperInstance, id);

        List<Long> dependencies = fetchTaskRunDependencies(id);
        if (Objects.nonNull(taskRun)) {
            return Optional.of(taskRun.cloneBuilder()
                    .withDependentTaskRunIds(dependencies)
                    .build());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Persist TaskRun instance in database
     *
     * @param taskRun instance of TaskRun model to be stored
     * @return TaskRun instance itself
     * @throws RuntimeException if primary key (id) is duplicated
     */
    public TaskRun createTaskRun(TaskRun taskRun) {

        Optional<TaskRun> savedTaskRun = fetchTaskRunById(taskRun.getId());
        if (savedTaskRun.isPresent()) {
            return savedTaskRun.get();
        }
        dbOperator.transaction(() -> {
            OffsetDateTime now = DateTimeUtils.now();

            List<String> tableColumns = new ImmutableList.Builder<String>()
                    .addAll(taskRunCols)
                    .build();

            String sql = DefaultSQLBuilder.newBuilder()
                    .insert(tableColumns.toArray(new String[0]))
                    .into(TASK_RUN_TABLE_NAME)
                    .asPrepared()
                    .getSQL();
            String scheduleType = taskRun.getScheduledType() != null ?
                    taskRun.getScheduledType().name() : taskRun.getTask().getScheduleConf().getType().name();
            dbOperator.update(sql,
                    taskRun.getId(),
                    taskRun.getTask().getId(),
                    taskRun.getScheduledTick().toString(),
                    toNullableString(taskRun.getStatus()),
                    scheduleType,
                    taskRun.getStartAt(),
                    taskRun.getEndAt(),
                    JSONUtils.toJsonString(taskRun.getConfig()),
                    JSONUtils.toJsonString(taskRun.getInlets()),
                    JSONUtils.toJsonString(taskRun.getOutlets()),
                    now,
                    now,
                    taskRun.getQueueName(),
                    taskRun.getPriority()
            );

            createTaskRunDependencies(taskRun.getId(), taskRun.getDependentTaskRunIds(), taskRun.getTask());
            return taskRun;
        });

        return taskRun;
    }

    public TaskRun fetchTaskRunByTaskAndTick(Long taskId, Tick tick) {
        TaskRun taskRun = dbOperator.fetchOne(getSelectSQL(TASK_RUN_MODEL_NAME + ".task_id = ? and " +
                "scheduled_tick = ?"), taskRunMapperInstance, taskId, tick.toString());
        return taskRun;
    }

    public List<TaskRun> createTaskRuns(List<TaskRun> taskRuns) {
        return taskRuns.stream().map(this::createTaskRun).collect(Collectors.toList());
    }

    public List<TaskRun> createTaskRuns(Map<TaskGraph, List<TaskRun>> graphTaskRuns) {
        List<TaskRun> result = new ArrayList<>();
        for (Map.Entry<TaskGraph, List<TaskRun>> entry : graphTaskRuns.entrySet()) {
            TaskGraph graph = entry.getKey();
            List<TaskRun> taskRunList = entry.getValue();
            for (TaskRun taskRun : taskRunList) {
                logger.debug("to create taskRun , taskRunId = {}", taskRun.getId());
                result.add(createTaskRun(taskRun, graph));
            }
        }
        return result;
    }

    public TaskRun createTaskRun(TaskRun taskRun, TaskGraph taskGraph) {
        Optional<TaskRun> savedTaskRun = fetchTaskRunById(taskRun.getId());
        if (savedTaskRun.isPresent()) {
            return savedTaskRun.get();
        }
        dbOperator.transaction(() -> {
            OffsetDateTime now = DateTimeUtils.now();

            List<String> tableColumns = new ImmutableList.Builder<String>()
                    .addAll(taskRunCols)
                    .build();

            String sql = DefaultSQLBuilder.newBuilder()
                    .insert(tableColumns.toArray(new String[0]))
                    .into(TASK_RUN_TABLE_NAME)
                    .asPrepared()
                    .getSQL();
            String scheduleType = taskRun.getScheduledType() != null ?
                    taskRun.getScheduledType().name() : taskRun.getTask().getScheduleConf().getType().name();

            dbOperator.update(sql,
                    taskRun.getId(),
                    taskRun.getTask().getId(),
                    taskRun.getScheduledTick().toString(),
                    toNullableString(taskRun.getStatus()),
                    scheduleType,
                    taskRun.getStartAt(),
                    taskRun.getEndAt(),
                    JSONUtils.toJsonString(taskRun.getConfig()),
                    JSONUtils.toJsonString(taskRun.getInlets()),
                    JSONUtils.toJsonString(taskRun.getOutlets()),
                    now,
                    now,
                    taskRun.getQueueName(),
                    taskRun.getPriority()
            );

            createTaskRunDependencies(taskRun.getId(), taskRun.getDependentTaskRunIds(), taskRun.getTask());
            if (taskRun.getScheduledTick() != SpecialTick.NULL) {
                taskGraph.updateTasksNextExecutionTick(taskRun.getScheduledTick(), Lists.newArrayList(taskRun.getTask()));
            }
            return taskRun;
        });

        return taskRun;
    }

    /**
     * Update a TaskRun instance
     *
     * @param taskRun TaskRun instance with properties updated
     * @return
     */
    public TaskRun updateTaskRun(TaskRun taskRun) {
        OffsetDateTime now = DateTimeUtils.now();
        dbOperator.transaction(() -> {
            List<String> tableColumns = new ImmutableList.Builder<String>()
                    .addAll(taskRunCols)
                    .build();

            String sql = DefaultSQLBuilder.newBuilder()
                    .update(TASK_RUN_TABLE_NAME)
                    .set(tableColumns.toArray(new String[0]))
                    .where("id = ?")
                    .asPrepared()
                    .getSQL();

            dbOperator.update(sql,
                    taskRun.getId(),
                    taskRun.getTask().getId(),
                    taskRun.getScheduledTick().toString(),
                    toNullableString(taskRun.getStatus()),
                    taskRun.getScheduledType().name(),
                    taskRun.getStartAt(),
                    taskRun.getEndAt(),
                    JSONUtils.toJsonString(taskRun.getConfig()),
                    JSONUtils.toJsonString(taskRun.getInlets()),
                    JSONUtils.toJsonString(taskRun.getOutlets()),
                    taskRun.getCreatedAt(),       // created_at
                    now,                          // updated_at
                    taskRun.getQueueName(),
                    taskRun.getPriority(),
                    taskRun.getId()
            );

            deleteTaskRunDependencies(taskRun.getId());
            createTaskRunDependencies(taskRun.getId(), taskRun.getDependentTaskRunIds(), taskRun.getTask());
            return taskRun;
        });

        return taskRun;
    }

    public void updateTaskRunInletsOutlets(long taskRunId, List<DataStore> inlets, List<DataStore> outlets) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(TASK_RUN_TABLE_NAME)
                .set("inlets", "outlets")
                .where("id = ?")
                .asPrepared()
                .getSQL();

        dbOperator.update(sql,
                JSONUtils.toJsonString(inlets, new TypeReference<List<DataStore>>() {
                }),
                JSONUtils.toJsonString(outlets, new TypeReference<List<DataStore>>() {
                }),
                taskRunId
        );


    }

    public void updateAttemptStatusByTaskRunIds(List<Long> taskRunIds, TaskRunStatus taskRunStatus) {
        String filterTaskRunId = taskRunIds.stream().map(x -> "?").collect(Collectors.joining(","));
        String taskRunSql = DefaultSQLBuilder.newBuilder()
                .update(TASK_RUN_TABLE_NAME)
                .set("status=?")
                .where("id in " + "(" + filterTaskRunId + ")")
                .getSQL();
        String taskAttemptSql = DefaultSQLBuilder.newBuilder()
                .update(TASK_ATTEMPT_TABLE_NAME)
                .set("status=?")
                .where("task_run_id in " + "(" + filterTaskRunId + ")")
                .getSQL();
        dbOperator.transaction(() -> {
            dbOperator.update(taskRunSql, taskRunStatus.name(), taskRunIds.toArray());
            dbOperator.update(taskAttemptSql, taskRunStatus.name(), taskRunIds.toArray());
            DependencyStatus dependencyStatus = DependencyStatus.fromUpstreamStatus(taskRunStatus);
            updateTaskRunDependencyByTaskRunIds(taskRunIds, dependencyStatus);
            return null;
        });
    }

    /**
     * Recursively fetch all downstream taskRunIds with the given taskRunId
     *
     * @param taskRunId
     * @return
     */
    public List<Long> fetchDownStreamTaskRunIdsRecursive(Long taskRunId) {
        Set<Long> downStreamTaskRunIds = new HashSet<>();
        Queue<Long> childTaskRunIdQueue = new LinkedList<>();
        childTaskRunIdQueue.add(taskRunId);
        while (!childTaskRunIdQueue.isEmpty()) {
            Long nextTaskRunId = childTaskRunIdQueue.poll();
            if (downStreamTaskRunIds.add(nextTaskRunId)) {
                List<Long> childTaskRunIdList = fetchDownStreamTaskRunIds(nextTaskRunId);
                childTaskRunIdList.forEach(x -> {
                    if (!downStreamTaskRunIds.contains(x)) {
                        childTaskRunIdQueue.add(x);
                    }
                });
            }
        }
        return downStreamTaskRunIds.stream().collect(Collectors.toList());
    }

    /**
     * Fetch downstream taskRunIds with the given taskRunId
     *
     * @param taskRunId
     * @return
     */
    public List<Long> fetchDownStreamTaskRunIds(Long taskRunId) {
        String dependencySQL = DefaultSQLBuilder.newBuilder()
                .select("downstream_task_run_id")
                .from(RELATION_TABLE_NAME)
                .where("upstream_task_run_id = ?")
                .getSQL();
        return dbOperator.fetchAll(dependencySQL, rs -> rs.getLong(1), taskRunId);
    }


    /**
     * Delete a TaskRun instance by ID
     *
     * @param taskRunId ID of target TaskRun instance
     * @return
     */
    public boolean deleteTaskRun(Long taskRunId) {
        return dbOperator.transaction(() -> {

            deleteTaskRunDependencies(taskRunId);
            deleteTaskAttempts(taskRunId);
            String deleteSQL = DefaultSQLBuilder.newBuilder()
                    .delete()
                    .from(TASK_RUN_TABLE_NAME)
                    .where("id = ?")
                    .getSQL();
            return dbOperator.update(deleteSQL, taskRunId) > 0;
        });
    }

    /**
     * Search task runs by given filter (pagination, date range, task ids, status, tags, etc.)
     *
     * @param filter filter value object
     * @return filtered list of task runs
     */
    public List<TaskRun> fetchTaskRunsByFilter(TaskRunSearchFilter filter) {
        Preconditions.checkNotNull(filter, "Invalid argument `filter`: null");
        Preconditions.checkArgument(Objects.isNull(filter.getPageNum()) || filter.getPageNum() > 0, "page number should be positive");
        Preconditions.checkArgument(Objects.isNull(filter.getPageSize()) || filter.getPageSize() > 0, "page size should be positive");

        int pageNum = Objects.nonNull(filter.getPageNum()) ? filter.getPageNum() : 1;
        int pageSize = Objects.nonNull(filter.getPageSize()) ? filter.getPageSize() : 100;
        String sortKey = Objects.nonNull(filter.getSortKey()) ? sortKeyToFieldMapper.get(filter.getSortKey()) : "start_at";
        String sortOrder = Objects.nonNull(filter.getSortOrder()) ? filter.getSortOrder() : "DESC";

        Pair<String, List<Object>> whereClauseAndParams = generateWhereClauseAndParamsByFilter(filter);
        String whereClause = whereClauseAndParams.getLeft();
        List<Object> params = whereClauseAndParams.getRight();

        String sql = getTaskRunSQLBuilderWithDefaultConfig()
                .where(whereClause)
                .orderBy(sortKey + " " + sortOrder)
                .limit(pageSize)
                .offset((pageNum - 1) * pageSize)
                .getSQL();

        return dbOperator.fetchAll(sql, taskRunMapperInstance, params.toArray());
    }

    public Integer fetchTotalCount() {
        return fetchTotalCountByFilter(TaskRunSearchFilter.newBuilder().build());
    }

    public Integer fetchTotalCountByFilter(TaskRunSearchFilter filter) {
        Preconditions.checkNotNull(filter, "Invalid argument `filter`: null");

        Pair<String, List<Object>> whereClauseAndParams = generateWhereClauseAndParamsByFilter(filter);
        String whereClause = whereClauseAndParams.getLeft();
        List<Object> params = whereClauseAndParams.getRight();

        String sql = DefaultSQLBuilder.newBuilder()
                .select("COUNT(*)")
                .from(TASK_RUN_TABLE_NAME, TASK_RUN_MODEL_NAME)
                .join("INNER", TaskDao.TASK_TABLE_NAME, TaskDao.TASK_MODEL_NAME)
                .on(TASK_RUN_MODEL_NAME + ".task_id = " + TaskDao.TASK_MODEL_NAME + ".id")
                .autoAliasColumns()
                .where(whereClause)
                .getSQL();

        return dbOperator.fetchOne(sql, rs -> rs.getInt(1), params.toArray());
    }

    public List<TaskRunDailyStatisticInfo> fetchTotalCountByDay(TaskRunSearchFilter filter, Integer offsetHour) {
        Pair<String, List<Object>> whereClauseAndParams = generateWhereClauseAndParamsByFilter(filter);
        String whereClause = whereClauseAndParams.getLeft();
        List<Object> params = whereClauseAndParams.getRight();

        String sql = DefaultSQLBuilder.newBuilder()
                .select(String.format("date_trunc('day', %s.end_at + interval '%s hour') \"day\", %s.status, count(1)", TASK_RUN_MODEL_NAME, offsetHour, TASK_RUN_MODEL_NAME))
                .from(TASK_RUN_TABLE_NAME, TASK_RUN_MODEL_NAME)
                .where(whereClause)
                .groupBy(TASK_RUN_MODEL_NAME + ".status, day")
                .orderBy("day ASC")
                .getSQL();

        return dbOperator.fetchAll(sql, rs -> {
            String dt = rs.getString(1);
            String statusString = rs.getString(2);
            return new TaskRunDailyStatisticInfo(
                    StringUtils.isNoneEmpty(dt) ?
                            OffsetDateTime.of(
                                    LocalDateTime.parse(dt, LOCAL_DATE_TIME_FORMATTER),
                                    ZoneOffset.ofHours(offsetHour)
                            ) :
                            null,
                    StringUtils.isNoneEmpty(statusString) ? TaskRunStatus.valueOf(statusString) : null,
                    rs.getInt(3)
            );
        }, params.toArray());
    }

    /**
     * Returns a list of value object
     *
     * @param taskRunId
     * @return
     */
    public List<TaskAttemptProps> fetchAttemptsPropByTaskRunId(Long taskRunId) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_ATTEMPT_MODEL_NAME, taskAttemptCols);

        String sql = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_ATTEMPT_TABLE_NAME, TASK_ATTEMPT_MODEL_NAME)
                .autoAliasColumns()
                .where(TASK_ATTEMPT_MODEL_NAME + ".task_run_id = ?")
                .getSQL();

        return dbOperator.fetchAll(sql, new TaskAttemptPropsMapper(TASK_ATTEMPT_MODEL_NAME), taskRunId);
    }

    /**
     * Returns a list of value object
     *
     * @param taskRunIds
     * @return
     */
    public List<TaskAttemptProps> fetchAttemptsPropByTaskRunIds(List<Long> taskRunIds) {
        if (CollectionUtils.isEmpty(taskRunIds)) {
            return Lists.newArrayList();
        }

        String idsFieldsPlaceholder = "(" + taskRunIds.stream().map(id -> "?")
                .collect(Collectors.joining(", ")) + ")";
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_ATTEMPT_MODEL_NAME, taskAttemptCols);
        String sql = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_ATTEMPT_TABLE_NAME, TASK_ATTEMPT_MODEL_NAME)
                .autoAliasColumns()
                .where(TASK_ATTEMPT_MODEL_NAME + ".task_run_id IN " + idsFieldsPlaceholder)
                .getSQL();

        return dbOperator.fetchAll(sql, new TaskAttemptPropsMapper(TASK_ATTEMPT_MODEL_NAME), taskRunIds.toArray());
    }

    /**
     * Fetch TaskAttempt instance by given id,
     * a TaskAttempt instance is nested with the TaskRun and Task model it derived from.
     *
     * @param attemptId
     * @return
     */
    public Optional<TaskAttempt> fetchAttemptById(Long attemptId) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_ATTEMPT_MODEL_NAME, taskAttemptCols);
        columnsMap.put(TASK_RUN_MODEL_NAME, taskRunCols);
        columnsMap.put(TaskDao.TASK_MODEL_NAME, TaskDao.getTaskCols());

        String sql = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_ATTEMPT_TABLE_NAME, TASK_ATTEMPT_MODEL_NAME)
                .join("INNER", TASK_RUN_TABLE_NAME, TASK_RUN_MODEL_NAME)
                .on(TASK_RUN_MODEL_NAME + ".id = " + TASK_ATTEMPT_MODEL_NAME + ".task_run_id")
                .join("INNER", TaskDao.TASK_TABLE_NAME, TaskDao.TASK_MODEL_NAME)
                .on(TaskDao.TASK_MODEL_NAME + ".id = " + TASK_RUN_MODEL_NAME + ".task_id")
                .autoAliasColumns()
                .where(TASK_ATTEMPT_MODEL_NAME + ".id = ?")
                .getSQL();

        return Optional.ofNullable(dbOperator.fetchOne(sql, new TaskAttemptMapper(TASK_ATTEMPT_MODEL_NAME, taskRunMapperInstance), attemptId));
    }

    public TaskAttempt createAttempt(TaskAttempt taskAttempt) {
        Optional<TaskAttempt> savedTaskAttempt = fetchAttemptById(taskAttempt.getId());
        if (savedTaskAttempt.isPresent()) {
            return savedTaskAttempt.get();
        }
        List<String> tableColumns = new ImmutableList.Builder<String>()
                .addAll(taskAttemptCols)
                .build();

        String sql = DefaultSQLBuilder.newBuilder()
                .insert(tableColumns.toArray(new String[0]))
                .into(TASK_ATTEMPT_TABLE_NAME)
                .asPrepared()
                .getSQL();

        dbOperator.update(sql,
                taskAttempt.getId(),
                taskAttempt.getTaskRun().getId(),
                taskAttempt.getAttempt(),
                toNullableString(taskAttempt.getStatus()),
                taskAttempt.getStartAt(),
                taskAttempt.getEndAt(),
                taskAttempt.getLogPath(),
                taskAttempt.getQueueName(),
                taskAttempt.getPriority()
        );
        return taskAttempt;
    }

    public Optional<TaskRunStatus> fetchTaskAttemptStatus(Long taskAttemptId) {
        checkNotNull(taskAttemptId, "taskAttemptId should not be null.");

        String sql = new DefaultSQLBuilder()
                .select("status")
                .from(TASK_ATTEMPT_TABLE_NAME)
                .where("id = ?")
                .getSQL();

        return Optional.ofNullable(
                dbOperator.fetchOne(sql, (rs) -> TaskRunStatus.valueOf(rs.getString("status")), taskAttemptId));
    }

    public Optional<TaskRunStatus> updateTaskAttemptStatus(Long taskAttemptId, TaskRunStatus status) {
        return updateTaskAttemptStatus(taskAttemptId, status, null, null);
    }

    public Optional<TaskRunStatus> updateTaskAttemptStatus(Long taskAttemptId, TaskRunStatus status,
                                                           @Nullable OffsetDateTime startAt, @Nullable OffsetDateTime endAt) {
        checkNotNull(taskAttemptId, "taskAttemptId should not be null.");
        checkNotNull(status, "status should not be null.");

        long taskRunId = WorkflowIdGenerator.taskRunIdFromTaskAttemptId(taskAttemptId);
        List<Object> pmTa = Lists.newArrayList();
        List<Object> pmTr = Lists.newArrayList();

        SQLBuilder sbTa = DefaultSQLBuilder.newBuilder()
                .update(TASK_ATTEMPT_TABLE_NAME)
                .set("status")
                .where("id = ?");
        pmTa.add(status.toString());

        SQLBuilder sbTr = DefaultSQLBuilder.newBuilder()
                .update(TASK_RUN_TABLE_NAME)
                .set("status")
                .where("id = ?");
        pmTr.add(status.toString());

        if (startAt != null) {
            sbTa.set("start_at");
            pmTa.add(startAt);
            sbTr.set("start_at");
            pmTr.add(startAt);
        }

        if (endAt != null) {
            sbTa.set("end_at");
            pmTa.add(endAt);
            sbTr.set("end_at");
            pmTr.add(endAt);
        }

        pmTa.add(taskAttemptId);
        pmTr.add(taskRunId);

        return dbOperator.transaction(() -> {
            Optional<TaskRunStatus> prev = fetchTaskAttemptStatus(taskAttemptId);
            if (prev.isPresent()) {
                dbOperator.update(sbTa.asPrepared().getSQL(), pmTa.toArray());
                dbOperator.update(sbTr.asPrepared().getSQL(), pmTr.toArray());
                if (status.isFinished()) {
                    DependencyStatus dependencyStatus = status.isSuccess() ? DependencyStatus.SUCCESS : DependencyStatus.FAILED;
                    updateTaskRunDependency(taskRunId, dependencyStatus);
                }
            }
            return prev;
        });
    }

    public List<TaskAttempt> fetchAllSatisfyTaskAttempt() {
        return dbOperator.transaction(() -> {
            List<Long> taskRunIdList = fetchAllSatisfyTaskRunId();
            return fetchAllLatestTaskAttempt(taskRunIdList);
        });
    }

    public List<TaskAttempt> fetchAllLatestTaskAttempt(List<Long> taskRunIdList) {
        if (taskRunIdList.size() == 0) {
            return Lists.newArrayList();
        }
        String taskRunIdFilter = "(" + taskRunIdList.stream().map(id -> "?")
                .collect(Collectors.joining(", ")) + ")";
        String fetchIdSql = DefaultSQLBuilder.newBuilder()
                .select("max(id) as task_attempt_id")
                .from(TASK_ATTEMPT_TABLE_NAME)
                .where("task_run_id in " + taskRunIdFilter)
                .groupBy("task_run_id")
                .asPrepared()
                .getSQL();
        List<Long> taskAttemptIdList = dbOperator.fetchAll(fetchIdSql, rs -> rs.getLong("task_attempt_id"), taskRunIdList.toArray());
        return fetchTaskAttemptByIds(taskAttemptIdList);


    }

    public List<TaskAttempt> fetchTaskAttemptByIds(List<Long> taskAttemptIdList) {
        if (taskAttemptIdList.size() == 0) {
            return new ArrayList<>();
        }
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_ATTEMPT_MODEL_NAME, taskAttemptCols);
        columnsMap.put(TASK_RUN_MODEL_NAME, taskRunCols);
        columnsMap.put(TaskDao.TASK_MODEL_NAME, TaskDao.getTaskCols());

        String attemptIdFilter = "(" + taskAttemptIdList.stream().map(x -> "?").collect(Collectors.joining(",")) + ")";

        String sql = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_ATTEMPT_TABLE_NAME, TASK_ATTEMPT_MODEL_NAME)
                .join("INNER", TASK_RUN_TABLE_NAME, TASK_RUN_MODEL_NAME)
                .on(TASK_RUN_MODEL_NAME + ".id = " + TASK_ATTEMPT_MODEL_NAME + ".task_run_id")
                .join("INNER", TaskDao.TASK_TABLE_NAME, TaskDao.TASK_MODEL_NAME)
                .on(TaskDao.TASK_MODEL_NAME + ".id = " + TASK_RUN_MODEL_NAME + ".task_id")
                .autoAliasColumns()
                .where(TASK_ATTEMPT_MODEL_NAME + ".id in " + attemptIdFilter)
                .getSQL();
        return dbOperator.fetchAll(sql, new TaskAttemptMapper(TASK_ATTEMPT_MODEL_NAME, taskRunMapperInstance), taskAttemptIdList.toArray());
    }

    public Optional<TaskRunStatus> updateTaskAttemptExecutionTime(Long taskAttemptId, @Nullable OffsetDateTime startAt,
                                                                  @Nullable OffsetDateTime endAt) {
        checkNotNull(taskAttemptId, "taskAttemptId should not be null.");

        long taskRunId = WorkflowIdGenerator.taskRunIdFromTaskAttemptId(taskAttemptId);
        List<Object> pmTa = Lists.newArrayList();
        List<Object> pmTr = Lists.newArrayList();

        SQLBuilder sbTa = DefaultSQLBuilder.newBuilder()
                .update(TASK_ATTEMPT_TABLE_NAME)
                .where("id = ?");

        SQLBuilder sbTr = DefaultSQLBuilder.newBuilder()
                .update(TASK_RUN_TABLE_NAME)
                .where("id = ?");

        if (startAt != null) {
            sbTa.set("start_at");
            pmTa.add(startAt);
            sbTr.set("start_at");
            pmTr.add(startAt);
        }

        if (endAt != null) {
            sbTa.set("end_at");
            pmTa.add(endAt);
            sbTr.set("end_at");
            pmTr.add(endAt);
        }

        pmTa.add(taskAttemptId);
        pmTr.add(taskRunId);

        return dbOperator.transaction(() -> {
            Optional<TaskRunStatus> prev = fetchTaskAttemptStatus(taskAttemptId);
            if (prev.isPresent()) {
                dbOperator.update(sbTa.asPrepared().getSQL(), pmTa.toArray());
                dbOperator.update(sbTr.asPrepared().getSQL(), pmTr.toArray());
            }
            return prev;
        });
    }

    public boolean updateTaskAttemptLogPath(Long taskAttemptId, String logPath) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(TASK_ATTEMPT_TABLE_NAME)
                .set("log_path")
                .where("id = ?")
                .asPrepared()
                .getSQL();
        return dbOperator.update(sql, logPath, taskAttemptId) >= 0;
    }

    private boolean deleteTaskAttempts(Long taskRunId) {
        String dependencySQL = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(TASK_ATTEMPT_TABLE_NAME)
                .where("task_run_id = ?")
                .getSQL();
        return dbOperator.update(dependencySQL, taskRunId) >= 0;
    }

    private List<Long> fetchTaskRunDependencies(Long taskRunId) {
        String dependencySQL = DefaultSQLBuilder.newBuilder()
                .select("upstream_task_run_id")
                .from(RELATION_TABLE_NAME)
                .where("downstream_task_run_id = ?")
                .getSQL();
        return dbOperator.fetchAll(dependencySQL, rs -> rs.getLong(1), taskRunId);
    }

    private boolean deleteTaskRunDependencies(Long taskRunId) {
        String dependencySQL = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(RELATION_TABLE_NAME)
                .where("downstream_task_run_id = ?")
                .getSQL();
        return dbOperator.update(dependencySQL, taskRunId) >= 0;
    }

    private void createTaskRunDependencies(Long taskRunId, List<Long> dependencies, Task task) {
        if (dependencies.isEmpty()) return;
        Map<Long, DependencyLevel> levelMap = new HashMap<>();
        for (TaskDependency taskDependency : task.getDependencies()) {
            levelMap.put(taskDependency.getUpstreamTaskId(), taskDependency.getDependencyLevel());
        }
        List<TaskRunDependency> taskRunDependencyList = new ArrayList<>();
        for (Long upstreamTaskRunId : dependencies) {
            TaskRun upstreamTaskRun = fetchTaskRunById(upstreamTaskRunId).get();
            DependencyStatus dependencyStatus = DependencyStatus.CREATED;
            if (upstreamTaskRun.getStatus() != null && upstreamTaskRun.getStatus().isFinished()) {
                dependencyStatus = upstreamTaskRun.getStatus().isSuccess() ? DependencyStatus.SUCCESS : DependencyStatus.FAILED;
            }
            taskRunDependencyList.add(new TaskRunDependency(upstreamTaskRunId, taskRunId, levelMap.get(upstreamTaskRun.getTask().getId()), dependencyStatus));
        }

        List<String> tableColumns = new ImmutableList.Builder<String>()
                .addAll(taskRunRelationCols)
                .build();

        String dependencySQL = DefaultSQLBuilder.newBuilder()
                .insert(tableColumns.toArray(new String[0]))
                .into(RELATION_TABLE_NAME)
                .asPrepared()
                .getSQL();

        Object[][] params = new Object[dependencies.size()][2];
        for (int i = 0; i < taskRunDependencyList.size(); i++) {
            params[i] = new Object[]{taskRunDependencyList.get(i).getUpstreamTaskRunId(),
                    taskRunId, taskRunDependencyList.get(i).getDependencyLevel().name(), taskRunDependencyList.get(i).getDependencyStatus().name()};
        }

        dbOperator.batch(dependencySQL, params);
    }

    public TaskRun fetchLatestTaskRun(Long taskId) {
        List<TaskRun> latestRunInList = fetchLatestTaskRuns(taskId, 1);
        if (latestRunInList.isEmpty()) {
            return null;
        }
        // else
        return latestRunInList.get(0);
    }

    public TaskRun fetchLatestTaskRunToday(Long taskId) {
        List<TaskRun> latestRunInList = fetchLatestTaskRunsToday(taskId, 1);
        if (latestRunInList.isEmpty()) {
            return null;
        }
        // else
        return latestRunInList.get(0);
    }

    public List<TaskRun> fetchLatestTaskRuns(Long taskId, int limit) {
        checkNotNull(taskId, "taskId should not be null.");

        String sql = getTaskRunSQLBuilderWithDefaultConfig()
                .where("task_id = ?")
                .orderBy(TASK_RUN_MODEL_NAME + ".id DESC")
                .limit(limit)
                .getSQL();
        return dbOperator.fetchAll(sql, taskRunMapperInstance, taskId);
    }

    public Map<Long, List<TaskRun>> fetchLatestTaskRunsByBatch(List<Long> taskIds, int limitPerTask) {
        String subQuery = getTaskRunSQLBuilderForLatestTaskRuns()
                .where("task_id IN (" + repeatJoin("?", ",", taskIds.size()) + ")")
                .orderBy(TASK_RUN_MODEL_NAME + ".task_id DESC, " + TASK_RUN_MODEL_NAME + ".id DESC")
                .getSQL();
        String sql = "SELECT * FROM (" + subQuery + ") as t WHERE t.row_num <= ?";
        List<Object> params = new ArrayList<>(taskIds.size() + 1);
        for (Long taskId : taskIds) {
            params.add(taskId);
        }
        params.add(limitPerTask);

        List<TaskRun> taskRunsList = dbOperator.fetchAll(sql, taskRunMapperInstance, params.toArray());

        Map<Long, List<TaskRun>> resultMap = new HashMap<>();
        for (Long taskId : taskIds) {
            resultMap.put(taskId, new LinkedList<>());
        }
        for (TaskRun taskRun : taskRunsList) {
            Long taskId = taskRun.getTask().getId();
            resultMap.get(taskId).add(taskRun);
        }

        return resultMap;
    }

    public List<TaskRun> fetchLatestTaskRunsToday(Long taskId, int limit) {
        checkNotNull(taskId, "taskId should not be null.");
        OffsetDateTime todayMorning = DateTimeUtils.todayMorning();
        String sql = getTaskRunSQLBuilderWithDefaultConfig()
                .where("task_id = ? and " + TASK_RUN_MODEL_NAME + ".created_at > ?")
                .orderBy(TASK_RUN_MODEL_NAME + ".created_at DESC")
                .limit(limit)
                .getSQL();
        return dbOperator.fetchAll(sql, taskRunMapperInstance, taskId, todayMorning);
    }

    public TaskAttemptProps fetchLatestTaskAttempt(Long taskRunId) {
        Preconditions.checkNotNull(taskRunId, "task run id should not be null");

        List<TaskAttemptProps> attemptPropsList = fetchLatestTaskAttempt(Lists.newArrayList(taskRunId));
        if (attemptPropsList.isEmpty()) {
            return null;
        }
        return attemptPropsList.get(0);
    }

    public List<TaskAttemptProps> fetchLatestTaskAttempt(List<Long> taskRunIds) {
        checkNotNull(taskRunIds, "taskRunIds should not be null.");

        if (taskRunIds.isEmpty()) {
            return Collections.emptyList();
        }

        String idsFieldsPlaceholder = "(" + taskRunIds.stream().map(id -> "?")
                .collect(Collectors.joining(", ")) + ")";

        String orderClause = "(" + taskRunIds.stream().map(id -> TASK_ATTEMPT_MODEL_NAME + ".task_run_id = " + id)
                .collect(Collectors.joining(", ")) + ") DESC";

        String subSelectSql = DefaultSQLBuilder.newBuilder()
                .select("u.id")
                .from(TASK_ATTEMPT_TABLE_NAME, "u")
                .where(TASK_ATTEMPT_MODEL_NAME + ".task_run_id = u.task_run_id")
                .orderBy("u.attempt DESC")
                .limit(1)
                .getSQL();
        String sql = DefaultSQLBuilder.newBuilder()
                .select(taskAttemptCols.toArray(new String[0]))
                .from(TASK_ATTEMPT_TABLE_NAME, TASK_ATTEMPT_MODEL_NAME)
                .where(TASK_ATTEMPT_MODEL_NAME + ".task_run_id IN " + idsFieldsPlaceholder + " AND " + TASK_ATTEMPT_MODEL_NAME + ".id IN (" + subSelectSql + ")")
                .autoAliasColumns()
                .orderBy(orderClause)
                .getSQL();

        return dbOperator.fetchAll(sql, new TaskAttemptPropsMapper(), taskRunIds.toArray());
    }

    /**
     * Fetch and return upstream task runs within maximum distance from given source
     *
     * @param srcTaskRunId id of source task run
     * @param distance     max distance from source task, required to be positive
     * @param includeSelf  whether source task run should be included
     * @return list of upstream task runs
     * @throws IllegalArgumentException when distance is illegal
     */
    public List<TaskRun> fetchUpstreamTaskRunsById(Long srcTaskRunId, int distance, boolean includeSelf) {
        Preconditions.checkNotNull(srcTaskRunId, "Invalid argument `srcTaskRunId`: null");
        Preconditions.checkArgument(distance > 0, "Argument `distance` should be positive, but found: %d", distance);

        return fetchDependentTaskRunsById(srcTaskRunId, distance, DependencyDirection.UPSTREAM, includeSelf);
    }

    /**
     * Fetch and return downstream task runs within maximum distance from given source
     *
     * @param srcTaskRunId id of source task run
     * @param distance     max distance from source task, required to be positive
     * @param includeSelf  whether source task run should be included
     * @return list of downstream task runs
     * @throws IllegalArgumentException when distance is illegal
     */
    public List<TaskRun> fetchDownstreamTaskRunsById(Long srcTaskRunId, int distance, boolean includeSelf) {
        Preconditions.checkNotNull(srcTaskRunId, "Invalid argument `srcTaskRunId`: null");
        Preconditions.checkArgument(distance > 0, "Argument `distance` should be positive, but found: %d", distance);

        return fetchDependentTaskRunsById(srcTaskRunId, distance, DependencyDirection.DOWNSTREAM, includeSelf);
    }

    public List<TaskRun> fetchTaskRunListWithoutAttempt() {
        String whereCase = TASK_RUN_MODEL_NAME + ".status is NULL " +
                "and " + TASK_RUN_MODEL_NAME + ".created_at > ?";
        String sql = getTaskRunSQLBuilderWithDefaultConfig()
                .where(whereCase)
                .getSQL();
        OffsetDateTime recoverLimit = DateTimeUtils.now().plusDays(-1);
        List<TaskRun> taskRunList = dbOperator.fetchAll(sql, taskRunMapperInstance, recoverLimit);
        Map<Long, List<Long>> taskRunRelations = fetchAllRelationsFromDownstreamTaskRunIds(taskRunList.stream().map(TaskRun::getId).collect(Collectors.toList()));
        return taskRunList.stream().map(taskRun -> taskRun.cloneBuilder()
                .withDependentTaskRunIds(taskRunRelations.get(taskRun.getId()))
                .build()).collect(Collectors.toList());
    }

    /**
     * Fetch all relations in a `id - [dependencies]` hashmap whose downstream taskRun ID is included in the given list
     *
     * @param taskRunIds list of downstream taskRun IDs
     * @return
     */
    private Map<Long, List<Long>> fetchAllRelationsFromDownstreamTaskRunIds(List<Long> taskRunIds) {
        if (taskRunIds.isEmpty()) {
            return new HashMap<>();
        }

        Map<Long, List<Long>> taskRunIdToDependenciesMap = new HashMap<>();
        taskRunIds.forEach(taskRunId -> {
            taskRunIdToDependenciesMap.put(taskRunId, new ArrayList<>());
        });
        String idsFieldsPlaceholder = "(" + taskRunIds.stream().map(id -> "?")
                .collect(Collectors.joining(", ")) + ")";
        Map<String, List<String>> taskRunRelationColumnsMap = new HashMap<>();
        taskRunRelationColumnsMap.put(RELATION_MODEL_NAME, taskRunRelationCols);
        String sql = DefaultSQLBuilder.newBuilder()
                .columns(taskRunRelationColumnsMap)
                .from(RELATION_TABLE_NAME, RELATION_MODEL_NAME)
                .autoAliasColumns()
                .where(RELATION_MODEL_NAME + ".downstream_task_run_id IN " + idsFieldsPlaceholder)
                .orderBy(RELATION_MODEL_NAME + ".upstream_task_run_id ASC")
                .asPrepared()
                .getSQL();
        List<TaskRunDependency> allDeps = dbOperator.fetchAll(sql, TaskRunDependencyMapper.getInstance(), taskRunIds.toArray());
        allDeps.forEach(dep -> {
            List<Long> dependencyList = taskRunIdToDependenciesMap.get(dep.getDownStreamTaskRunId());
            dependencyList.add(dep.getUpstreamTaskRunId());
            taskRunIdToDependenciesMap.put(dep.getDownStreamTaskRunId(), dependencyList);
        });
        return taskRunIdToDependenciesMap;
    }

    public List<TaskAttempt> fetchUnStartedTaskAttemptList() {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_ATTEMPT_MODEL_NAME, taskAttemptCols);
        columnsMap.put(TASK_RUN_MODEL_NAME, taskRunCols);
        columnsMap.put(TaskDao.TASK_MODEL_NAME, TaskDao.getTaskCols());

        String whereCase = TASK_ATTEMPT_MODEL_NAME + ".status in (?,?) " +
                "and " + TASK_ATTEMPT_MODEL_NAME + ".created_at > ?";
        String sql = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_ATTEMPT_TABLE_NAME, TASK_ATTEMPT_MODEL_NAME)
                .join("INNER", TASK_RUN_TABLE_NAME, TASK_RUN_MODEL_NAME)
                .on(TASK_RUN_MODEL_NAME + ".id = " + TASK_ATTEMPT_MODEL_NAME + ".task_run_id")
                .join("INNER", TaskDao.TASK_TABLE_NAME, TaskDao.TASK_MODEL_NAME)
                .on(TaskDao.TASK_MODEL_NAME + ".id = " + TASK_RUN_MODEL_NAME + ".task_id")
                .autoAliasColumns()
                .where(whereCase)
                .getSQL();
        OffsetDateTime recoverLimit = DateTimeUtils.now().plusDays(-1);
        return dbOperator.fetchAll(sql, new TaskAttemptMapper(TASK_ATTEMPT_MODEL_NAME, taskRunMapperInstance),
                TaskRunStatus.QUEUED.toString(), TaskRunStatus.ERROR.toString(), recoverLimit);
    }

    public List<TaskAttempt> fetchRunningTaskAttemptList() {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_ATTEMPT_MODEL_NAME, taskAttemptCols);
        columnsMap.put(TASK_RUN_MODEL_NAME, taskRunCols);
        columnsMap.put(TaskDao.TASK_MODEL_NAME, TaskDao.getTaskCols());

        String whereCase = TASK_ATTEMPT_MODEL_NAME + ".status in (?,?) " +
                "and " + TASK_ATTEMPT_MODEL_NAME + ".created_at > ?";
        String sql = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_ATTEMPT_TABLE_NAME, TASK_ATTEMPT_MODEL_NAME)
                .join("INNER", TASK_RUN_TABLE_NAME, TASK_RUN_MODEL_NAME)
                .on(TASK_RUN_MODEL_NAME + ".id = " + TASK_ATTEMPT_MODEL_NAME + ".task_run_id")
                .join("INNER", TaskDao.TASK_TABLE_NAME, TaskDao.TASK_MODEL_NAME)
                .on(TaskDao.TASK_MODEL_NAME + ".id = " + TASK_RUN_MODEL_NAME + ".task_id")
                .autoAliasColumns()
                .where(whereCase)
                .getSQL();
        OffsetDateTime recoverLimit = DateTimeUtils.now().plusDays(-1);
        return dbOperator.fetchAll(sql, new TaskAttemptMapper(TASK_ATTEMPT_MODEL_NAME, taskRunMapperInstance), TaskRunStatus.INITIALIZING.toString(),
                TaskRunStatus.RUNNING.toString(), recoverLimit);
    }


    //创建TaskRun依赖
    public void createTaskRunDependency(TaskRunDependency dependency) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(taskRunRelationCols.toArray(new String[0]))
                .into(RELATION_TABLE_NAME)
                .asPrepared()
                .getSQL();
        dbOperator.create(sql, dependency.getUpstreamTaskRunId(), dependency.getDownStreamTaskRunId(),
                dependency.getDependencyLevel().name(), dependency.getDependencyStatus().name());
    }

    //根据taskRunId更新taskRun依赖
    public void updateTaskRunDependency(long taskRunId, DependencyStatus status) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(RELATION_TABLE_NAME)
                .set("dependency_status")
                .where("upstream_task_run_id = ?")
                .asPrepared()
                .getSQL();
        dbOperator.update(sql, status.name(), taskRunId);
    }

    public void updateTaskRunDependencyByTaskRunIds(List<Long> taskRunIds, DependencyStatus status) {
        String filterTaskRunId = taskRunIds.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = DefaultSQLBuilder.newBuilder()
                .update(RELATION_TABLE_NAME)
                .set("dependency_status")
                .where("upstream_task_run_id in " + "(" + filterTaskRunId + ")")
                .asPrepared()
                .getSQL();
        dbOperator.update(sql, status.name(), taskRunIds.toArray());
    }

    public List<Long> fetchAllSatisfyTaskRunId() {
        OffsetDateTime notifyLimit = DateTimeUtils.now().plusDays(-1);
        String sql = DefaultSQLBuilder.newBuilder()
                .select("id")
                .from(TASK_RUN_TABLE_NAME, TASK_RUN_MODEL_NAME)
                .join("LEFT", RELATION_TABLE_NAME, RELATION_MODEL_NAME)
                .on(TASK_RUN_MODEL_NAME + ".id = " + RELATION_MODEL_NAME + ".downstream_task_run_id")
                .where(TASK_RUN_MODEL_NAME + ".status = ? and " + TASK_RUN_MODEL_NAME + ".created_at > ?")
                .groupBy("id")
                .having("sum(case when dependency_status = ? or (dependency_level = ? and dependency_status = ?) then 1 else 0 end) = 0")
                .asPrepared()
                .getSQL();
        List<Long> satisfyTaskRunId = dbOperator.fetchAll(sql, rs -> rs.getLong("id"), TaskRunStatus.CREATED.name(), notifyLimit,
                DependencyStatus.CREATED.name(), DependencyLevel.STRONG.name(), DependencyStatus.FAILED.name());
        return satisfyTaskRunId;
    }


    public static class TaskRunDependencyMapper implements ResultSetMapper<TaskRunDependency> {

        private static TaskRunDao.TaskRunDependencyMapper instance;

        public static TaskRunDao.TaskRunDependencyMapper getInstance() {
            if (instance == null) {
                instance = new TaskRunDao.TaskRunDependencyMapper();
            }
            return instance;
        }

        @Override
        public TaskRunDependency map(ResultSet rs) throws SQLException {
            return new TaskRunDependency(
                    rs.getLong(RELATION_MODEL_NAME + "_upstream_task_run_id"),
                    rs.getLong(RELATION_MODEL_NAME + "_downstream_task_run_id"),
                    DependencyLevel.resolve(rs.getString(RELATION_MODEL_NAME + "_dependency_level")),
                    DependencyStatus.resolve(rs.getString(RELATION_MODEL_NAME + "_dependency_status"))
            );
        }
    }


    public static class TaskRunMapper implements ResultSetMapper<TaskRun> {
        public static final TaskRunDao.TaskRunMapper INSTANCE = new TaskRunDao.TaskRunMapper();
        private final TaskDao.TaskMapper taskMapper = TaskDao.TaskMapper.INSTANCE;

        @Override
        public TaskRun map(ResultSet rs) throws SQLException {
            rs.getLong(TaskRunDao.TASK_RUN_MODEL_NAME + "_task_id");
            Task task = !rs.wasNull() ? taskMapper.map(rs) : null;

            return TaskRun.newBuilder()
                    .withTask(task)
                    .withId(rs.getLong(TASK_RUN_MODEL_NAME + "_id"))
                    .withScheduledTick(new Tick(rs.getString(TASK_RUN_MODEL_NAME + "_scheduled_tick")))
                    .withStatus(TaskRunStatus.resolve(rs.getString(TASK_RUN_MODEL_NAME + "_status")))
                    .withScheduleType(ScheduleType.valueOf(rs.getString(TASK_RUN_MODEL_NAME + "_schedule_type")))
                    .withInlets(JSONUtils.jsonToObject(rs.getString(TASK_RUN_MODEL_NAME + "_inlets"), new TypeReference<List<DataStore>>() {
                    }))
                    .withOutlets(JSONUtils.jsonToObject(rs.getString(TASK_RUN_MODEL_NAME + "_outlets"), new TypeReference<List<DataStore>>() {
                    }))
                    .withDependentTaskRunIds(Collections.emptyList())
                    .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_start_at")))
                    .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_end_at")))
                    .withCreatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_created_at")))
                    .withUpdatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_updated_at")))
                    .withConfig(JSONUtils.jsonToObject(rs.getString(TASK_RUN_MODEL_NAME + "_config"), Config.class))
                    .withQueueName(rs.getString(TASK_RUN_MODEL_NAME + "_queue_name"))
                    .withPriority(rs.getInt(TASK_RUN_MODEL_NAME + "_priority"))
                    .build();
        }
    }

    private static class TaskAttemptMapper implements ResultSetMapper<TaskAttempt> {
        private String tableAlias;

        private TaskRunMapper taskRunMapper;

        public TaskAttemptMapper(String tableAlias, TaskRunMapper taskRunMapper) {
            this.tableAlias = tableAlias;
            this.taskRunMapper = taskRunMapper;
        }

        @Override
        public TaskAttempt map(ResultSet rs) throws SQLException {
            return TaskAttempt.newBuilder()
                    .withId(rs.getLong(column("id", tableAlias)))
                    .withTaskRun(taskRunMapper.map(rs))
                    .withStatus(TaskRunStatus.resolve(rs.getString(column("status", tableAlias))))
                    .withAttempt(rs.getInt(column("attempt", tableAlias)))
                    .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(column("start_at", tableAlias))))
                    .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(column("end_at", tableAlias))))
                    .withLogPath(rs.getString(column("log_path", tableAlias)))
                    .withQueueName(rs.getString(column("queue_name", tableAlias)))
                    .withPriority(rs.getInt(column("priority", tableAlias)))
                    .build();
        }
    }

    private static class TaskAttemptPropsMapper implements ResultSetMapper<TaskAttemptProps> {
        private String tableAlias;

        public TaskAttemptPropsMapper() {
            this("");
        }

        public TaskAttemptPropsMapper(String tableAlias) {
            this.tableAlias = tableAlias;
        }

        @Override
        public TaskAttemptProps map(ResultSet rs) throws SQLException {
            return TaskAttemptProps.newBuilder()
                    .withId(rs.getLong(column("id", tableAlias)))
                    .withTaskRunId(rs.getLong(column("task_run_id", tableAlias)))
                    .withAttempt(rs.getInt(column("attempt", tableAlias)))
                    .withStatus(TaskRunStatus.valueOf(rs.getString(column("status", tableAlias))))
                    .withLogPath(rs.getString(column("log_path", tableAlias)))
                    .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(column("start_at", tableAlias))))
                    .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(column("end_at", tableAlias))))
                    .withQueueName(rs.getString(column("queue_name", tableAlias)))
                    .build();
        }
    }
}
