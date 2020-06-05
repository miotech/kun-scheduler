package com.miotech.kun.workflow.common.taskrun.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.exception.EntityNotFoundException;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.bo.TaskAttemptProps;
import com.miotech.kun.workflow.common.taskrun.filter.TaskRunSearchFilter;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.db.ResultSetMapper;
import com.miotech.kun.workflow.db.sql.DefaultSQLBuilder;
import com.miotech.kun.workflow.db.sql.SQLBuilder;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.miotech.kun.workflow.utils.StringUtils.toNullableString;

@Singleton
public class TaskRunDao {
    private static final Logger logger = LoggerFactory.getLogger(TaskRunDao.class);
    private static final String TASK_RUN_MODEL_NAME = "taskrun";
    private static final String TASK_RUN_TABLE_NAME = "kun_wf_task_run";
    private static final List<String> taskRunCols = ImmutableList.of("id", "task_id", "scheduled_tick", "status", "start_at", "end_at", "variables", "inlets", "outlets");

    private static final String TASK_ATTEMPT_MODEL_NAME = "taskattempt";
    private static final String TASK_ATTEMPT_TABLE_NAME = "kun_wf_task_attempt";
    private static final List<String> taskAttemptCols = ImmutableList.of("id", "task_run_id", "attempt", "status", "start_at", "end_at", "log_path");

    private static final String RELATION_TABLE_NAME = "kun_wf_task_run_relations";
    private static final List<String> taskRunRelationCols = ImmutableList.of("upstream_task_run_id", "downstream_task_run_id");

    @Inject
    private TaskDao taskDao;

    @Inject
    private DatabaseOperator dbOperator;

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
                .join("INNER", TaskDao.TASK_TABLE_NAME, TaskDao.TASK_MODEL_NAME)
                .on(TASK_RUN_MODEL_NAME + ".task_id = " + TaskDao.TASK_MODEL_NAME + ".id")
                .autoAliasColumns();
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

    /**
     * Fetch TaskRun instance with given id
     * @param id id of target task run instance
     * @return the query result of TaskRun instance, empty represents not found
     */
    public Optional<TaskRun> fetchTaskRunById(Long id) {
        TaskRun taskRun = dbOperator.fetchOne(getSelectSQL(TASK_RUN_MODEL_NAME + ".id = ?"), TaskRunMapper.INSTANCE, id);

        List<Long>  dependencies = fetchTaskRunDependencies(id);
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
     * @param taskRun instance of TaskRun model to be stored
     * @throws RuntimeException if primary key (id) is duplicated
     * @return TaskRun instance itself
     */
    public TaskRun createTaskRun(TaskRun taskRun) {
        dbOperator.transaction( () -> {
            List<String> tableColumns = new ImmutableList.Builder<String>()
                    .addAll(taskRunCols)
                    .build();

            String sql = DefaultSQLBuilder .newBuilder()
                    .insert(tableColumns.toArray(new String[0]))
                    .into(TASK_RUN_TABLE_NAME)
                    .asPrepared()
                    .getSQL();

            dbOperator.update(sql,
                    taskRun.getId(),
                    taskRun.getTask().getId(),
                    taskRun.getScheduledTick().toString(),
                    toNullableString(taskRun.getStatus()),
                    taskRun.getStartAt(),
                    taskRun.getEndAt(),
                    JSONUtils.toJsonString(taskRun.getVariables()),
                    JSONUtils.toJsonString(taskRun.getInlets()),
                    JSONUtils.toJsonString(taskRun.getOutlets())
            );

            createTaskRunDependencies(taskRun.getId(), taskRun.getDependentTaskRunIds());
            return taskRun;
        });

        return taskRun;
    }

    public List<TaskRun> createTaskRuns(List<TaskRun> taskRuns) {
        return taskRuns.stream().map(this::createTaskRun).collect(Collectors.toList());
    }

    /**
     * Update a TaskRun instance
     * @param taskRun TaskRun instance with properties updated
     * @return
     */
    public TaskRun updateTaskRun(TaskRun taskRun) {
        dbOperator.transaction( () -> {
            List<String> tableColumns = new ImmutableList.Builder<String>()
                    .addAll(taskRunCols)
                    .build();

            String sql = DefaultSQLBuilder .newBuilder()
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
                    taskRun.getStartAt(),
                    taskRun.getEndAt(),
                    JSONUtils.toJsonString(taskRun.getVariables()),
                    JSONUtils.toJsonString(taskRun.getInlets()),
                    JSONUtils.toJsonString(taskRun.getOutlets()),
                    taskRun.getId()
            );

            deleteTaskRunDependencies(taskRun.getId());
            createTaskRunDependencies(taskRun.getId(), taskRun.getDependentTaskRunIds());
            return taskRun;
        });

        return taskRun;
    }

    /**
     * Delete a TaskRun instance by ID
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
     * @param filter filter value object
     * @return filtered list of task runs
     */
    public List<TaskRun> fetchTaskRunsByFilter(TaskRunSearchFilter filter) {
        Preconditions.checkNotNull(filter, "Invalid argument `filter`: null");

        List<String> whereConditions = new ArrayList<>();
        List<Object> sqlArgs = new ArrayList<>();
        if (Objects.nonNull(filter.getTaskIds()) && (!filter.getTaskIds().isEmpty())) {
            String idsFieldsPlaceholder = "(" + filter.getTaskIds().stream().map(id -> "?")
                    .collect(Collectors.joining(", ")) + ")";
            sqlArgs.addAll(filter.getTaskIds());
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".task_id IN " + idsFieldsPlaceholder + " )");
        }
        // DON'T USE BETWEEN, see: https://wiki.postgresql.org/wiki/Don't_Do_This#Don.27t_use_BETWEEN_.28especially_with_timestamps.29
        if (Objects.nonNull(filter.getDateFrom())) {
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".start_at >= ? )");
            sqlArgs.add(filter.getDateFrom().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        if (Objects.nonNull(filter.getDateTo())) {
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".end_at <= ? )");
            sqlArgs.add(filter.getDateTo().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        if (Objects.nonNull(filter.getStatus())) {
            whereConditions.add("(" + TASK_RUN_MODEL_NAME + ".status = ? )");
            sqlArgs.add(filter.getStatus().toString());
        }

        int pageNum = Objects.nonNull(filter.getPageNum()) ? filter.getPageNum() : 1;
        int pageSize = Objects.nonNull(filter.getPageSize()) ? filter.getPageSize() : 100;

        String whereClause;
        if (whereConditions.isEmpty()) {
            whereClause = "1 = 1";
        } else {
            whereClause = String.join(" AND ", whereConditions.toArray(new String[0]));
        }
        String sql = getTaskRunSQLBuilderWithDefaultConfig()
                .where(whereClause)
                .limit(pageSize)
                .offset((pageNum - 1) * pageSize)
                .getSQL();

        return dbOperator.fetchAll(sql, TaskRunMapper.INSTANCE, sqlArgs.toArray());
    }

    /**
     * Returns a list of value object
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

        return dbOperator.fetchAll(sql, new TaskAttemptPropsMapper(true), taskRunId);
    }

    /**
     * Fetch TaskAttempt instance by given id,
     * a TaskAttempt instance is nested with the TaskRun and Task model it derived from.
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

        return Optional.ofNullable(dbOperator.fetchOne(sql, new TaskAttemptMapper(true), attemptId));
    }

    public TaskAttempt createAttempt(TaskAttempt taskAttempt) {
        List<String> tableColumns = new ImmutableList.Builder<String>()
                .addAll(taskAttemptCols)
                .build();

        String sql = DefaultSQLBuilder .newBuilder()
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
                taskAttempt.getLogPath()
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

    private void createTaskRunDependencies(Long taskRunId, List<Long> dependencies) {
        if (dependencies.isEmpty()) return;
        List<String> tableColumns = new ImmutableList.Builder<String>()
                .addAll(taskRunRelationCols)
                .build();

        String dependencySQL = DefaultSQLBuilder .newBuilder()
                .insert(tableColumns.toArray(new String[0]))
                .into(RELATION_TABLE_NAME)
                .asPrepared()
                .getSQL();

        Object[][] params = new Object[dependencies.size()][2];
        for (int i = 0; i < dependencies.size(); i++) {
            params[i] = new Object[]{dependencies.get(i), taskRunId};
        }

        dbOperator.batch(dependencySQL, params);
    }

    public TaskRun fetchLatestTaskRun(Long taskId) {
        checkNotNull(taskId, "taskId should not be null.");

        String sql = getTaskRunSQLBuilderWithDefaultConfig()
                .where("task_id = ?")
                .orderBy("start_at DESC")
                .limit(1)
                .getSQL();
        return dbOperator.fetchOne(sql, TaskRunMapper.INSTANCE, taskId);
    }

    public TaskAttemptProps fetchLatestTaskAttempt(Long taskRunId) {
        return fetchLatestTaskAttempt(Lists.newArrayList(taskRunId)).get(0);
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
                .orderBy( "u.attempt DESC")
                .limit(1)
                .getSQL();
        String sql = DefaultSQLBuilder.newBuilder()
                .select(taskAttemptCols.toArray(new String[0]))
                .from(TASK_ATTEMPT_TABLE_NAME, TASK_ATTEMPT_MODEL_NAME)
                .where(TASK_ATTEMPT_MODEL_NAME + ".task_run_id IN " + idsFieldsPlaceholder + " AND " + TASK_ATTEMPT_MODEL_NAME + ".id IN (" + subSelectSql + ")")
                .autoAliasColumns()
                .orderBy(orderClause)
                .getSQL();

        return dbOperator.fetchAll(sql, new TaskAttemptPropsMapper(false), taskRunIds.toArray());
    }

    /**
     * Fetch and return upstream task runs within maximum distance from given source
     * @param srcTaskRunId id of source task run
     * @param distance max distance from source task, required to be positive
     * @param includeSelf whether source task run should be included
     * @throws IllegalArgumentException when distance is illegal
     * @return list of upstream task runs
     */
    public List<TaskRun> fetchUpstreamTaskRunsById(Long srcTaskRunId, int distance, boolean includeSelf) {
        Preconditions.checkNotNull(srcTaskRunId, "Invalid argument `srcTaskRunId`: null");
        Preconditions.checkArgument(distance > 0, "Argument `distance` should be positive, but found: %d", distance);

        return fetchDependentTaskRunsById(srcTaskRunId, distance, DependencyDirection.UPSTREAM, includeSelf);
    }

    /**
     * Fetch and return downstream task runs within maximum distance from given source
     * @param srcTaskRunId id of source task run
     * @param distance max distance from source task, required to be positive
     * @param includeSelf whether source task run should be included
     * @throws IllegalArgumentException when distance is illegal
     * @return  list of downstream task runs
     */
    public List<TaskRun> fetchDownstreamTaskRunsById(Long srcTaskRunId, int distance, boolean includeSelf) {
        Preconditions.checkNotNull(srcTaskRunId, "Invalid argument `srcTaskRunId`: null");
        Preconditions.checkArgument(distance > 0, "Argument `distance` should be positive, but found: %d", distance);

        return fetchDependentTaskRunsById(srcTaskRunId, distance, DependencyDirection.DOWNSTREAM, includeSelf);
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
                    .withInlets(JSONUtils.jsonToObject(rs.getString(TASK_RUN_MODEL_NAME + "_inlets"), new TypeReference<List<DataStore>>() {}))
                    .withOutlets(JSONUtils.jsonToObject(rs.getString(TASK_RUN_MODEL_NAME + "_outlets"), new TypeReference<List<DataStore>>() {}))
                    .withDependentTaskRunIds(Collections.emptyList())
                    .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_start_at")))
                    .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_end_at")))
                    .withVariables(JSONUtils.jsonToObject(rs.getString(TASK_RUN_MODEL_NAME + "_variables"), new TypeReference<List<Variable>>() {}))
                    .build();
        }
    }

    private static class TaskAttemptMapper implements ResultSetMapper<TaskAttempt> {
        private boolean withColumnAliased;

        public TaskAttemptMapper(boolean withColumnAliased) {
            this.withColumnAliased = withColumnAliased;
        }

        @Override
        public TaskAttempt map(ResultSet rs) throws SQLException {
            if (withColumnAliased) {
                return TaskAttempt.newBuilder()
                        .withId(rs.getLong(TASK_ATTEMPT_MODEL_NAME + "_id"))
                        .withStatus(TaskRunStatus.resolve(rs.getString(TASK_ATTEMPT_MODEL_NAME + "_status")))
                        .withTaskRun(TaskRunMapper.INSTANCE.map(rs))
                        .withAttempt(rs.getInt(TASK_ATTEMPT_MODEL_NAME + "_attempt"))
                        .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_ATTEMPT_MODEL_NAME + "_start_at")))
                        .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_ATTEMPT_MODEL_NAME + "_end_at")))
                        .withLogPath(rs.getString(TASK_ATTEMPT_MODEL_NAME + "_log_path"))
                        .build();
            } else {
                return TaskAttempt.newBuilder()
                        .withId(rs.getLong("id"))
                        .withStatus(TaskRunStatus.resolve(rs.getString("status")))
                        .withAttempt(rs.getInt("attempt"))
                        .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("start_at")))
                        .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("end_at")))
                        .withLogPath(rs.getString("log_path"))
                        .build();
            }
        }
    }

    private static class TaskAttemptPropsMapper implements ResultSetMapper<TaskAttemptProps> {
        private boolean withColumnAliased;

        public TaskAttemptPropsMapper(boolean withColumnAliased) {
            this.withColumnAliased = withColumnAliased;
        }

        @Override
        public TaskAttemptProps map(ResultSet rs) throws SQLException {
            if (withColumnAliased) {
                return TaskAttemptProps.newBuilder()
                        .withId(rs.getLong(TASK_ATTEMPT_MODEL_NAME + "_id"))
                        .withTaskRunId(rs.getLong(TASK_ATTEMPT_MODEL_NAME + "_task_run_id"))
                        .withAttempt(rs.getInt(TASK_ATTEMPT_MODEL_NAME + "_attempt"))
                        .withStatus(TaskRunStatus.valueOf(rs.getString(TASK_ATTEMPT_MODEL_NAME + "_status")))
                        .withLogPath(rs.getString(TASK_ATTEMPT_MODEL_NAME + "_log_path"))
                        .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp( TASK_ATTEMPT_MODEL_NAME + "_start_at")))
                        .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_ATTEMPT_MODEL_NAME + "_end_at")))
                        .build();
            } else {
                return TaskAttemptProps.newBuilder()
                        .withId(rs.getLong("id"))
                        .withTaskRunId(rs.getLong("task_run_id"))
                        .withAttempt(rs.getInt("attempt"))
                        .withStatus(TaskRunStatus.valueOf(rs.getString("status")))
                        .withLogPath(rs.getString("log_path"))
                        .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp( "start_at")))
                        .withEndAt(DateTimeUtils.fromTimestamp( rs.getTimestamp("end_at")))
                        .build();
            }
        }
    }
}
