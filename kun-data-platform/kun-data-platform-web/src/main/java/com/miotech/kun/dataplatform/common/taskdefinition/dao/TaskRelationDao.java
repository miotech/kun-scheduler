package com.miotech.kun.dataplatform.common.taskdefinition.dao;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskRelation;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskPayload;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskRelation;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class TaskRelationDao {
    private static final String TASK_DEF_RELATION_TABLE_NAME = "kun_dp_task_relation";

    private static final String TASK_RELATION_MODEL_NAME = "taskrelation";

    private static final List<String> taskRelationCols = ImmutableList.of("upstream_task_id", "downstream_task_id", "created_at", "updated_at");


    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String getSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_RELATION_MODEL_NAME, taskRelationCols);
        SQLBuilder builder =  DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_DEF_RELATION_TABLE_NAME, TASK_RELATION_MODEL_NAME)
                .autoAliasColumns();

        if (StringUtils.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }

        return builder.getSQL();
    }

    public List<TaskRelation> fetchByUpstreamId(Long taskDefId){
        String sql = getSelectSQL(TASK_RELATION_MODEL_NAME + String.format(".upstream_task_id = ?"));
        List<TaskRelation> taskDefinitions = jdbcTemplate.query(sql, TaskRelationDao.TaskRelationMapper.INSTANCE, taskDefId);
        return taskDefinitions;
    }

    public List<TaskRelation> fetchByDownstreamId(Long taskDefId){
        String sql = getSelectSQL(TASK_RELATION_MODEL_NAME + String.format(".downstream_task_id = ?"));
        List<TaskRelation> taskDefinitions = jdbcTemplate.query(sql, TaskRelationDao.TaskRelationMapper.INSTANCE, taskDefId);
        return taskDefinitions;
    }

    public void create(List<TaskRelation> taskRelations) {

        String sql = DefaultSQLBuilder.newBuilder()
                .insert(taskRelationCols.toArray(new String[0]))
                .into(TASK_DEF_RELATION_TABLE_NAME)
                .asPrepared()
                .getSQL();

        List<Object[]> params = taskRelations.stream()
                .map (taskRelation -> new Object[] { taskRelation.getUpstreamId(),
                        taskRelation.getDownstreamId(),
                        taskRelation.getCreatedAt(),
                        taskRelation.getUpdatedAt() })
                .collect(Collectors.toList());
        jdbcTemplate.batchUpdate(sql, params);

    }

    public void delete(Long taskDefId) {
        String sqlRemove = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(TASK_DEF_RELATION_TABLE_NAME)
                .where("downstream_task_id = ?")
                .getSQL();
        jdbcTemplate.update(sqlRemove, taskDefId);
    }

    public static class TaskRelationMapper implements RowMapper<TaskRelation> {
        public static final TaskRelationDao.TaskRelationMapper INSTANCE = new TaskRelationDao.TaskRelationMapper();

        @Override
        public TaskRelation mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TaskRelation.newBuilder()
                    .withUpstreamId(rs.getLong(TASK_RELATION_MODEL_NAME + "_upstream_task_id"))
                    .withDownstreamId(rs.getLong(TASK_RELATION_MODEL_NAME + "_downstream_task_id"))
                    .withCreatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RELATION_MODEL_NAME + "_created_at")))
                    .withUpdatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RELATION_MODEL_NAME + "_updated_at")))
                    .build();
        }
    }
}
