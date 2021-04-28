package com.miotech.kun.dataplatform.common.taskdefinition.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.db.sql.SQLUtils;
import com.miotech.kun.dataplatform.common.taskdefinition.vo.TaskDefinitionSearchRequest;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskPayload;
import com.miotech.kun.workflow.client.model.PaginationResult;
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

@Repository
public class TaskDefinitionDao {
    private static final String TASK_DEF_TABLE_NAME = "kun_dp_task_definition";

    private static final String TASK_DEF_MODEL_NAME = "taskdef";

    private static final List<String> taskDefCols = ImmutableList.of("id", "definition_id", "name", "task_template_name", "task_payload", "creator", "owner", "is_archived", "last_modifier", "create_time", "update_time");

    private static final String VIEW_AND_TASK_DEF_RELATION_TABLE_NAME = "kun_dp_view_task_definition_relation";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String getSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_DEF_MODEL_NAME, taskDefCols);
        SQLBuilder builder =  DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_DEF_TABLE_NAME, TASK_DEF_MODEL_NAME)
                .autoAliasColumns();

        if (StringUtils.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }

        return builder.getSQL();
    }

    public Optional<TaskDefinition> fetchById(Long taskId) {
        String sql = getSelectSQL(TASK_DEF_MODEL_NAME + ".definition_id = ?");
        List<TaskDefinition> taskDefinitions = jdbcTemplate.query(sql, TaskDefinitionMapper.INSTANCE, taskId);
        return taskDefinitions.stream().findAny();
    }

    public List<TaskDefinition> fetchByIds(List<Long> ids) {
        Preconditions.checkNotNull(ids, "ids should not be `null`");
        if (ids.isEmpty()) return ImmutableList.of();
        String sql = getSelectSQL(TASK_DEF_MODEL_NAME + String.format(".definition_id in (%s)", com.miotech.kun.commons.utils.StringUtils.repeatJoin("?", ",", ids.size())));
        return jdbcTemplate.query(sql, TaskDefinitionMapper.INSTANCE, ids.toArray());
    }

    public List<TaskDefinition> fetchAll() {
        String sql = getSelectSQL(TASK_DEF_MODEL_NAME + ".is_archived = ?");
        return jdbcTemplate.query(sql, TaskDefinitionMapper.INSTANCE, false);
    }

    public PaginationResult<TaskDefinition> search(TaskDefinitionSearchRequest searchRequest) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" (1 = 1)");
        List<Object> params = new ArrayList();
        List<Long> definitionIds = searchRequest.getDefinitionIds();
        List<Long> viewIds = searchRequest.getViewIds();

        if (!definitionIds.isEmpty()) {
            whereClause.append(" AND ");
            whereClause.append(String.format(TASK_DEF_MODEL_NAME + ".definition_id in (%s)", SQLUtils.generateSqlInClausePlaceholders(definitionIds)));
            params.addAll(definitionIds);
        }

        if (Objects.nonNull(viewIds) && (!viewIds.isEmpty())) {
            whereClause.append(" AND ");
            String inClausePlaceholders = SQLUtils.generateSqlInClausePlaceholders(viewIds);
            whereClause.append("(" + TASK_DEF_MODEL_NAME + ".definition_id IN (SELECT task_def_id FROM " +
                    VIEW_AND_TASK_DEF_RELATION_TABLE_NAME + " WHERE view_id IN (" + inClausePlaceholders + ")))"
            );
            params.addAll(viewIds);
        }

        List<Long> creatorIds = searchRequest.getCreatorIds();
        if (!creatorIds.isEmpty()) {
            whereClause.append(" AND ");
            whereClause.append(String.format(TASK_DEF_MODEL_NAME + ".creator in (%s)", com.miotech.kun.commons.utils.StringUtils.repeatJoin("?", ",", creatorIds.size())));
            params.addAll(creatorIds);
        }

        if (StringUtils.isNoneBlank(searchRequest.getName())) {
            whereClause.append(" AND ");
            whereClause.append(TASK_DEF_MODEL_NAME + ".name ILIKE CONCAT('%', CAST(? AS TEXT) , '%')");
            params.add(searchRequest.getName());
        }

        if (StringUtils.isNoneBlank(searchRequest.getTaskTemplateName())) {
            whereClause.append(" AND ");
            whereClause.append(TASK_DEF_MODEL_NAME + ".task_template_name = ?");
            params.add(searchRequest.getTaskTemplateName());
        }

        if (searchRequest.getArchived().isPresent()) {
            whereClause.append(" AND ");
            whereClause.append(TASK_DEF_MODEL_NAME + ".is_archived = ?");
            params.add(searchRequest.getArchived().get());
        }


        String countSql = DefaultSQLBuilder.newBuilder()
                .select("COUNT(1)")
                .from(TASK_DEF_TABLE_NAME, TASK_DEF_MODEL_NAME)
                .where(whereClause.toString())
                .getSQL();
        Integer totalCount = jdbcTemplate.query(
                countSql,
                (rse) -> rse.next() ? rse.getInt(1): 0,
                params.toArray());
        String sql = DefaultSQLBuilder.newBuilder()
                .select(getSelectSQL(whereClause.toString()))
                .orderBy("id")
                .limit(searchRequest.getPageSize())
                .offset(searchRequest.getPageSize() * (searchRequest.getPageNum()-1))
                .getSQL();

        List<TaskDefinition> taskCommits = jdbcTemplate.query(sql, TaskDefinitionMapper.INSTANCE, params.toArray());
        return new PaginationResult<>(
                searchRequest.getPageSize(),
                searchRequest.getPageNum(),
                totalCount,
                taskCommits);
    }

    public boolean archive(Long taskId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(TASK_DEF_TABLE_NAME)
                .set("is_archived")
                .where("definition_id = ?")
                .asPrepared()
                .getSQL();
        return jdbcTemplate.update(sql, true, taskId) > 0;
    }

    public void create(TaskDefinition taskDefinition) {

        String sql = DefaultSQLBuilder.newBuilder()
                .insert(taskDefCols.toArray(new String[0]))
                .into(TASK_DEF_TABLE_NAME)
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(
                sql,
                taskDefinition.getId(),
                taskDefinition.getDefinitionId(),
                taskDefinition.getName(),
                taskDefinition.getTaskTemplateName(),
                JSONUtils.toJsonString(taskDefinition.getTaskPayload()),
                taskDefinition.getCreator(),
                taskDefinition.getOwner(),
                taskDefinition.isArchived(),
                taskDefinition.getLastModifier(),
                taskDefinition.getCreateTime(),
                taskDefinition.getUpdateTime()
        );
    }

    public void update(TaskDefinition taskDefinition) {

        String sql = DefaultSQLBuilder.newBuilder()
                .update(TASK_DEF_TABLE_NAME)
                .set(taskDefCols.toArray(new String[0]))
                .where("id = ?")
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(
                sql,
                taskDefinition.getId(),
                taskDefinition.getDefinitionId(),
                taskDefinition.getName(),
                taskDefinition.getTaskTemplateName(),
                JSONUtils.toJsonString(taskDefinition.getTaskPayload()),
                taskDefinition.getCreator(),
                taskDefinition.getOwner(),
                taskDefinition.isArchived(),
                taskDefinition.getLastModifier(),
                taskDefinition.getCreateTime(),
                taskDefinition.getUpdateTime(),
                taskDefinition.getId()
        );
    }

    public static class TaskDefinitionMapper implements RowMapper<TaskDefinition> {
        public static final TaskDefinitionDao.TaskDefinitionMapper INSTANCE = new TaskDefinitionMapper();

        @Override
        public TaskDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TaskDefinition.newBuilder()
                    .withId(rs.getLong(TASK_DEF_MODEL_NAME + "_id"))
                    .withDefinitionId(rs.getLong(TASK_DEF_MODEL_NAME + "_definition_id"))
                    .withName(rs.getString(TASK_DEF_MODEL_NAME + "_name"))
                    .withTaskTemplateName(rs.getString(TASK_DEF_MODEL_NAME + "_task_template_name"))
                    .withTaskPayload(JSONUtils.jsonToObject(rs.getString(TASK_DEF_MODEL_NAME + "_task_payload"), TaskPayload.class))
                    .withCreator(rs.getLong(TASK_DEF_MODEL_NAME + "_creator"))
                    .withOwner(rs.getLong(TASK_DEF_MODEL_NAME + "_owner"))
                    .withArchived(rs.getBoolean(TASK_DEF_MODEL_NAME + "_is_archived"))
                    .withLastModifier(rs.getLong(TASK_DEF_MODEL_NAME + "_last_modifier"))
                    .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_DEF_MODEL_NAME + "_create_time")))
                    .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_DEF_MODEL_NAME + "_update_time")))
                    .build();
        }
    }
}
