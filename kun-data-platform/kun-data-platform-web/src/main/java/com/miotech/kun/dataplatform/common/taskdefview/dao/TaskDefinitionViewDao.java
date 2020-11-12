package com.miotech.kun.dataplatform.common.taskdefview.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewSearchParams;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewVO;
import com.miotech.kun.dataplatform.common.taskdefview.vo.ViewAndTaskDefinitionRelationVO;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;

@Repository
public class TaskDefinitionViewDao {
    private static final String TASK_DEF_VIEW_TABLE_NAME = "kun_dp_task_definition_view";

    private static final String TASK_DEF_VIEW_MODEL_NAME = "taskdef_view";

    private static final String VIEW_AND_TASK_DEF_RELATION_TABLE_NAME = "kun_dp_view_task_definition_relation";

    private static final String VIEW_AND_TASK_DEF_RELATION_MODEL_NAME = "view_taskdef_relation";

    private static final List<String> viewCols = ImmutableList.of("id", "name", "creator", "last_modifier", "created_at", "updated_at");

    private static final List<String> viewTaskDefRelationCols = ImmutableList.of("view_id", "task_def_id", "creator", "created_at");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Fetch a task definition view by its id.
     * @param taskDefViewId id of target task definition view
     * @return An optional object wrapper
     */
    public Optional<TaskDefinitionView> fetchById(Long taskDefViewId) {
        String sql = getViewSelectSQL(TASK_DEF_VIEW_MODEL_NAME + ".id = ?");
        List<TaskDefinitionView> resultViews = jdbcTemplate.query(sql, TaskDefinitionViewMapper.INSTANCE, taskDefViewId);
        return resultViews.stream().findAny();
    }

    public List<TaskDefinitionView> fetchByIds(List<Long> taskDefIds) {
    }

    public List<TaskDefinitionViewVO> fetchListBySearchParams(TaskDefinitionViewSearchParams searchParams) {
        Preconditions.checkNotNull(searchParams);

        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_DEF_VIEW_MODEL_NAME, viewCols);

        boolean keywordFilterActive = StringUtils.isNotBlank(searchParams.getKeyword());

        String sql = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_DEF_VIEW_TABLE_NAME, TASK_DEF_VIEW_MODEL_NAME)
                .where(keywordFilterActive ? TASK_DEF_VIEW_MODEL_NAME + ".name LIKE (? AS TEXT)" : "1 = 1")
                .offset(searchParams.getPageSize() * (searchParams.getPageNum() - 1))
                .limit(searchParams.getPageSize())
                .asPrepared()
                .toString();

        List<Object> params = new ArrayList<>();
        if (keywordFilterActive) {
            params.add(searchParams.getKeyword().trim());
        }

        // TODO: complete this
        List<TaskDefinitionViewVO> results = jdbcTemplate.query(sql, null, params.toArray());
    }

    /**
     * Create a task definition view
     * @param taskDefinitionView view model object
     * @return true if created an object, false if the object with the same name or id already exists
     */
    public boolean create(TaskDefinitionView taskDefinitionView) {
        Preconditions.checkNotNull(taskDefinitionView);
        return this.create(TaskDefinitionViewVO.from(taskDefinitionView));
    }

    public boolean create(TaskDefinitionViewVO taskDefinitionViewVO) {
        Preconditions.checkNotNull(taskDefinitionViewVO);
        Optional<TaskDefinitionView> viewOptional = fetchById(taskDefinitionViewVO.getId());
        if (viewOptional.isPresent()) {
            return false;
        }
        // TODO: check name duplication
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(viewCols.toArray(new String[0]))
                .into(TASK_DEF_VIEW_TABLE_NAME)
                .autoAliasColumns()
                .asPrepared()
                .getSQL();
        OffsetDateTime currentTime = DateTimeUtils.now();
        Object[] params = {
                taskDefinitionViewVO.getId(),
                taskDefinitionViewVO.getName(),
                taskDefinitionViewVO.getCreator(),
                taskDefinitionViewVO.getLastModifier(),
                currentTime,
                currentTime,
        };
        jdbcTemplate.update(sql, params);
        return true;
    }

    private String getViewSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_DEF_VIEW_MODEL_NAME, viewCols);
        SQLBuilder builder =  DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_DEF_VIEW_TABLE_NAME, TASK_DEF_VIEW_MODEL_NAME)
                .autoAliasColumns();

        if (StringUtils.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }
        return builder.getSQL();
    }

    private String getViewTaskDefRelationSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(VIEW_AND_TASK_DEF_RELATION_MODEL_NAME, viewTaskDefRelationCols);
        SQLBuilder builder =  DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(VIEW_AND_TASK_DEF_RELATION_TABLE_NAME, VIEW_AND_TASK_DEF_RELATION_MODEL_NAME)
                .autoAliasColumns();

        if (StringUtils.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }
        return builder.getSQL();
    }

    /**
     * Provide a set of view ids as input, returns related task definition ids in a hashmap
     * @param viewIds set of view ids to query
     * @return a hashmap where view ids are keys and corresponding set of related task definition ids as values
     */
    private Map<Long, Set<Long>> loadTaskDefIdMappingsForViews(Set<Long> viewIds) {
        String whereClause = String.format(
                VIEW_AND_TASK_DEF_RELATION_MODEL_NAME + ".view_id in (%s)",
                com.miotech.kun.commons.utils.StringUtils.repeatJoin("?", ",", viewIds.size())
        );
        String sql = getViewTaskDefRelationSelectSQL(whereClause);
        List<ViewAndTaskDefinitionRelationVO> results = jdbcTemplate.query(
                sql,
                ViewAndTaskDefinitionRelationVOMapper.INSTANCE,
                viewIds.toArray()
        );
        Map<Long, Set<Long>> resultMap = new HashMap<>();
        for (ViewAndTaskDefinitionRelationVO relationVO : results) {
            if (!resultMap.containsKey(relationVO.getViewId())) {
                resultMap.put(relationVO.getViewId(), Sets.newHashSet(relationVO.getTaskDefinitionId()));
            } else {
                resultMap.get(relationVO.getViewId()).add(relationVO.getTaskDefinitionId());
            }
        }
        return resultMap;
    }

    public static class TaskDefinitionViewMapper implements RowMapper<TaskDefinitionView> {
        public static final TaskDefinitionViewMapper INSTANCE = new TaskDefinitionViewMapper();

        @Override
        public TaskDefinitionView mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TaskDefinitionView.newBuilder()
                    .withId(rs.getLong(TASK_DEF_VIEW_MODEL_NAME + "_id"))
                    .withName(rs.getString(TASK_DEF_VIEW_MODEL_NAME + "_name"))
                    .withCreator(rs.getLong(TASK_DEF_VIEW_MODEL_NAME + "_creator"))
                    .withLastModifier(rs.getLong(TASK_DEF_VIEW_MODEL_NAME + "_last_modifier"))
                    .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_DEF_VIEW_MODEL_NAME + "_create_time")))
                    .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_DEF_VIEW_MODEL_NAME + "_update_time")))
                    // TODO: fill task definition list field
                    .withIncludedTaskDefinitions(new ArrayList<>())
                    .build();
        }
    }

    public static class TaskDefinitionViewVOMapper implements RowMapper<TaskDefinitionView> {
        public static final TaskDefinitionViewVOMapper INSTANCE = new TaskDefinitionViewVOMapper();

        @Override
        public TaskDefinitionView mapRow(ResultSet rs, int rowNum) throws SQLException {
            // TODO: complete this
        }
    }

    public static class ViewAndTaskDefinitionRelationVOMapper implements RowMapper<ViewAndTaskDefinitionRelationVO> {
        public static final ViewAndTaskDefinitionRelationVOMapper INSTANCE = new ViewAndTaskDefinitionRelationVOMapper();

        @Override
        public ViewAndTaskDefinitionRelationVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long viewId = rs.getLong(VIEW_AND_TASK_DEF_RELATION_MODEL_NAME + "_view_id");
            Long taskDefId = rs.getLong(VIEW_AND_TASK_DEF_RELATION_MODEL_NAME + "_task_def_id");
            return new ViewAndTaskDefinitionRelationVO(viewId, taskDefId);
        }
    }
}
