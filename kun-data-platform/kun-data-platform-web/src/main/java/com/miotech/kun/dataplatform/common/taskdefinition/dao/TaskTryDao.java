package com.miotech.kun.dataplatform.common.taskdefinition.dao;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskTry;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TaskTryDao {
    private static final String TASK_TRY_TABLE_NAME = "kun_dp_task_try";

    private static final String TASK_TRY_MODEL_NAME = "tasktry";

    private static final List<String> taskTryCols = ImmutableList.of("id", "definition_id", "wf_task_id", "wf_task_run_id", "task_config", "creator");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String getSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_TRY_MODEL_NAME, taskTryCols);
        SQLBuilder builder =  DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_TRY_TABLE_NAME, TASK_TRY_MODEL_NAME)
                .autoAliasColumns();

        if (StringUtils.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }

        return builder.getSQL();
    }

    public Optional<TaskTry> fetchById(Long id) {
        String sql = getSelectSQL(TASK_TRY_MODEL_NAME + ".id = ?");
        List<TaskTry> taskTries = jdbcTemplate.query(sql, TaskTryMapper.INSTANCE, id);
        return taskTries.stream().findAny();
    }

    public void create(TaskTry taskTry) {

        String sql = DefaultSQLBuilder.newBuilder()
                .insert(taskTryCols.toArray(new String[0]))
                .into(TASK_TRY_TABLE_NAME)
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(
                sql,
                taskTry.getId(),
                taskTry.getDefinitionId(),
                taskTry.getWorkflowTaskId(),
                taskTry.getWorkflowTaskRunId(),
                JSONUtils.toJsonString(taskTry.getTaskConfig()),
                taskTry.getCreator()
        );
    }

    public static class TaskTryMapper implements RowMapper<TaskTry> {
        public static final TaskTryMapper INSTANCE = new TaskTryMapper();

        @Override
        public TaskTry mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TaskTry.newBuilder()
                    .withId(rs.getLong(TASK_TRY_MODEL_NAME + "_id"))
                    .withDefinitionId(rs.getLong(TASK_TRY_MODEL_NAME + "_definition_id"))
                    .withWorkflowTaskId(rs.getLong(TASK_TRY_MODEL_NAME + "_wf_task_id"))
                    .withWorkflowTaskRunId(rs.getLong(TASK_TRY_MODEL_NAME + "_wf_task_run_id"))
                    .withTaskConfig(JSONUtils.jsonToObject(rs.getString(TASK_TRY_MODEL_NAME + "_task_config"), JSONObject.class))
                    .withCreator(rs.getLong(TASK_TRY_MODEL_NAME + "_creator"))
                    .build();
        }
    }

}
