package com.miotech.kun.dataplatform.common.tasktemplate.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.dataplatform.model.tasktemplate.ParameterDefinition;
import com.miotech.kun.dataplatform.model.tasktemplate.TaskTemplate;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TaskTemplateDaoImpl implements TaskTemplateDao {
    private final static Logger logger = LoggerFactory.getLogger(TaskTemplateDaoImpl.class);

    private static final String TASK_TEMPLATE_TABLE_NAME = "kun_dp_task_template";

    private static final String TASK_TEMPLATE_MODEL_NAME = "tasktemplate";

    private static final List<String> taskTemplateCols = ImmutableList.of("name", "template_type", "template_group", "operator_id", "default_values", "display_parameters", "renderer_class");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WorkflowClient workflowClient;

    @Override
    public List<TaskTemplate> getTaskTemplates() {
        String sql = getSelectSQL(null);
        List<TaskTemplate> taskTemplates = jdbcTemplate.query(sql, TaskTemplateMapper.INSTANCE);
        return taskTemplates.stream()
                .map(this::enrichWithOperator)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TaskTemplate> fetchByName(String name) {
        Preconditions.checkNotNull(name, "Template name should not be null");
        String sql = getSelectSQL(TASK_TEMPLATE_MODEL_NAME + ".name = ?");
        List<TaskTemplate> taskTemplates = jdbcTemplate.query(sql, TaskTemplateMapper.INSTANCE, name);
        return taskTemplates.stream()
                .findAny()
                .map(this::enrichWithOperator);
    }

    @Override
    public TaskTemplate create(TaskTemplate template) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(taskTemplateCols.toArray(new String[0]))
                .into(TASK_TEMPLATE_TABLE_NAME)
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(
                sql,
                template.getName(),
                template.getTemplateType(),
                template.getTemplateGroup(),
                template.getOperator().getId(),
                JSONUtils.toJsonString(template.getDefaultValues()),
                JSONUtils.toJsonString(template.getDisplayParameters()),
                template.getRenderClassName()
        );
        return template;
    }

    @Override
    public TaskTemplate update(TaskTemplate template) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(TASK_TEMPLATE_TABLE_NAME)
                .set(taskTemplateCols.toArray(new String[0]))
                .where("name = ?")
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(
                sql,
                template.getName(),
                template.getTemplateType(),
                template.getTemplateGroup(),
                template.getOperator().getId(),
                JSONUtils.toJsonString(template.getDefaultValues()),
                JSONUtils.toJsonString(template.getDisplayParameters()),
                template.getRenderClassName(),
                template.getName()
        );
        return template;
    }

    private String getSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_TEMPLATE_MODEL_NAME, taskTemplateCols);
        SQLBuilder builder = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_TEMPLATE_TABLE_NAME, TASK_TEMPLATE_MODEL_NAME)
                .autoAliasColumns();

        if (StringUtils.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }

        return builder.getSQL();
    }

    private TaskTemplate enrichWithOperator(TaskTemplate template) {
        Operator op = workflowClient.getOperator(template.getOperator().getId());
        return template.cloneBuilder()
                .withOperator(op)
                .build();
    }

    public static class TaskTemplateMapper implements RowMapper<TaskTemplate> {
        public static final TaskTemplateMapper INSTANCE = new TaskTemplateMapper();

        @Override
        public TaskTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long operatorId = rs.getLong(TASK_TEMPLATE_MODEL_NAME + "_operator_id");
            Operator operator = Operator.newBuilder()
                    .withId(operatorId)
                    .build();
            return TaskTemplate.newBuilder()
                    .withName(rs.getString(TASK_TEMPLATE_MODEL_NAME + "_name"))
                    .withTemplateType(rs.getString(TASK_TEMPLATE_MODEL_NAME + "_template_type"))
                    .withTemplateGroup(rs.getString(TASK_TEMPLATE_MODEL_NAME + "_template_group"))
                    .withOperator(operator)
                    .withDefaultValues(JSONUtils.jsonToObject(rs.getString(TASK_TEMPLATE_MODEL_NAME + "_default_values"),
                            new TypeReference<Map<String, Object>>() {}))
                    .withDisplayParameters(JSONUtils.jsonToObject(rs.getString(TASK_TEMPLATE_MODEL_NAME + "_display_parameters"),
                            new TypeReference<List<ParameterDefinition>>() {}))
                    .withRenderClassName(rs.getString(TASK_TEMPLATE_MODEL_NAME + "_renderer_class"))
                    .build();
        }
    }
}
