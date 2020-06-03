package com.miotech.kun.workflow.common.task.dao;

import com.cronutils.model.Cron;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.workflow.common.task.dependency.TaskDependencyFunctionProvider;
import com.miotech.kun.workflow.common.task.filter.TaskSearchFilter;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskDependency;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.db.ResultSetMapper;
import com.miotech.kun.workflow.db.sql.DefaultSQLBuilder;
import com.miotech.kun.workflow.db.sql.SQLBuilder;
import com.miotech.kun.workflow.utils.CronUtils;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class TaskDao {

    private final Logger logger = LoggerFactory.getLogger(TaskDao.class);

    public static final String TASK_TABLE_NAME = "kun_wf_task";

    public static final String TICK_TASK_MAPPING_TABLE_NAME = "kun_wf_tick_task_mapping";

    public static final String TASK_MODEL_NAME = "tasks";

    public static final String TICK_TASK_MAPPING_TABLE_ALIAS = "tick_task_mapping";

    public static final String TASK_RELATION_TABLE_NAME = "kun_wf_task_relations";

    public static final String TASK_RELATION_MODEL_NAME = "task_relation";

    private static final List<String> taskCols = ImmutableList.of("id", "name", "description", "operator_id", "arguments", "variable_defs", "schedule");

    private static final List<String> tickTaskCols = ImmutableList.of("task_id", "scheduled_tick");

    private static final List<String> taskRelationCols = ImmutableList.of("upstream_task_id", "downstream_task_id", "dependency_function");

    private final DatabaseOperator dbOperator;

    private final TaskDependencyFunctionProvider functionProvider;

    @Inject
    public TaskDao(DatabaseOperator dbOperator, TaskDependencyFunctionProvider functionProvider) {
        this.dbOperator = dbOperator;
        this.functionProvider = functionProvider;
    }

    public static List<String> getTaskCols() {
        return taskCols;
    }

    private void insertTickTaskRecordByScheduleConf(Long taskId, ScheduleConf scheduleConf, Clock clock) {
        boolean shouldInsertTickTask;

        switch (scheduleConf.getType()) {
            case SCHEDULED:
                shouldInsertTickTask = true;
                break;
            default:
                shouldInsertTickTask = false;
        }

        if (shouldInsertTickTask) {
            String cronExpression = scheduleConf.getCronExpr();
            Cron cron = CronUtils.convertStringToCron(cronExpression);

            List<String> tickTaskColumns = new ImmutableList.Builder<String>()
                    .addAll(tickTaskCols)
                    .build();

            String tickTaskInsertionSql = DefaultSQLBuilder.newBuilder()
                    .insert(tickTaskColumns.toArray(new String[0]))
                    .into(TICK_TASK_MAPPING_TABLE_NAME)
                    .asPrepared()
                    .getSQL();

            Optional<OffsetDateTime> nextExecutionTimeOptional = CronUtils.getNextExecutionTimeFromNow(cron, clock);
            String formattedScheduleTick;
            if (nextExecutionTimeOptional.isPresent()) {
                OffsetDateTime nextExecutionTime = nextExecutionTimeOptional.get();
                formattedScheduleTick = (new Tick(nextExecutionTime)).toString();
            } else {
                throw new RuntimeException(
                        String.format("Cannot compute next execution time for cron expression: \"%s\" (Task ID: %s)", cronExpression, taskId)
                );
            }

            dbOperator.update(
                    tickTaskInsertionSql,
                    taskId,
                    formattedScheduleTick
            );
        }
    }

    private void updateTaskUpstreamDependencies(Task task, List<TaskDependency> taskDependencies) {
        Preconditions.checkNotNull(task, "Invalid argument `task`: null");
        Preconditions.checkNotNull(taskDependencies, "Invalid argument `taskDependencies`: null");
        // 1. Clear all previous task upstream relations, if any
        String sqlRemove = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(TASK_RELATION_TABLE_NAME)
                .where("downstream_task_id = ?")
                .getSQL();

        // 2. then insert updated task upstream relations
        String sqlInsert = DefaultSQLBuilder.newBuilder()
                .insert(taskRelationCols.toArray(new String[0]))
                .into(TASK_RELATION_TABLE_NAME)
                .valueSize(taskRelationCols.size())
                .asPrepared()
                .getSQL();

        // 3. execute with database operator
        dbOperator.update(sqlRemove, task.getId());
        if (!taskDependencies.isEmpty()) {
            Object[][] params = taskDependencies
                    .stream()
                    .map(taskDependency -> new Object[]{
                            // upstream_task_id
                            taskDependency.getUpstreamTaskId(),
                            // downstream_task_id
                            task.getId(),
                            // dependency_function
                            taskDependency.getDependencyFunction().toFunctionType()
                    })
                    .collect(Collectors.toList())
                    .toArray(new Object[0][0]);
            dbOperator.batch(sqlInsert, params);
        }
    }

    /**
     * Fetch all relations in a `id - [dependencies]` hashmap whose downstream task ID is included in the given list
     * @param taskIds list of downstream task IDs
     * @return
     */
    private Map<Long, List<TaskDependency>> fetchAllRelationsFromDownstreamTaskIds(List<Long> taskIds) {
        Map<Long, List<TaskDependency>> taskIdToDependenciesMap = new HashMap<>();
        taskIds.forEach(taskId -> {
            taskIdToDependenciesMap.put(taskId, new ArrayList<>());
        });
        String idsFieldsPlaceholder = "(" + taskIds.stream().map(id -> "?")
                .collect(Collectors.joining(", ")) + ")";
        Map<String, List<String>> taskRelationColumnsMap = new HashMap<>();
        taskRelationColumnsMap.put(TASK_RELATION_MODEL_NAME, taskRelationCols);
        String sql = DefaultSQLBuilder.newBuilder()
                .columns(taskRelationColumnsMap)
                .from(TASK_RELATION_TABLE_NAME, TASK_RELATION_MODEL_NAME)
                .autoAliasColumns()
                .where(TASK_RELATION_MODEL_NAME + ".downstream_task_id IN " + idsFieldsPlaceholder)
                .orderBy(TASK_RELATION_MODEL_NAME + ".upstream_task_id ASC")
                .asPrepared()
                .getSQL();
        List<TaskDependency> allDeps = dbOperator.fetchAll(sql, TaskDependencyMapper.getInstance(functionProvider), taskIds.toArray());
        allDeps.forEach(dep -> {
            List<TaskDependency> dependencyList = taskIdToDependenciesMap.get(dep.getDownstreamTaskId());
            dependencyList.add(dep);
            taskIdToDependenciesMap.put(dep.getDownstreamTaskId(), dependencyList);
        });
        return taskIdToDependenciesMap;
    }

    private boolean deleteTickTaskMappingRecord(Long taskId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(TICK_TASK_MAPPING_TABLE_NAME)
                .where("task_id = ?")
                .getSQL();
        int affectedRows = dbOperator.update(sql, taskId);
        return affectedRows > 0;
    }

    private boolean deleteTaskRelations(Long taskId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(TASK_RELATION_TABLE_NAME)
                .where("(upstream_task_id = ?) OR (downstream_task_id = ?)")
                .getSQL();
        int affectedRows = dbOperator.update(sql, taskId, taskId);
        return affectedRows > 0;
    }

    private String getSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_MODEL_NAME, taskCols);
        SQLBuilder builder =  DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_TABLE_NAME, TASK_MODEL_NAME)
                .autoAliasColumns();

        if (StringUtils.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }

        return builder.getSQL();
    }

    public List<Task> fetchWithFilters(TaskSearchFilter filters) {
        Preconditions.checkArgument(Objects.nonNull(filters.getPageNum()) && filters.getPageNum() > 0, "Invalid page num: %d", filters.getPageNum());
        Preconditions.checkArgument(Objects.nonNull(filters.getPageSize()) && filters.getPageSize() > 0, "Invalid page size: %d", filters.getPageSize());
        boolean filterContainsKeyword = StringUtils.isNotEmpty(filters.getName());
        boolean filterContainsTags = CollectionUtils.isNotEmpty(filters.getTags());

        Integer pageNum = filters.getPageNum();
        Integer pageSize = filters.getPageSize();
        Integer offset = (pageNum - 1) * pageSize;
        List<Object> params = new ArrayList<>();

        StringBuilder whereClause = new StringBuilder();
        if (filterContainsKeyword && !filterContainsTags) {
            whereClause.append(" name LIKE CONCAT('%', ?, '%')");
            params.add(filters.getName());
        } else if (!filterContainsKeyword && filterContainsTags) {
            // TODO: allow filter by tags
            throw new UnsupportedOperationException("Filter by tags not implemented yet.");
        } else if (filterContainsKeyword && filterContainsTags) {
            // TODO: allow filter by tags
            throw new UnsupportedOperationException("Filter by tags not implemented yet.");
        }
        // else pass
        String baseSelect = getSelectSQL(null);
        String sql = DefaultSQLBuilder.newBuilder()
                .select(baseSelect)
                .where(whereClause.toString())
                .limit(pageSize)
                .offset(offset)
                .asPrepared()
                .getSQL();
        Collections.addAll(params, pageSize, offset);

        // For all tasks we directly fetched from task table, property `dependencies` will always be null
        List<Task> plainTasks = dbOperator.fetchAll(sql, TaskMapper.INSTANCE, params.toArray());

        // Retrieve all relations whose downstream ids are involved in result tasks
        List<Long> taskIds = plainTasks.stream().map(task -> task.getId()).collect(Collectors.toList());
        Map<Long, List<TaskDependency>> dependenciesMap = fetchAllRelationsFromDownstreamTaskIds(taskIds);

        // re-construct all tasks with full properties
        return plainTasks.stream()
                .map(t -> t.cloneBuilder()
                    .withDependencies(dependenciesMap.get(t.getId()))
                    .build())
                .collect(Collectors.toList());
    }

    public List<Task> fetchByOperatorId(Long operatorId) {
        Preconditions.checkNotNull(operatorId, "Invalid argument `operatorId`: null");
        String sql = getSelectSQL(TASK_MODEL_NAME + ".operator_id = ?");
        return dbOperator.fetchAll(sql, TaskMapper.INSTANCE, operatorId);
    }

    public Optional<Task> fetchById(Long taskId) {
        String sql = getSelectSQL(TASK_MODEL_NAME + ".id = ?");
        Task task = dbOperator.fetchOne(sql, TaskMapper.INSTANCE, taskId);
        if (Objects.isNull(task)) {
            return Optional.empty();
        }
        // else
        Map<String, List<String>> taskRelationColumnsMap = new HashMap<>();
        taskRelationColumnsMap.put(TASK_RELATION_MODEL_NAME, taskRelationCols);
        String dependenciesQuerySQL = DefaultSQLBuilder.newBuilder()
                .columns(taskRelationColumnsMap)
                .from(TASK_RELATION_TABLE_NAME, TASK_RELATION_MODEL_NAME)
                .autoAliasColumns()
                .where(TASK_RELATION_MODEL_NAME + ".downstream_task_id = ?")
                .orderBy(TASK_RELATION_MODEL_NAME + ".upstream_task_id ASC")
                .asPrepared()
                .getSQL();

        List<TaskDependency> dependencies = dbOperator.fetchAll(
                dependenciesQuerySQL,
                TaskDependencyMapper.getInstance(functionProvider),
                taskId
        );

        return Optional.of(task.cloneBuilder().withDependencies(dependencies).build());
    }

    public void create(Task task) {
        create(task, Clock.systemDefaultZone());
    }

    public void create(Task task, Clock mockClock) {
        /*
         * Creating a task consists of following steps:
         * 1. Insert task record into database
         * 2. Insert a tick-task mapping record according to schedule config
         * 3. For each dependency, insert records into table `kun_wf_task_relations`
         * Note: if any of the steps above failed, the entire insertion operation should be aborted and reverted.
         * */
        List<String> tableColumns = new ImmutableList.Builder<String>()
                .addAll(taskCols)
                .build();

        String sql = DefaultSQLBuilder.newBuilder()
                .insert(tableColumns.toArray(new String[0]))
                .into(TASK_TABLE_NAME)
                .asPrepared()
                .getSQL();

        dbOperator.transaction(() -> {
            dbOperator.update(
                    sql,
                    task.getId(),
                    task.getName(),
                    task.getDescription(),
                    task.getOperatorId(),
                    JSONUtils.toJsonString(task.getArguments()),
                    JSONUtils.toJsonString(task.getVariableDefs()),
                    JSONUtils.toJsonString(task.getScheduleConf())
            );
            insertTickTaskRecordByScheduleConf(task.getId(), task.getScheduleConf(), mockClock);
            updateTaskUpstreamDependencies(task, task.getDependencies());
            return null;
        });
    }

    public boolean update(Task task) {
        return update(task, Clock.systemDefaultZone());
    }

    public boolean update(Task task, Clock mockClock) {
        List<String> tableColumns = new ImmutableList.Builder<String>()
                .addAll(taskCols)
                .build();

        String sql = DefaultSQLBuilder .newBuilder()
                .update(TASK_TABLE_NAME)
                .set(tableColumns.toArray(new String[0]))
                .where("id = ?")
                .asPrepared()
                .getSQL();

        int affectedRows = dbOperator.transaction(() -> {
            int updatedRows = dbOperator.update(
                    sql,
                    task.getId(),
                    task.getName(),
                    task.getDescription(),
                    task.getOperatorId(),
                    JSONUtils.toJsonString(task.getArguments()),
                    JSONUtils.toJsonString(task.getVariableDefs()),
                    JSONUtils.toJsonString(task.getScheduleConf()),
                    task.getId()
            );

            // remove existing task mappings, if any
            deleteTickTaskMappingRecord(task.getId());
            // and re-insert by updated schedule configuration
            insertTickTaskRecordByScheduleConf(task.getId(), task.getScheduleConf(), mockClock);

            return updatedRows;
        });
        return affectedRows > 0;
    }

    public boolean deleteById(Long taskId) {
        return dbOperator.transaction(() -> {
            String sql = DefaultSQLBuilder.newBuilder()
                    .delete()
                    .from(TASK_TABLE_NAME)
                    .where("id = ?")
                    .getSQL();
            int affectedRows = dbOperator.update(sql, taskId);
            // remove existing task mappings, if any
            deleteTickTaskMappingRecord(taskId);
            // remove existing task relations, if any
            deleteTaskRelations(taskId);
            return affectedRows > 0;
        });
    }

    public Optional<Tick> fetchNextExecutionTickByTaskId(Long taskId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("scheduled_tick")
                .from(TICK_TASK_MAPPING_TABLE_NAME)
                .where("task_id = ?")
                .orderBy("scheduled_tick ASC")
                .limit(1)
                .toString();
        String nextExecutionTimeString = (String) dbOperator.fetchOne(
                sql,
                (ResultSetMapper) rs -> rs.getString("scheduled_tick"),
                taskId
        );
        if (Objects.nonNull(nextExecutionTimeString)) {
            return Optional.of(new Tick(nextExecutionTimeString));
        } else {
            return Optional.empty();
        }
    }

    public List<Task> fetchScheduledTaskAtTick(Tick tick) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(getSelectSQL(null))
                .join("INNER", TICK_TASK_MAPPING_TABLE_NAME, TICK_TASK_MAPPING_TABLE_ALIAS)
                .on(TASK_MODEL_NAME + ".id = " + TICK_TASK_MAPPING_TABLE_ALIAS + ".task_id")
                .where(TICK_TASK_MAPPING_TABLE_ALIAS + ".scheduled_tick <= ?")
                .getSQL();
        return dbOperator.fetchAll(sql, TaskMapper.INSTANCE, tick.toString());
    }

    public static class TaskMapper implements ResultSetMapper<Task> {
        public static final TaskDao.TaskMapper INSTANCE = new TaskMapper();

        @Override
        public Task map(ResultSet rs) throws SQLException {
            return Task.newBuilder()
                    .withId(rs.getLong(TASK_MODEL_NAME + "_id"))
                    .withName(rs.getString(TASK_MODEL_NAME + "_name"))
                    .withDescription(rs.getString(TASK_MODEL_NAME + "_description"))
                    .withOperatorId(rs.getLong(TASK_MODEL_NAME + "_operator_id"))
                    .withArguments(JSONUtils.jsonToObject(rs.getString(TASK_MODEL_NAME + "_arguments"), new TypeReference<List<Param>>() {}))
                    .withVariableDefs(JSONUtils.jsonToObject(rs.getString(TASK_MODEL_NAME + "_variable_defs"), new TypeReference<List<Variable>>() {}))
                    .withScheduleConf( JSONUtils.jsonToObject(rs.getString(TASK_MODEL_NAME + "_schedule"), new TypeReference<ScheduleConf>() {}))
                    .withDependencies(new ArrayList<>())
                    .build();
        }
    }

    public static class TaskDependencyMapper implements ResultSetMapper<TaskDependency> {
        private TaskDependencyFunctionProvider functionProvider;

        private static TaskDependencyMapper instance;

        private TaskDependencyMapper(TaskDependencyFunctionProvider functionProvider) {
            this.functionProvider = functionProvider;
        }

        public static TaskDependencyMapper getInstance(TaskDependencyFunctionProvider functionProvider) {
            if (instance == null) {
                instance = new TaskDependencyMapper(functionProvider);
            }
            return instance;
        }

        @Override
        public TaskDependency map(ResultSet rs) throws SQLException {
            return new TaskDependency(
                    rs.getLong(TASK_RELATION_MODEL_NAME + "_upstream_task_id"),
                    functionProvider.from(rs.getString(TASK_RELATION_MODEL_NAME + "_dependency_function")),
                    rs.getLong(TASK_RELATION_MODEL_NAME + "_downstream_task_id")
            );
        }
    }
}
