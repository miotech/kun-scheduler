package com.miotech.kun.common.taskrun.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.common.dao.Constants;
import com.miotech.kun.common.task.dao.TaskDao;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.entity.Entity;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.db.ResultSetMapper;
import com.miotech.kun.workflow.db.sql.DefaultSQLBuilder;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.JSONUtils;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;

@Singleton
public class TaskRunDao {
    private static final String TASK_RUN_MODEL_NAME = "taskrun";
    private static final String TASKRUN_TABLE_NAME = "kun_wf_task_run";
    private static final List<String> taskRunCols = ImmutableList.copyOf(
            new String[]{"id", "task_id", "scheduled_tick", "status", "start_at", "end_at", "variables", "inlets", "outlets"});

    private static final String TASK_ATTEMPT_MODEL_NAME = "taskattempt";
    private static final String TASK_ATTEMPT_TABLE_NAME = "kun_wf_task_attempt";
    private static final List<String> taskAttemptCols = ImmutableList.copyOf(
            new String[]{"id", "task_run_id", "attempt", "status", "start_at", "end_at", "log_path"});

    private static final String RELATION_TABLE_NAME = "kun_wf_task_run_relations";
    private static final List<String> taskRunRelationCols = ImmutableList.copyOf(
            new String[]{"upstream_task_run_id", "downstream_task_run_id"});

    @Inject
    private TaskDao taskDao;

    @Inject
    private DatabaseOperator dbOperator;

    public TaskRun fetchById(Long id) {
        TaskRun taskRun = dbOperator.fetchOne(getSelectSQL(TASK_RUN_MODEL_NAME + ".id = ?"), TaskRunMapper.INSTANCE, id);

        List<Long>  dependencies = fetchTaskRunDependencies(id);
        return taskRun.cloneBuilder()
                .withDependencies(dependencies)
                .build();
    }

    private String getSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_RUN_MODEL_NAME, taskRunCols);
        columnsMap.put(TaskDao.TASK_MODEL_NAME, TaskDao.getTaskCols());

        return DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASKRUN_TABLE_NAME, TASK_RUN_MODEL_NAME)
                .join("LEFT OUTER", taskDao.TASK_TABLE_NAME, taskDao.TASK_MODEL_NAME)
                .autoAliasColumns()
                .where(whereClause)
                .getSQL();
    }

    public TaskRun createTaskRun(TaskRun taskRun) {
        dbOperator.transaction( () -> {
            List<String> tableColumns = new ImmutableList.Builder<String>()
                    .addAll(taskRunCols)
                    .add(Constants.CREATE_COL)
                    .add(Constants.UPDATE_COL)
                    .build();

            String sql = DefaultSQLBuilder .newBuilder()
                    .insert(tableColumns.toArray(new String[0]))
                    .into(TASKRUN_TABLE_NAME)
                    .asPrepared()
                    .getSQL();

            OffsetDateTime nowTime = OffsetDateTime.now();

            dbOperator.update(sql,
                    taskRun.getId(),
                    taskRun.getTask().getId(),
                    taskRun.getScheduledTick().toString(),
                    taskRun.getStatus().toString(),
                    taskRun.getStartAt(),
                    taskRun.getEndAt(),
                    JSONUtils.toJsonString(taskRun.getVariables()),
                    JSONUtils.toJsonString(taskRun.getInlets()),
                    JSONUtils.toJsonString(taskRun.getOutlets()),
                    nowTime,
                    nowTime
                    );

            createTaskRunDependencies(taskRun.getId(), taskRun.getDependencies());
            return taskRun;
        });

        return taskRun;
    }

    public TaskRun updateTaskRun(TaskRun taskRun) {
        dbOperator.transaction( () -> {
            List<String> tableColumns = new ImmutableList.Builder<String>()
                    .addAll(taskRunCols)
                    .add(Constants.UPDATE_COL)
                    .build();

            String sql = DefaultSQLBuilder .newBuilder()
                    .update(TASKRUN_TABLE_NAME)
                    .set(tableColumns.toArray(new String[0]))
                    .where("id = ?")
                    .asPrepared()
                    .getSQL();

            OffsetDateTime nowTime = OffsetDateTime.now();
            dbOperator.update(sql,
                    taskRun.getId(),
                    taskRun.getTask().getId(),
                    taskRun.getScheduledTick().toString(),
                    taskRun.getStatus().toString(),
                    taskRun.getStartAt(),
                    taskRun.getEndAt(),
                    JSONUtils.toJsonString(taskRun.getVariables()),
                    JSONUtils.toJsonString(taskRun.getInlets()),
                    JSONUtils.toJsonString(taskRun.getOutlets()),
                    nowTime,
                    taskRun.getId()
            );

            deleteTaskRunDependencies(taskRun.getId());
            createTaskRunDependencies(taskRun.getId(), taskRun.getDependencies());
            return taskRun;
        });

        return taskRun;
    }

    public boolean deleteTaskRun(Long taskRunId) {
        return dbOperator.transaction(() -> {

            deleteTaskRunDependencies(taskRunId);
            deleteTaskAttempts(taskRunId);
            String deleteSQL = DefaultSQLBuilder.newBuilder()
                    .delete()
                    .from(TASKRUN_TABLE_NAME)
                    .where("id = ?")
                    .getSQL();
            return dbOperator.update(deleteSQL, taskRunId) >= 0;
        });
    }

    public List<TaskAttempt> fetchAttemptsByTaskRunId(Long taskRunId) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_ATTEMPT_MODEL_NAME, taskAttemptCols);

        String sql = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_ATTEMPT_TABLE_NAME, TASK_ATTEMPT_MODEL_NAME)
                .autoAliasColumns()
                .where(TASK_ATTEMPT_MODEL_NAME + ".task_run_id = ?")
                .getSQL();
        return dbOperator.fetchAll(sql, TaskAttemptMapper.INSTANCE, taskRunId);
    }

    public TaskAttempt fetchAttemptById(Long attemptId) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_ATTEMPT_MODEL_NAME, taskAttemptCols);

        String sql = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_ATTEMPT_TABLE_NAME, TASK_ATTEMPT_MODEL_NAME)
                .autoAliasColumns()
                .where(TASK_ATTEMPT_MODEL_NAME + ".id = ?")
                .getSQL();
        return dbOperator.fetchOne(sql, TaskAttemptMapper.INSTANCE, attemptId);
    }

    public TaskAttempt createAttempt(TaskAttempt taskAttempt) {
        List<String> tableColumns = new ImmutableList.Builder<String>()
                .addAll(taskAttemptCols)
                .add(Constants.CREATE_COL)
                .add(Constants.UPDATE_COL)
                .build();

        String sql = DefaultSQLBuilder .newBuilder()
                .insert(tableColumns.toArray(new String[0]))
                .into(TASK_ATTEMPT_TABLE_NAME)
                .asPrepared()
                .getSQL();

        OffsetDateTime nowTime = OffsetDateTime.now();
        dbOperator.update(sql,
                taskAttempt.getId(),
                taskAttempt.getTaskRun().getId(),
                taskAttempt.getAttempt(),
                taskAttempt.getStatus().toString(),
                taskAttempt.getStartAt(),
                taskAttempt.getEndAt(),
                taskAttempt.getLogPath(),
                nowTime,
                nowTime
        );
        return taskAttempt;
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
                .add(Constants.CREATE_COL)
                .add(Constants.UPDATE_COL)
                .build();

        String dependencySQL = DefaultSQLBuilder .newBuilder()
                .insert(tableColumns.toArray(new String[0]))
                .into(RELATION_TABLE_NAME)
                .asPrepared()
                .getSQL();

        Object[][] params = new Object[dependencies.size()][2];
        OffsetDateTime nowTime = OffsetDateTime.now();
        for (int i = 0; i < dependencies.size(); i++) {
            params[i] = new Object[]{dependencies.get(i), taskRunId, nowTime, nowTime};
        }

        dbOperator.batch(dependencySQL, params);
    }

    public static class TaskRunMapper implements ResultSetMapper<TaskRun> {
        public static final TaskRunDao.TaskRunMapper INSTANCE = new TaskRunDao.TaskRunMapper();
        private TaskDao.TaskMapper taskMapper = TaskDao.TaskMapper.INSTANCE;

        @Override
        public TaskRun map(ResultSet rs) throws SQLException {
            rs.getLong(TaskRunDao.TASK_RUN_MODEL_NAME + "_task_id");
            Task task = !rs.wasNull() ? taskMapper.map(rs) : null;

            return TaskRun.newBuilder()
                    .withTask(task)
                    .withId(rs.getLong(TASK_RUN_MODEL_NAME + "_id"))
                    .withScheduledTick(new Tick(rs.getString(TASK_RUN_MODEL_NAME + "_scheduled_tick")))
                    .withStatus(TaskRunStatus.resolve(rs.getString(TASK_RUN_MODEL_NAME + "_status")))
                    .withInlets(JSONUtils.jsonToObject(rs.getString(TASK_RUN_MODEL_NAME + "_inlets"), new TypeReference<List<Entity>>() {}))
                    .withOutlets(JSONUtils.jsonToObject(rs.getString(TASK_RUN_MODEL_NAME + "_outlets"), new TypeReference<List<Entity>>() {}))
                    .withDependencies(Collections.emptyList())
                    .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_start_at")))
                    .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_end_at")))
                    .build();
        }
    }

    private static class TaskAttemptMapper implements ResultSetMapper<TaskAttempt> {
        public static TaskRunDao.TaskAttemptMapper INSTANCE = new TaskRunDao.TaskAttemptMapper();

        @Override
        public TaskAttempt map(ResultSet rs) throws SQLException {
            return TaskAttempt.newBuilder()
                    .withId(rs.getLong(TASK_ATTEMPT_MODEL_NAME + "_id"))
                    .withStatus(TaskRunStatus.resolve(rs.getString(TASK_ATTEMPT_MODEL_NAME + "_status")))
                    .withAttempt(rs.getInt(TASK_ATTEMPT_MODEL_NAME + "_attempt"))
                    .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_ATTEMPT_MODEL_NAME + "_start_at")))
                    .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_ATTEMPT_MODEL_NAME + "_end_at")))
                    .build();
        }
    }
}
