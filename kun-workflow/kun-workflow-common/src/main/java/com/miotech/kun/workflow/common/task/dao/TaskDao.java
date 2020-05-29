package com.miotech.kun.workflow.common.task.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.miotech.kun.workflow.common.task.filter.TaskSearchFilter;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.db.ResultSetMapper;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Singleton
public class TaskDao {
    private final Logger logger = LoggerFactory.getLogger(TaskDao.class);

    private final DatabaseOperator dbOperator;

    private final String DB_TABLE_NAME = "kun_wf_task";

    @Inject
    public TaskDao(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
    }

    public List<Task> fetchScheduledTaskAtTick(Tick tick) {
        String sql = String.format("select * from %s where scheduled_tick = ?", DB_TABLE_NAME);
        return dbOperator.fetchAll(sql, TaskMapper.INSTANCE, tick.toString());
    }

    public List<Task> getList(TaskSearchFilter filters) {
        Preconditions.checkArgument(Objects.nonNull(filters.getPageNum()) && filters.getPageNum() > 0, "Invalid page num: %d", filters.getPageNum());
        Preconditions.checkArgument(Objects.nonNull(filters.getPageSize()) && filters.getPageSize() > 0, "Invalid page size: %d", filters.getPageSize());
        boolean filterContainsKeyword = StringUtils.isNotEmpty(filters.getName());
        boolean filterContainsTags = CollectionUtils.isNotEmpty(filters.getTags());

        Integer pageNum = filters.getPageNum();
        Integer pageSize = filters.getPageSize();
        Integer offset = (pageNum - 1) * pageSize;
        StringBuilder sqlBuilder = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sqlBuilder.append(String.format("SELECT id, name, description, operator_id, arguments, variable_defs, schedule FROM %s", DB_TABLE_NAME));

        if (filterContainsKeyword && !filterContainsTags) {
            sqlBuilder.append(" WHERE name LIKE CONCAT('%', ?, '%')");
            params.add(filters.getName());
        } else if (!filterContainsKeyword && filterContainsTags) {
            // TODO: allow filter by tags
            throw new UnsupportedOperationException("Filter by tags not implemented yet.");
        } else if (filterContainsKeyword && filterContainsTags) {
            // TODO: allow filter by tags
            throw new UnsupportedOperationException("Filter by tags not implemented yet.");
        }
        // else pass

        sqlBuilder.append(" LIMIT ?, ?");
        Collections.addAll(params, offset, pageSize);

        return dbOperator.fetchAll(sqlBuilder.toString(), TaskMapper.INSTANCE, params.toArray());
    }

    public Optional<Task> getById(Long taskId) {
        String sql = "SELECT id, name, description, operator_id, arguments, variable_defs, schedule " +
                String.format("FROM %s t WHERE t.id = ?", DB_TABLE_NAME);
        Task task = dbOperator.fetchOne(sql, TaskMapper.INSTANCE, taskId);
        return Optional.ofNullable(task);
    }

    public void create(Task task) {
        String sql = String.format("INSERT INTO %s (id, name, description, operator_id, arguments, variable_defs, schedule) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)", DB_TABLE_NAME);
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
    }

    public void updateById(Long id, Task task) {
        String sql = String.format("UPDATE %s SET " +
                "name = ?, description = ?, operator_id = ?, arguments = ?, variable_defs = ?, schedule = ? " +
                "WHERE id = ?", DB_TABLE_NAME);
        dbOperator.update(
                sql,
                task.getName(),
                task.getDescription(),
                task.getOperatorId(),
                JSONUtils.toJsonString(task.getArguments()),
                JSONUtils.toJsonString(task.getVariableDefs()),
                JSONUtils.toJsonString(task.getScheduleConf()),
                task.getId()
        );
    }

    public void deleteById(Long taskId) {
        String sql = String.format("DELETE FROM %s WHERE id = ?", DB_TABLE_NAME);
        dbOperator.update(sql, taskId);
    }

    private static class TaskMapper implements ResultSetMapper<Task> {
        public static final TaskMapper INSTANCE = new TaskMapper();

        @Override
        public Task map(ResultSet rs) throws SQLException {
            return Task.newBuilder()
                    .withId(rs.getLong("id"))
                    .withName(rs.getString("name"))
                    .withDescription(rs.getString("description"))
                    .withOperatorId(rs.getLong("operator_id"))
                    .withArguments(JSONUtils.jsonToObject(rs.getString("arguments"), new TypeReference<List<Param>>() {}))
                    .withVariableDefs(JSONUtils.jsonToObject(rs.getString("variable_defs"), new TypeReference<List<Variable>>() {}))
                    .withScheduleConf( JSONUtils.jsonToObject(rs.getString("schedule"), new TypeReference<ScheduleConf>() {}))
                    .build();
        }
    }
}
