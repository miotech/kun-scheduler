package com.miotech.kun.workflow.common.task.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.miotech.kun.workflow.common.task.filter.TaskSearchFilter;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.dao.Constants;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.db.ResultSetMapper;
import com.miotech.kun.workflow.db.sql.DefaultSQLBuilder;
import com.miotech.kun.workflow.db.sql.SQLBuilder;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;

@Singleton
public class TaskDao {
    private final Logger logger = LoggerFactory.getLogger(TaskDao.class);

    public final static String TASK_TABLE_NAME = "kun_wf_task";
    public final static String TASK_MODEL_NAME = "tasks";

    private static final List<String> taskCols = ImmutableList.copyOf(
            new String[]{"id", "name", "description", "operator_id", "arguments", "variable_defs", "schedule"});

    private final DatabaseOperator dbOperator;

    @Inject
    public TaskDao(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
    }

    public static List<String> getTaskCols() {
        return taskCols;
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

        return dbOperator.fetchAll(sql, TaskMapper.INSTANCE, params.toArray());
    }

    public List<Task> fetchWithOperatorId(Long operatorId) {
        Preconditions.checkNotNull(operatorId, "Invalid argument `operatorId`: null");
        String sql = getSelectSQL(TASK_MODEL_NAME + ".operator_id = ?");
        return dbOperator.fetchAll(sql, TaskMapper.INSTANCE, operatorId);
    }

    public Optional<Task> fetchById(Long taskId) {
        String sql = getSelectSQL(TASK_MODEL_NAME + ".id = ?");
        Task task = dbOperator.fetchOne(sql, TaskMapper.INSTANCE, taskId);
        return Optional.ofNullable(task);
    }

    public List<Task> fetchScheduledTaskAtTick(Tick tick) {
        String sql = getSelectSQL(TASK_MODEL_NAME + ".scheduled_tick = ?");
        return dbOperator.fetchAll(sql, TaskMapper.INSTANCE, tick.toString());
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

    public void create(Task task) {
        List<String> tableColumns = new ImmutableList.Builder<String>()
                .addAll(taskCols)
                .add(Constants.CREATE_COL)
                .add(Constants.UPDATE_COL)
                .build();

        String sql = DefaultSQLBuilder.newBuilder()
                .insert(tableColumns.toArray(new String[0]))
                .into(TASK_TABLE_NAME)
                .asPrepared()
                .getSQL();

        OffsetDateTime nowTime = OffsetDateTime.now();
        dbOperator.update(
                sql,
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getOperatorId(),
                JSONUtils.toJsonString(task.getArguments()),
                JSONUtils.toJsonString(task.getVariableDefs()),
                JSONUtils.toJsonString(task.getScheduleConf()),
                nowTime,
                nowTime
        );
    }

    public boolean update(Task task) {
        List<String> tableColumns = new ImmutableList.Builder<String>()
                .addAll(taskCols)
                .add(Constants.UPDATE_COL)
                .build();

        String sql = DefaultSQLBuilder .newBuilder()
                .update(TASK_TABLE_NAME)
                .set(tableColumns.toArray(new String[0]))
                .where("id = ?")
                .asPrepared()
                .getSQL();

        OffsetDateTime nowTime = OffsetDateTime.now();
        int affectedRows = dbOperator.update(
                sql,
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getOperatorId(),
                JSONUtils.toJsonString(task.getArguments()),
                JSONUtils.toJsonString(task.getVariableDefs()),
                JSONUtils.toJsonString(task.getScheduleConf()),
                nowTime,
                task.getId()
        );
        return affectedRows > 0;
    }

    public boolean deleteById(Long taskId) {
        String sql = DefaultSQLBuilder .newBuilder()
                .delete()
                .from(TASK_TABLE_NAME)
                .where("id = ?")
                .getSQL();
        int affectedRows = dbOperator.update(sql, taskId);
        return affectedRows > 0;
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
                    .build();
        }
    }
}
