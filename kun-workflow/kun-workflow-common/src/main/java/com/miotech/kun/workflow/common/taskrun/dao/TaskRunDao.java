package com.miotech.kun.workflow.common.taskrun.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.bo.TaskRunDailyStatisticInfo;
import com.miotech.kun.workflow.common.taskrun.bo.TaskRunProps;
import com.miotech.kun.workflow.common.taskrun.filter.TaskRunSearchFilter;
import com.miotech.kun.workflow.common.tick.TickDao;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Condition;
import com.miotech.kun.workflow.core.model.common.SpecialTick;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import com.miotech.kun.workflow.core.model.task.*;
import com.miotech.kun.workflow.core.model.taskrun.*;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

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
    protected static final String TASK_RUN_MODEL_NAME = "taskrun";
    protected static final String TASK_RUN_TABLE_NAME = "kun_wf_task_run";
    private static final List<String> taskRunCols = ImmutableList.of("id", "task_id", "scheduled_tick", "status", "schedule_type", "queued_at", "start_at", "end_at", "term_at", "config", "inlets", "outlets", "failed_upstream_task_run_ids", "created_at", "updated_at", "queue_name", "priority", "target", "executor_label", "schedule_time");
    private static final List<String> taskRunPropsCols = ImmutableList.of("id", "scheduled_tick", "status", "schedule_type", "queued_at", "start_at", "end_at", "config", "created_at", "updated_at", "queue_name", "priority", "target");

    private static final String TASK_RUN_STAT_MODEL_NAME = "task_run_stat";
    private static final String TASK_RUN_STAT_TABLE_NAME = "kun_wf_task_run_stat";
    private static final List<String> taskRunStatCols = ImmutableList.of("task_run_id", "average_running_time", "average_queuing_time");

    private static final String TASK_ATTEMPT_MODEL_NAME = "taskattempt";
    private static final String TASK_ATTEMPT_TABLE_NAME = "kun_wf_task_attempt";
    private static final List<String> taskAttemptCols = ImmutableList.of("id", "task_run_id", "attempt", "status", "start_at", "end_at", "log_path", "queue_name", "priority", "retry_times", "executor_label", "runtime_label");

    private static final String RELATION_TABLE_NAME = "kun_wf_task_run_relations";
    private static final String RELATION_MODEL_NAME = "task_run_relations";
    private static final List<String> taskRunRelationCols = ImmutableList.of("upstream_task_run_id", "downstream_task_run_id", "dependency_level", "dependency_status");

    private static final String CONDITION_TABLE_NAME = "kun_wf_task_run_conditions";
    private static final String CONDITION_MODEL_NAME = "task_run_conditions";
    private static final List<String> taskRunConditionCols = ImmutableList.of("task_run_id", "condition", "result", "type", "created_at", "updated_at");

    private static final Map<String, String> sortKeyToFieldMapper = new HashMap<>();

    private static final Integer RECOVER_LIMIT_DAYS = 3;

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
    private TaskRunConditionMapper taskRunConditionMapperInstance;

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

    public String getTickByTaskRunId(Long taskRunId) {
        return dbOperator.fetchOne("SELECT scheduled_tick FROM " + TASK_RUN_TABLE_NAME + " WHERE id = ?",
                rs -> rs.getString("scheduled_tick"), taskRunId);
    }

    public String getScheduleTimeByTaskRunId(Long taskRunId) {
        return dbOperator.fetchOne("SELECT schedule_time FROM " + TASK_RUN_TABLE_NAME + " WHERE id = ?",
                rs -> rs.getString("schedule_time"), taskRunId);
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

    public List<Optional<TaskRun>> fetchTaskRunsByIds(Collection<Long> taskRunIds) {
        if (taskRunIds.isEmpty()) {
            return Collections.singletonList(Optional.empty());
        }
        String sql = getTaskRunSQLBuilderWithDefaultConfig()
                .where(TASK_RUN_MODEL_NAME + ".id IN (" + repeatJoin("?", ",", taskRunIds.size()) + ")")
                .getSQL();
        List<TaskRun> result = dbOperator.fetchAll(sql, taskRunMapperInstance, taskRunIds.toArray());

        return result.stream()
                .map(x -> x.cloneBuilder()
                        .withDependentTaskRunIds(fetchTaskRunDependencies(x.getId()))
                        .build())
                .map(Optional::of)
                .collect(Collectors.toList());
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
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".created_at < ? )");
            sqlArgs.add(filter.getDateTo());
        }

        if (Objects.nonNull(filter.getStartFrom())) {
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".start_at >= ? )");
            sqlArgs.add(filter.getStartFrom());
        }

        if (Objects.nonNull(filter.getStartTo())) {
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".start_at < ? )");
            sqlArgs.add(filter.getStartTo());
        }

        if (Objects.nonNull(filter.getQueueFrom())) {
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".queued_at >= ? )");
            sqlArgs.add(filter.getQueueFrom());
        }

        if (Objects.nonNull(filter.getQueueTo())) {
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".queued_at < ? )");
            sqlArgs.add(filter.getQueueTo());
        }

        if (Objects.nonNull(filter.getEndBefore())) {
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".term_at < ? )");
            sqlArgs.add(filter.getEndBefore());
        }

        if (Objects.nonNull(filter.getEndAfter())) {
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".term_at >= ?  or " + TASK_RUN_MODEL_NAME + ".term_at is NULL)");
            sqlArgs.add(filter.getEndAfter());
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
                    taskRun.getQueuedAt(),
                    taskRun.getStartAt(),
                    taskRun.getEndAt(),
                    taskRun.getTermAt(),
                    JSONUtils.toJsonString(taskRun.getConfig()),
                    JSONUtils.toJsonString(taskRun.getInlets()),
                    JSONUtils.toJsonString(taskRun.getOutlets()),
                    JSONUtils.toJsonString(taskRun.getFailedUpstreamTaskRunIds()),
                    now,
                    now,
                    taskRun.getQueueName(),
                    taskRun.getPriority(),
                    JSONUtils.toJsonString(taskRun.getExecuteTarget()),
                    taskRun.getExecutorLabel(),
                    taskRun.getScheduleTime().toString()
            );

            createTaskRunDependencies(taskRun.getId(), taskRun.getDependentTaskRunIds(), taskRun.getTask());
            if (taskRun.getTaskRunConditions() != null) {
                createTaskRunConditions(taskRun.getId(), taskRun.getTaskRunConditions());
            }
            return taskRun;
        });

        return taskRun;
    }

    public TaskRun fetchTaskRunByTaskAndTick(Long taskId, Tick tick) {
        String sql = getTaskRunSQLBuilderWithDefaultConfig()
                .where(TASK_RUN_MODEL_NAME + ".task_id = ? and " + "scheduled_tick = ?")
                .limit(1)
                .getSQL();
        TaskRun taskRun = dbOperator.fetchOne(sql, taskRunMapperInstance, taskId, tick.toString());
        return taskRun;
    }

    /**
     * 'termAt' is a column of TaskRun and TaskAttempt for internal use only. So we refrain from providing a public
     * accessible way.
     *
     * @param taskRunId
     * @return
     */
    @VisibleForTesting
    public OffsetDateTime getTermAtOfTaskRun(Long taskRunId) {
        return dbOperator.fetchOne("SELECT term_at FROM " + TASK_RUN_TABLE_NAME + " WHERE id = ?",
                rs -> DateTimeUtils.fromTimestamp(rs.getTimestamp("term_at")), taskRunId);
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
                    taskRun.getQueuedAt(),
                    taskRun.getStartAt(),
                    taskRun.getEndAt(),
                    taskRun.getTermAt(),
                    JSONUtils.toJsonString(taskRun.getConfig()),
                    JSONUtils.toJsonString(taskRun.getInlets()),
                    JSONUtils.toJsonString(taskRun.getOutlets()),
                    JSONUtils.toJsonString(taskRun.getFailedUpstreamTaskRunIds()),
                    now,
                    now,
                    taskRun.getQueueName(),
                    taskRun.getPriority(),
                    JSONUtils.toJsonString(taskRun.getExecuteTarget()),
                    taskRun.getExecutorLabel(),
                    taskRun.getScheduleTime().toString()
            );

            createTaskRunDependencies(taskRun.getId(), taskRun.getDependentTaskRunIds(), taskRun.getTask());
            if (taskRun.getTaskRunConditions() != null) {
                createTaskRunConditions(taskRun.getId(), taskRun.getTaskRunConditions());
            }
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
                    taskRun.getQueuedAt(),
                    taskRun.getStartAt(),
                    taskRun.getEndAt(),
                    taskRun.getTermAt(),
                    JSONUtils.toJsonString(taskRun.getConfig()),
                    JSONUtils.toJsonString(taskRun.getInlets()),
                    JSONUtils.toJsonString(taskRun.getOutlets()),
                    JSONUtils.toJsonString(taskRun.getFailedUpstreamTaskRunIds()),
                    taskRun.getCreatedAt(),       // created_at
                    now,                          // updated_at
                    taskRun.getQueueName(),
                    taskRun.getPriority(),
                    JSONUtils.toJsonString(taskRun.getExecuteTarget()),
                    taskRun.getExecutorLabel(),
                    taskRun.getScheduleTime().toString(),
                    taskRun.getId()
            );

            deleteTaskRunDependencies(taskRun.getId());
            createTaskRunDependencies(taskRun.getId(), taskRun.getDependentTaskRunIds(), taskRun.getTask());
            deleteTaskRunConditions(taskRun.getId());
            if (taskRun.getTaskRunConditions() != null) {
                createTaskRunConditions(taskRun.getId(), taskRun.getTaskRunConditions());
            }
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

    public void resetTaskRunTimestampToNull(List<Long> taskRunIds, String timestampName) {
        if (taskRunIds == null || taskRunIds.size() == 0) {
            return;
        }

        List<Long> taskAttemptIds = fetchAllLatestTaskAttemptIds(taskRunIds);

        String updateTaskRunSql = DefaultSQLBuilder.newBuilder()
                .update(TASK_RUN_TABLE_NAME)
                .set(timestampName + "= null")
                .where("id in (" + repeatJoin("?", ",", taskRunIds.size()) + ")")
                .getSQL();
        String updateTaskAttemptSql = DefaultSQLBuilder.newBuilder()
                .update(TASK_ATTEMPT_TABLE_NAME)
                .set(timestampName + "= null")
                .where("id in (" + repeatJoin("?", ",", taskAttemptIds.size()) + ")")
                .getSQL();
        dbOperator.transaction(() -> {
            dbOperator.update(updateTaskRunSql, taskRunIds.toArray());
            dbOperator.update(updateTaskAttemptSql, taskAttemptIds.toArray());
            return null;
        });
    }

    public void updateAttemptStatusByTaskRunIds(List<Long> taskRunIds, TaskRunStatus taskRunStatus) {
        updateAttemptStatusByTaskRunIds(taskRunIds, taskRunStatus, null);
    }

    public void updateAttemptStatusByTaskRunIds(List<Long> taskRunIds, TaskRunStatus taskRunStatus, @Nullable OffsetDateTime termAt) {
        updateAttemptStatusByTaskRunIds(taskRunIds, taskRunStatus, termAt, new ArrayList<>());
    }

    public void updateAttemptStatusByTaskRunIds(List<Long> taskRunIds, TaskRunStatus taskRunStatus,
                                                @Nullable OffsetDateTime termAt, List<TaskRunStatus> filterStatusList) {
        Preconditions.checkNotNull(taskRunIds, "taskRunIds should not be null");
        Preconditions.checkNotNull(filterStatusList, "filterStatusList should not be null");

        if (taskRunIds.size() == 0) {
            return;
        }

        List<Long> taskAttemptIds = fetchAllLatestTaskAttemptIds(taskRunIds);

        SQLBuilder updateTaskRun = DefaultSQLBuilder.newBuilder()
                .update(TASK_RUN_TABLE_NAME)
                .set("status=?");
        if (termAt != null) {
            updateTaskRun.set("term_at=?");
        }
        String taskRunWhereCase = "id in (" + repeatJoin("?", ",", taskRunIds.size()) + ")";
        if (filterStatusList.size() != 0) {
            taskRunWhereCase += " and status in (" + repeatJoin("?", ",", filterStatusList.size()) + ")";
        }
        updateTaskRun.where(taskRunWhereCase);

        SQLBuilder updateTaskAttempt = DefaultSQLBuilder.newBuilder()
                .update(TASK_ATTEMPT_TABLE_NAME)
                .set("status=?");
        if (termAt != null) {
            updateTaskAttempt.set("term_at=?");
        }
        String taskAttemptWhereCase = "id in (" + repeatJoin("?", ",", taskAttemptIds.size()) + ")";
        if (filterStatusList.size() != 0) {
            taskAttemptWhereCase += " and status in (" + repeatJoin("?", ",", filterStatusList.size()) + ")";
        }

        updateTaskAttempt.where(taskAttemptWhereCase);

        dbOperator.transaction(() -> {
            List<Object> taskRunParams = new ArrayList<>();
            taskRunParams.add(taskRunStatus.name());
            if (termAt != null) {
                taskRunParams.add(termAt);
            }
            taskRunParams.addAll(taskRunIds);
            if (filterStatusList.size() > 0) {
                taskRunParams.addAll(filterStatusList.stream().map(Enum::name).collect(Collectors.toList()));
            }
            dbOperator.update(updateTaskRun.getSQL(), taskRunParams.toArray());
            List<Object> taskAttemptParams = new ArrayList<>();
            taskAttemptParams.add(taskRunStatus.name());
            if (termAt != null) {
                taskAttemptParams.add(termAt);
            }
            taskAttemptParams.addAll(taskAttemptIds);
            if (filterStatusList.size() > 0) {
                taskAttemptParams.addAll(filterStatusList.stream().map(Enum::name).collect(Collectors.toList()));
            }
            dbOperator.update(updateTaskAttempt.getSQL(), taskAttemptParams.toArray());

            return null;
        });
    }

    public void updateTaskRunStatusByTaskRunId(List<Long> taskRunIds, TaskRunStatus taskRunStatus) {
        updateTaskRunStatusByTaskRunId(taskRunIds, taskRunStatus, new ArrayList<>());
    }

    /**
     * update task run status whose original status is allowed to change status
     *
     * @param taskRunIds          task runs to be updated
     * @param taskRunStatus       result status
     * @param allowToChangeStatus task run in these status is allowed to update status
     * @return
     */
    public void updateTaskRunStatusByTaskRunId(List<Long> taskRunIds, TaskRunStatus taskRunStatus,
                                               @Nullable List<TaskRunStatus> allowToChangeStatus) {
        updateAttemptStatusByTaskRunIds(taskRunIds, taskRunStatus, null, allowToChangeStatus);

    }

    public void updateTaskRunStat(Long taskRunId, Long averageRunningTime, Long averageQueuingTime) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(taskRunStatCols.toArray(new String[0]))
                .into(TASK_RUN_STAT_TABLE_NAME)
                .asPrepared()
                .getSQL();
        dbOperator.create(sql, taskRunId, averageRunningTime, averageQueuingTime);
    }

    public void updateTaskRunWithFailedUpstream(Long taskRunId, List<Long> downstreamTaskRunIds, TaskRunStatus taskRunStatus) {
        for (Long downstreamTaskRunId : downstreamTaskRunIds) {
            List<TaskRun> failedTaskRuns = fetchFailedUpstreamTaskRuns(downstreamTaskRunId);
            List<Long> failedTaskRunIds = failedTaskRuns.stream()
                    .map(TaskRun::getId)
                    .collect(Collectors.toList());
            SQLBuilder sql = DefaultSQLBuilder.newBuilder()
                    .update(TASK_RUN_TABLE_NAME)
                    .set("failed_upstream_task_run_ids=?")
                    .where("id = " + downstreamTaskRunId);
            List<Object> taskRunParams = new ArrayList<>();
            if (taskRunStatus.isCreated()) {
                failedTaskRunIds.remove(taskRunId);
            }
            if (taskRunStatus.isUpstreamFailed()) {
                failedTaskRunIds.add(taskRunId);
            }
            taskRunParams.add(JSONUtils.toJsonString(failedTaskRunIds));
            dbOperator.update(sql.getSQL(), taskRunParams.toArray());
        }
    }

    public void removeFailedUpstreamTaskRunIds(List<Long> taskRunIds, List<Long> failedUpstreamTaskRunIdsToBeRemoved) {
        if (failedUpstreamTaskRunIdsToBeRemoved.isEmpty()) {
            return;
        }

        String filterFailedUpstreamTaskRunId = failedUpstreamTaskRunIdsToBeRemoved.stream().map(x -> "'" + x + "'").collect(Collectors.joining(","));
        String filterTaskRunId = taskRunIds.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = DefaultSQLBuilder.newBuilder()
                .update(TASK_RUN_TABLE_NAME)
                .set("failed_upstream_task_run_ids = failed_upstream_task_run_ids::jsonb - array[" + filterFailedUpstreamTaskRunId + "]")
                .where("id in (" + filterTaskRunId + ")")
                .getSQL();
        List<Object> params = new ArrayList<>();
        params.addAll(taskRunIds);
        dbOperator.update(sql, params.toArray());
    }


    public List<TaskRun> fetchFailedUpstreamTaskRuns(Long taskRunId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("failed_upstream_task_run_ids")
                .from(TASK_RUN_TABLE_NAME)
                .where("id = ?")
                .getSQL();
        List<Long> failedUpstreamTaskRunIds = dbOperator.fetchOne(sql,
                rs -> JSONUtils.jsonToObjectOrDefault(rs.getString(1), new TypeReference<List<Long>>() {
                }, new ArrayList<>()),
                taskRunId);
        if (failedUpstreamTaskRunIds == null || failedUpstreamTaskRunIds.isEmpty()) return new ArrayList<>();
        List<Optional<TaskRun>> failedUpstreamTaskRuns = fetchTaskRunsByIds(failedUpstreamTaskRunIds);
        return failedUpstreamTaskRuns.stream()
                .map(r -> r.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Recursively fetch all downstream taskRunIds with the given taskRunId
     * only support postgres
     *
     * @param taskRunId
     * @return
     */
    public List<Long> fetchDownStreamTaskRunIdsRecursive(Long taskRunId) {
        String recursiveResult = "fetch_downstream";
        String unRecursiveSql = DefaultSQLBuilder.newBuilder()
                .select("downstream_task_run_id")
                .from(RELATION_TABLE_NAME)
                .where("upstream_task_run_id=?")
                .getSQL();
        String dependencySQL = DefaultSQLBuilder.newBuilder()
                .select(RELATION_MODEL_NAME + ".downstream_task_run_id")
                .from(RELATION_TABLE_NAME, RELATION_MODEL_NAME)
                .join("inner", recursiveResult, "fd")
                .on("fd.downstream_task_run_id=" + RELATION_MODEL_NAME + ".upstream_task_run_id")
                .getSQL();
        String finalSql = DefaultSQLBuilder.newBuilder()
                .select("downstream_task_run_id")
                .from(recursiveResult)
                .getSQL();
        StringBuilder recursiveSqlBuilder = new StringBuilder();
        String recursiveSql = recursiveSqlBuilder.append("WITH RECURSIVE fetch_downstream AS (\n")
                .append(unRecursiveSql + "\n")
                .append("UNION\n")
                .append(dependencySQL + "\n")
                .append(") " + finalSql)
                .toString();
        return dbOperator.fetchAll(recursiveSql, rs -> rs.getLong(1), taskRunId);
    }

    /**
     * Fetch downstream taskRunIds with the given taskRunId
     *
     * @param taskRunIds
     * @return
     */
    public List<Long> fetchDownStreamTaskRunIds(List<Long> taskRunIds) {
        String filterTaskRunId = taskRunIds.stream().map(x -> "?").collect(Collectors.joining(","));
        String dependencySQL = DefaultSQLBuilder.newBuilder()
                .select("downstream_task_run_id")
                .from(RELATION_TABLE_NAME)
                .where("upstream_task_run_id in (" + filterTaskRunId + ")")
                .getSQL();
        return dbOperator.fetchAll(dependencySQL, rs -> rs.getLong(1), taskRunIds.toArray());
    }

    /**
     * Fetch upstream taskRunIds with the given taskRunId
     *
     * @param taskRunIds
     * @return
     */
    public List<Long> fetchUpStreamTaskRunIds(List<Long> taskRunIds) {
        String filterTaskRunId = taskRunIds.stream().map(x -> "?").collect(Collectors.joining(","));
        String dependencySQL = DefaultSQLBuilder.newBuilder()
                .select("upstream_task_run_id")
                .from(RELATION_TABLE_NAME)
                .where("downstream_task_run_id in (" + filterTaskRunId + ")")
                .getSQL();
        return dbOperator.fetchAll(dependencySQL, rs -> rs.getLong(1), taskRunIds.toArray());
    }

    /**
     * Recursively fetch all upstream taskRunIds with the given taskRunId
     * only support postgres
     *
     * @param taskRunId
     * @return
     */
    public List<Long> fetchUpStreamTaskRunIdsRecursive(Long taskRunId) {
        String recursiveResult = "fetch_upstream";
        String unRecursiveSql = DefaultSQLBuilder.newBuilder()
                .select("upstream_task_run_id")
                .from(RELATION_TABLE_NAME)
                .where("downstream_task_run_id=?")
                .getSQL();
        String dependencySQL = DefaultSQLBuilder.newBuilder()
                .select(RELATION_MODEL_NAME + ".upstream_task_run_id")
                .from(RELATION_TABLE_NAME, RELATION_MODEL_NAME)
                .join("inner", recursiveResult, "fu")
                .on("fu.upstream_task_run_id=" + RELATION_MODEL_NAME + ".downstream_task_run_id")
                .getSQL();
        String finalSql = DefaultSQLBuilder.newBuilder()
                .select("upstream_task_run_id")
                .from(recursiveResult)
                .getSQL();
        StringBuilder recursiveSqlBuilder = new StringBuilder();
        String recursiveSql = recursiveSqlBuilder.append("WITH RECURSIVE fetch_upstream AS (\n")
                .append(unRecursiveSql + "\n")
                .append("UNION\n")
                .append(dependencySQL + "\n")
                .append(") " + finalSql)
                .toString();
        return dbOperator.fetchAll(recursiveSql, rs -> rs.getLong(1), taskRunId);
    }


    /**
     * Delete a TaskRun instance by ID
     *
     * @param taskRunId ID of target TaskRun instance
     * @return
     */
    public boolean deleteTaskRun(Long taskRunId) {
        return dbOperator.transaction(() -> {
            deleteTaskRunConditions(taskRunId);
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

    public List<TaskRun> fetchTaskRunsByFilterWithoutPagination(TaskRunSearchFilter filter) {
        Preconditions.checkNotNull(filter, "Invalid argument `filter`: null");

        String sortKey = Objects.nonNull(filter.getSortKey()) ? sortKeyToFieldMapper.get(filter.getSortKey()) : "start_at";
        String sortOrder = Objects.nonNull(filter.getSortOrder()) ? filter.getSortOrder() : "ASC";

        Pair<String, List<Object>> whereClauseAndParams = generateWhereClauseAndParamsByFilter(filter);
        String whereClause = whereClauseAndParams.getLeft();
        List<Object> params = whereClauseAndParams.getRight();

        String sql = getTaskRunSQLBuilderWithDefaultConfig()
                .where(whereClause)
                .orderBy(sortKey + " " + sortOrder)
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
        if (taskRunIds == null || taskRunIds.isEmpty()) {
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
                taskAttempt.getPriority(),
                taskAttempt.getRetryTimes(),
                taskAttempt.getExecutorLabel(),
                taskAttempt.getRuntimeLabel()
        );
        return taskAttempt;
    }

    public TaskAttempt updateAttempt(TaskAttempt taskAttempt) {

        List<String> tableColumns = new ImmutableList.Builder<String>()
                .addAll(taskAttemptCols)
                .build();

        String sql = DefaultSQLBuilder.newBuilder()
                .update(TASK_ATTEMPT_TABLE_NAME)
                .set(tableColumns.toArray(new String[0]))
                .where("id = ?")
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
                taskAttempt.getPriority(),
                taskAttempt.getRetryTimes(),
                taskAttempt.getExecutorLabel(),
                taskAttempt.getRuntimeLabel(),
                taskAttempt.getId()
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
        return updateTaskAttemptStatus(taskAttemptId, status, null, null, null, null);
    }

    public Optional<TaskRunStatus> updateTaskAttemptStatus(Long taskAttemptId, TaskRunStatus status,
                                                           @Nullable OffsetDateTime startAt, @Nullable OffsetDateTime endAt) {
        return updateTaskAttemptStatus(taskAttemptId, status, null, startAt, endAt, null);
    }

    public Optional<TaskRunStatus> updateTaskAttemptStatus(Long taskAttemptId, TaskRunStatus status, @Nullable OffsetDateTime queuedAt,
                                                           @Nullable OffsetDateTime startAt, @Nullable OffsetDateTime endAt, @Nullable OffsetDateTime termAt) {
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

        if (queuedAt != null) {
            sbTa.set("queued_at");
            pmTa.add(queuedAt);
            sbTr.set("queued_at");
            pmTr.add(queuedAt);
        }

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

        if (termAt != null) {
            sbTa.set("term_at");
            pmTa.add(termAt);
            sbTr.set("term_at");
            pmTr.add(termAt);
        }

        pmTa.add(taskAttemptId);
        pmTr.add(taskRunId);

        return dbOperator.transaction(() -> {
            Optional<TaskRunStatus> prev = fetchTaskAttemptStatus(taskAttemptId);
            if (prev.isPresent()) {
                dbOperator.update(sbTa.asPrepared().getSQL(), pmTa.toArray());
                dbOperator.update(sbTr.asPrepared().getSQL(), pmTr.toArray());
                DependencyStatus dependencyStatus = DependencyStatus.CREATED;
                if (status.isFinished()) {
                    dependencyStatus = status.isSuccess() ? DependencyStatus.SUCCESS : DependencyStatus.FAILED;
                }
                updateTaskRunDependency(taskRunId, dependencyStatus);
            }
            return prev;
        });
    }

    public List<TaskAttempt> fetchAllSatisfyTaskAttempt() {
        OffsetDateTime dateLimit = DateTimeUtils.now().plusDays(-RECOVER_LIMIT_DAYS);
        return dbOperator.transaction(() -> {
            List<Long> taskRunIdList = fetchAllSatisfyTaskRunId();
            List<Long> taskAttemptIdList = fetchAllLatestTaskAttemptIds(taskRunIdList, dateLimit);
            return fetchTaskAttemptByIds(taskAttemptIdList);
        });
    }

    public List<Long> fetchAllLatestTaskAttemptIds(List<Long> taskRunIdList) {
        return fetchAllLatestTaskAttemptIds(taskRunIdList, null);
    }

    public List<Long> fetchAllLatestTaskAttemptIds(List<Long> taskRunIdList, OffsetDateTime dateLimit) {
        if (taskRunIdList.size() == 0) {
            return Lists.newArrayList();
        }
        String taskRunIdFilter = "(" + taskRunIdList.stream().map(id -> "?")
                .collect(Collectors.joining(", ")) + ")";
        String whereCase = "task_run_id in " + taskRunIdFilter;
        if (dateLimit != null) {
            whereCase += " and created_at > ?";
        }
        String fetchIdSql = DefaultSQLBuilder.newBuilder()
                .select("max(id) as task_attempt_id")
                .from(TASK_ATTEMPT_TABLE_NAME)
                .where(whereCase)
                .groupBy("task_run_id")
                .asPrepared()
                .getSQL();
        List<Object> params = new ArrayList<>();
        params.addAll(taskRunIdList);
        if (dateLimit != null) {
            params.add(dateLimit);
        }
        return dbOperator.fetchAll(fetchIdSql, rs -> rs.getLong("task_attempt_id"), params.toArray());
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

    public boolean changePriority(Long taskRunId, Integer priority) {
        String taskRunSql = DefaultSQLBuilder.newBuilder()
                .update(TASK_RUN_TABLE_NAME)
                .set("priority")
                .where("id = ?")
                .asPrepared()
                .getSQL();

        String taskAttemptSql = DefaultSQLBuilder.newBuilder()
                .update(TASK_ATTEMPT_TABLE_NAME)
                .set("priority")
                .where("task_run_id = ?")
                .asPrepared()
                .getSQL();
        return dbOperator.transaction(() -> {
            dbOperator.update(taskAttemptSql, priority, taskRunId);
            return dbOperator.update(taskRunSql, priority, taskRunId) > 0;
        });
    }

    private boolean deleteTaskAttempts(Long taskRunId) {
        String dependencySQL = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(TASK_ATTEMPT_TABLE_NAME)
                .where("task_run_id = ?")
                .getSQL();
        return dbOperator.update(dependencySQL, taskRunId) >= 0;
    }

    private DependencyStatus fromUpstreamStatus(TaskRunStatus taskRunStatus) {
        if (taskRunStatus.isSuccess()) {
            return DependencyStatus.SUCCESS;
        }
        if (taskRunStatus.isFailure()) {
            return DependencyStatus.FAILED;
        }
        return DependencyStatus.CREATED;
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

    public List<TaskRunCondition> fetchTaskRunConditionsById(Long taskRunId) {
        String conditionSQL = DefaultSQLBuilder.newBuilder()
                .select(taskRunConditionCols.toArray(new String[0]))
                .from(CONDITION_TABLE_NAME, CONDITION_MODEL_NAME)
                .where("task_run_id = ?")
                .autoAliasColumns()
                .asPrepared()
                .getSQL();
        return dbOperator.fetchAll(conditionSQL, new TaskRunConditionMapper(), taskRunId);
    }

    public void createTaskRunConditions(Long taskRunId, List<TaskRunCondition> taskRunConditions) {
        if (taskRunConditions.isEmpty()) return;
        OffsetDateTime now = DateTimeUtils.now();
        List<String> tableColumns = new ImmutableList.Builder<String>()
                .addAll(taskRunConditionCols)
                .build();
        String conditionSQL = DefaultSQLBuilder.newBuilder()
                .insert(tableColumns.toArray(new String[0]))
                .into(CONDITION_TABLE_NAME)
                .asPrepared()
                .getSQL();
        Object[][] params = new Object[taskRunConditions.size()][6];
        for (int i = 0; i < taskRunConditions.size(); i++) {
            TaskRunCondition taskRunCondition = taskRunConditions.get(i);
            params[i] = new Object[]{taskRunId, JSONUtils.toJsonString(taskRunCondition.getCondition()), taskRunCondition.getResult(), taskRunCondition.getType().name(), now, now};
        }
        dbOperator.batch(conditionSQL, params);
    }

    public boolean deleteTaskRunConditions(Long taskRunId) {
        String conditionSQL = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(CONDITION_TABLE_NAME)
                .where("task_run_id = ?")
                .getSQL();
        return dbOperator.update(conditionSQL, taskRunId) >= 0;
    }

    /**
     * when task run is updated to static status
     * update condition table whose condition is this taskrun
     * result is calculated by status
     *
     * @param taskRunIds
     * @param status     status should be static (exclude xx-ing)
     * @return number of effected rows
     */
    public int updateConditionsWithTaskRuns(List<Long> taskRunIds, TaskRunStatus status) {
        SQLBuilder sbTc = DefaultSQLBuilder.newBuilder()
                .update(CONDITION_TABLE_NAME)
                .set("result");
        List<Object> pmTc = Lists.newArrayList();
        if (status.isCreated()) {
            pmTc.add(false);
        } else {
            pmTc.add(true);
        }
        taskRunIds.forEach(x -> pmTc.add(JSONUtils.toJsonString(new Condition(Collections.singletonMap("taskRunId", x.toString())))));
        if (status.isSuccess() || status.isCreated()) {
            sbTc.where("condition in (" + repeatJoin("?", ",", taskRunIds.size()) + ") and (type = ? or type = ?)");
            pmTc.add(ConditionType.TASKRUN_DEPENDENCY_SUCCESS.name());
        } else if (status.isFailure() || status.isUpstreamFailed()) {
            sbTc.where("condition in (" + repeatJoin("?", ",", taskRunIds.size()) + ") and type = ?");
        }
        pmTc.add(ConditionType.TASKRUN_PREDECESSOR_FINISH.name());
        String conditionSQL = sbTc.asPrepared().getSQL();
        return dbOperator.update(conditionSQL, pmTc.toArray());
    }

    /**
     * Fetch task run ids not satisfy condition type
     * which type is conditionType and result is false
     *
     * @param taskRunIds
     * @param conditionType
     * @return list of task run ids
     */
    public List<Long> fetchRestrictedTaskRunIdsWithConditionType(List<Long> taskRunIds, ConditionType conditionType) {
        if (taskRunIds.isEmpty()) return Collections.emptyList();
        String sql = DefaultSQLBuilder.newBuilder()
                .select("task_run_id")
                .from(CONDITION_TABLE_NAME)
                .where("task_run_id IN (" + repeatJoin("?", ",", taskRunIds.size()) + ")")
                .groupBy("task_run_id")
                .having("sum(case when type = ? and result = ? then 1 else 0 end) > 0")
                .asPrepared()
                .getSQL();
        List<Object> params = new ArrayList<>(taskRunIds.size() + 2);
        params.addAll(taskRunIds);
        params.add(conditionType.name());
        params.add(false);

        return dbOperator.fetchAll(sql, rs -> rs.getLong(1), params.toArray());
    }

    public List<Long> fetchTaskRunIdsWithBlockType(List<Long> taskRunIds) {
        if (taskRunIds.isEmpty()) return Collections.emptyList();
        String sql = DefaultSQLBuilder.newBuilder()
                .select("task_run_id")
                .from(CONDITION_TABLE_NAME)
                .where("task_run_id IN (" + repeatJoin("?", ",", taskRunIds.size()) + ")")
                .groupBy("task_run_id")
                .having("sum(case when type = ? and result = ? then 1 else 0 end) = 0 and" +
                        " sum(case when type = ? and result = ? then 1 else 0 end) > 0")
                .asPrepared()
                .getSQL();
        List<Object> params = new ArrayList<>(taskRunIds.size() + 4);
        params.addAll(taskRunIds);
        params.add(ConditionType.TASKRUN_DEPENDENCY_SUCCESS.name());
        params.add(false);
        params.add(ConditionType.TASKRUN_PREDECESSOR_FINISH.name());
        params.add(false);
        return dbOperator.fetchAll(sql, rs -> rs.getLong(1), params.toArray());
    }

    public List<Long> fetchRestrictedTaskRunIdsFromConditions(List<Condition> conditions) {
        if (conditions.isEmpty()) {
            return Collections.emptyList();
        }
        String conditionSQL = DefaultSQLBuilder.newBuilder()
                .select("task_run_id")
                .from(CONDITION_TABLE_NAME)
                .where("condition in (" + repeatJoin("?", ",", conditions.size()) + ")")
                .asPrepared().getSQL();
        return dbOperator.fetchAll(conditionSQL, rs -> rs.getLong(1),
                        conditions.stream().map(JSONUtils::toJsonString).toArray())
                .stream().distinct().collect(Collectors.toList());
    }


    public TaskRun fetchLatestTaskRun(Long taskId) {
        List<TaskRun> latestRunInList = fetchLatestTaskRuns(taskId, null, 1);
        if (latestRunInList.isEmpty()) {
            return null;
        }
        // else
        return latestRunInList.get(0);
    }

    public List<TaskRun> fetchLatestTaskRuns(Long taskId, List<TaskRunStatus> filterStatus, int limit) {
        checkNotNull(taskId, "taskId should not be null.");

        List<Object> params = new ArrayList<>();
        params.add(taskId);
        SQLBuilder sqlBuilder = getTaskRunSQLBuilderWithDefaultConfig();
        StringBuilder whereCase = new StringBuilder().append("task_id = ?");
        if (filterStatus != null && filterStatus.size() > 0) {
            String filterString = filterStatus.stream().map(x -> "?").collect(Collectors.joining(","));
            whereCase.append(" and status in (").append(filterString).append(")");
            List<String> statusStringList = filterStatus.stream().map(Enum::name).collect(Collectors.toList());
            params.addAll(statusStringList);
        }
        sqlBuilder.where(whereCase.toString());
        params.add(limit);
        String sql = sqlBuilder
                .orderBy(TASK_RUN_MODEL_NAME + ".id DESC")
                .limit()
                .asPrepared()
                .getSQL();
        return dbOperator.fetchAll(sql, taskRunMapperInstance, params.toArray());
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

    /**
     * fetch direct upstreamTaskRun by taskRunId
     *
     * @param srcTaskRunId
     * @return
     */
    public List<TaskRunProps> fetchUpstreamTaskRunsById(Long srcTaskRunId) {
        String subSql = DefaultSQLBuilder.newBuilder()
                .select("upstream_task_run_id")
                .from(RELATION_TABLE_NAME)
                .where("downstream_task_run_id = ?")
                .asPrepared()
                .getSQL();
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_RUN_MODEL_NAME, taskRunPropsCols);
        String sql = DefaultSQLBuilder.newBuilder()
                .select(taskRunPropsCols.toArray(new String[0]))
                .from(TASK_RUN_TABLE_NAME)
                .where("id in ( " + subSql + " )")
                .asPrepared()
                .getSQL();

        return dbOperator.fetchAll(sql, TaskRunPropsMapper.INSTANCE, srcTaskRunId);
    }

    /**
     * fetch task runs witch has created but attempt is null
     *
     * @return
     */
    public List<TaskRun> fetchTaskRunListWithoutAttempt() {
        OffsetDateTime recoverLimit = DateTimeUtils.now().plusDays(-RECOVER_LIMIT_DAYS);
        List<Long> taskRunIds = fetchTaskRunIdsWithoutAttempt(recoverLimit);
        if (taskRunIds.size() == 0) {
            return new ArrayList<>();
        }
        String filterTaskRunId = taskRunIds.stream().map(x -> "?").collect(Collectors.joining(","));
        String whereCase = TASK_RUN_MODEL_NAME + ".id in (" + filterTaskRunId + ")";
        String sql = getTaskRunSQLBuilderWithDefaultConfig()
                .where(whereCase)
                .getSQL();
        List<TaskRun> taskRunList = dbOperator.fetchAll(sql, taskRunMapperInstance, taskRunIds.toArray());
        Map<Long, List<Long>> taskRunRelations = fetchAllRelationsFromDownstreamTaskRunIds(taskRunList.stream().map(TaskRun::getId).collect(Collectors.toList()));
        return taskRunList.stream().map(taskRun -> taskRun.cloneBuilder()
                .withDependentTaskRunIds(taskRunRelations.get(taskRun.getId()))
                .build()).collect(Collectors.toList());
    }

    public void removeTaskRunDependency(Long taskRunId, List<Long> dependencyTaskRunIds) {
        if (dependencyTaskRunIds.size() == 0) {
            return;
        }
        String upstreamFilter = repeatJoin("?", ",", dependencyTaskRunIds.size());
        //remove dependency
        String removeDependencySql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(RELATION_TABLE_NAME)
                .where("downstream_task_run_id = ? and upstream_task_run_id in ( " + upstreamFilter + " )")
                .asPrepared()
                .getSQL();
        List<Object> dependencyParams = new ArrayList<>();
        dependencyParams.add(taskRunId);
        dependencyParams.addAll(dependencyTaskRunIds);
        //remove conditions
        String removeConditionSql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(CONDITION_TABLE_NAME)
                .where("task_run_id = ? and type = ? and condition in ( " + upstreamFilter + " )")
                .asPrepared()
                .getSQL();
        List<Object> conditionParams = new ArrayList<>();
        conditionParams.add(taskRunId);
        conditionParams.add(ConditionType.TASKRUN_DEPENDENCY_SUCCESS.name());
        dependencyTaskRunIds.forEach(x -> conditionParams.add(JSONUtils.toJsonString(new Condition(Collections.singletonMap("taskRunId", x.toString())))));

        dbOperator.transaction(() -> {
            dbOperator.update(removeDependencySql, dependencyParams.toArray());
            dbOperator.update(removeConditionSql, conditionParams.toArray());
            List<Long> conditions = dbOperator.fetchAll("select task_run_id from kun_wf_task_run_conditions", rs -> rs.getLong(1));
            return conditions;
        });


    }

    private List<Long> fetchTaskRunIdsWithoutAttempt(OffsetDateTime recoverLimit) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(TASK_RUN_MODEL_NAME + ".id")
                .from(TASK_RUN_TABLE_NAME, TASK_RUN_MODEL_NAME)
                .join("left", TASK_ATTEMPT_TABLE_NAME, TASK_ATTEMPT_MODEL_NAME)
                .on(TASK_RUN_MODEL_NAME + ".id = " + TASK_ATTEMPT_MODEL_NAME + ".task_run_id")
                .where(TASK_ATTEMPT_MODEL_NAME + ".task_run_id is null and " + TASK_RUN_MODEL_NAME + ".created_at > ?")
                .getSQL();
        return dbOperator.fetchAll(sql, rs -> rs.getLong(1), recoverLimit);
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

    public List<TaskAttempt> fetchTaskAttemptListForRecover(List<TaskRunStatus> taskRunStatusList) {
        if (taskRunStatusList.size() == 0) {
            return new ArrayList<>();
        }
        String filterTaskRunStatus = taskRunStatusList.stream().map(x -> "?").collect(Collectors.joining(","));
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_ATTEMPT_MODEL_NAME, taskAttemptCols);
        columnsMap.put(TASK_RUN_MODEL_NAME, taskRunCols);
        columnsMap.put(TaskDao.TASK_MODEL_NAME, TaskDao.getTaskCols());

        String whereCase = TASK_ATTEMPT_MODEL_NAME + ".status in " + "(" + filterTaskRunStatus +
                ") and " + TASK_ATTEMPT_MODEL_NAME + ".created_at > ?";
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
        OffsetDateTime recoverLimit = DateTimeUtils.now().plusDays(-RECOVER_LIMIT_DAYS);
        List<Object> params = new ArrayList<>();
        params.addAll(taskRunStatusList.stream().map(Enum::toString).collect(Collectors.toList()));
        params.add(recoverLimit);
        return dbOperator.fetchAll(sql, new TaskAttemptMapper(TASK_ATTEMPT_MODEL_NAME, taskRunMapperInstance),
                params.toArray());
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

    public List<Long> taskRunShouldBeCreated(List<Long> taskRunIds) {
        if (taskRunIds.size() == 0) {
            return new ArrayList<>();
        }
        String sql = DefaultSQLBuilder.newBuilder()
                .select("id")
                .from(TASK_RUN_TABLE_NAME)
                .where("id in (" + repeatJoin("?", ",", taskRunIds.size()) +
                        ") and jsonb_array_length(failed_upstream_task_run_ids) = 0 and status = ?")
                .getSQL();
        List<Object> params = new ArrayList<>();
        params.addAll(taskRunIds);
        params.add(TaskRunStatus.UPSTREAM_FAILED.name());
        return dbOperator.fetchAll(sql, rs -> rs.getLong("id"), params.toArray());
    }

    public void updateTaskRunDependencyByTaskRunIds(List<Long> taskRunIds, DependencyStatus status) {
        String filterTaskRunId = taskRunIds.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = DefaultSQLBuilder.newBuilder()
                .update(RELATION_TABLE_NAME)
                .set("dependency_status")
                .where("downstream_task_run_id in " + "(" + filterTaskRunId + ")")
                .asPrepared()
                .getSQL();
        List<Object> params = new ArrayList<>();
        params.add(status.name());
        params.addAll(taskRunIds);
        dbOperator.update(sql, params.toArray());
    }

    public List<Long> fetchAllSatisfyTaskRunId() {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(TASK_RUN_MODEL_NAME + ".id")
                .from(TASK_RUN_TABLE_NAME, TASK_RUN_MODEL_NAME)
                .join("LEFT", CONDITION_TABLE_NAME, CONDITION_MODEL_NAME)
                .on(TASK_RUN_MODEL_NAME + ".id = " + CONDITION_MODEL_NAME + ".task_run_id")
                .where(TASK_RUN_MODEL_NAME + ".status = ? ")
                .groupBy(TASK_RUN_MODEL_NAME + ".id")
                .having("sum(case when result = ? then 1 else 0 end) = 0")
                .asPrepared()
                .getSQL();
        List<Long> satisfyTaskRunId = dbOperator.fetchAll(sql, rs -> rs.getLong("id"),
                TaskRunStatus.CREATED.name(), false);
        return satisfyTaskRunId;
    }

    public List<TaskRunStat> fetchTaskRunStat(List<Long> taskRunIds) {
        String filterTaskRunIds = taskRunIds.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = DefaultSQLBuilder.newBuilder()
                .select(taskRunStatCols.toArray(new String[0]))
                .from(TASK_RUN_STAT_TABLE_NAME)
                .where("task_run_id IN (" + filterTaskRunIds + ")")
                .asPrepared()
                .getSQL();
        return dbOperator.fetchAll(sql, rs -> TaskRunStat.newBuilder()
                .withId(rs.getLong("task_run_id"))
                .withAverageRunningTime(rs.getLong("average_running_time"))
                .withAverageQueuingTime(rs.getLong("average_queuing_time"))
                .build(), taskRunIds.toArray());
    }

    public List<Long> lossUpdateConditionTaskRuns() {
        OffsetDateTime recoverLimit = DateTimeUtils.now().plusDays(-RECOVER_LIMIT_DAYS);
        String sql = DefaultSQLBuilder.newBuilder()
                .select(TASK_RUN_MODEL_NAME + ".id")
                .from(TASK_RUN_TABLE_NAME, TASK_RUN_MODEL_NAME)
                .join("inner", CONDITION_TABLE_NAME, CONDITION_MODEL_NAME)
                .on(TASK_RUN_MODEL_NAME + ".id::varchar(255) = " + CONDITION_MODEL_NAME + ".condition -> 'content' ->> 'taskRunId'")
                .where(TASK_RUN_MODEL_NAME + ".status = ? and " + CONDITION_MODEL_NAME + ".result = ? and "
                        + TASK_RUN_MODEL_NAME + ".created_at > ?")
                .asPrepared()
                .getSQL();
        return dbOperator.fetchAll(sql, rs -> rs.getLong("id"), TaskRunStatus.SUCCESS.name(), false, recoverLimit);
    }

    public void fixConditionWithTaskRunId(Long lossUpdateTaskRunId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(CONDITION_TABLE_NAME)
                .set("result")
                .where("condition -> 'content' ->> 'taskRunId' = ?")
                .asPrepared()
                .getSQL();
        dbOperator.update(sql, true, String.valueOf(lossUpdateTaskRunId));
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

    public static class TaskRunPropsMapper implements ResultSetMapper<TaskRunProps> {

        public static final TaskRunDao.TaskRunPropsMapper INSTANCE = new TaskRunDao.TaskRunPropsMapper();

        @Override
        public TaskRunProps map(ResultSet rs) throws SQLException {
            return TaskRunProps.newBuilder()
                    .withId(rs.getLong("id"))
                    .withConfig(JSONUtils.jsonToObject(rs.getString("config"), Config.class))
                    .withScheduledTick(new Tick(rs.getString("scheduled_tick")))
                    .withStatus(TaskRunStatus.valueOf(rs.getString("status")))
                    .withQueuedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("queued_at")))
                    .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("start_at")))
                    .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("end_at")))
                    .withCreatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("created_at")))
                    .withUpdatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("updated_at")))
                    .withScheduleType(ScheduleType.valueOf(rs.getString("schedule_type")))
                    .withQueueName(rs.getString("queue_name"))
                    .withPriority(rs.getInt("priority"))
                    .withExecuteTarget(JSONUtils.jsonToObjectOrDefault(rs.getString("target"),
                            ExecuteTarget.class, ExecuteTarget.newBuilder().build()))
                    .build();
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
                    .withFailedUpstreamTaskRunIds(JSONUtils.jsonToObjectOrDefault(rs.getString(TASK_RUN_MODEL_NAME + "_failed_upstream_task_run_ids"),
                            new TypeReference<List<Long>>() {
                            }, new ArrayList<>()))
                    .withDependentTaskRunIds(Collections.emptyList())
                    .withQueuedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_queued_at")))
                    .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_start_at")))
                    .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_end_at")))
                    .withTermAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_term_at")))
                    .withCreatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_created_at")))
                    .withUpdatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_updated_at")))
                    .withConfig(JSONUtils.jsonToObject(rs.getString(TASK_RUN_MODEL_NAME + "_config"), Config.class))
                    .withQueueName(rs.getString(TASK_RUN_MODEL_NAME + "_queue_name"))
                    .withPriority(rs.getInt(TASK_RUN_MODEL_NAME + "_priority"))
                    .withExecuteTarget(JSONUtils.jsonToObjectOrDefault(rs.getString(TASK_RUN_MODEL_NAME + "_target"),
                            ExecuteTarget.class, ExecuteTarget.newBuilder().build()))
                    .withExecutorLabel(rs.getString(TASK_RUN_MODEL_NAME + "_executor_label"))
                    .withScheduleTime(new Tick(rs.getString(TASK_RUN_MODEL_NAME + "_schedule_time")))
                    .build();
        }
    }

    private static class TaskRunConditionMapper implements ResultSetMapper<TaskRunCondition> {

        @Override
        public TaskRunCondition map(ResultSet rs) throws SQLException {
            return TaskRunCondition.newBuilder()
                    .withCondition(JSONUtils.jsonToObject(rs.getString("condition"), Condition.class))
                    .withResult(rs.getBoolean("result"))
                    .withType(ConditionType.valueOf(rs.getString("type")))
                    .withCreatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("created_at")))
                    .withUpdatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("updated_at")))
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
                    .withRetryTimes(rs.getInt(column("retry_times", tableAlias)))
                    .withExecutorLabel(rs.getString(column("executor_label", tableAlias)))
                    .withRuntimeLabel(rs.getString(column("runtime_label", tableAlias)))
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
