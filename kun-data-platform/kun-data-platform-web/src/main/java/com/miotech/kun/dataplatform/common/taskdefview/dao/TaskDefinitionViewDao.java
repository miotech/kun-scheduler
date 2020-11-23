package com.miotech.kun.dataplatform.common.taskdefview.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.db.sql.*;
import com.miotech.kun.dataplatform.common.taskdefinition.dao.TaskDefinitionDao;
import com.miotech.kun.dataplatform.common.taskdefview.vo.TaskDefinitionViewSearchParams;
import com.miotech.kun.dataplatform.common.taskdefview.vo.ViewAndTaskDefinitionRelationVO;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("SqlResolve")
@Slf4j
@Repository
public class TaskDefinitionViewDao {
    public static final String TASK_DEF_VIEW_TABLE_NAME = "kun_dp_task_definition_view";

    private static final String TASK_DEF_VIEW_MODEL_NAME = "taskdef_view";

    public static final String VIEW_AND_TASK_DEF_RELATION_TABLE_NAME = "kun_dp_view_task_definition_relation";

    private static final String VIEW_AND_TASK_DEF_RELATION_MODEL_NAME = "view_taskdef_relation";

    private static final List<String> viewCols = ImmutableList.of("id", "name", "creator", "last_modifier", "create_time", "update_time");

    private static final List<String> viewTaskDefRelationCols = ImmutableList.of("view_id", "task_def_id");   //NOSONAR

    private final JdbcTemplate jdbcTemplate;

    private final TaskDefinitionDao taskDefinitionDao;

    @Autowired
    public TaskDefinitionViewDao(JdbcTemplate jdbcTemplate, TaskDefinitionDao taskDefinitionDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.taskDefinitionDao = taskDefinitionDao;
    }

    /**
     * Fetch a task definition view by its id.
     * @param taskDefViewId id of target task definition view
     * @return An optional object wrapper
     */
    public Optional<TaskDefinitionView> fetchById(Long taskDefViewId) {
        String sql = getViewSelectSQL(TASK_DEF_VIEW_MODEL_NAME + ".id = ?");
        List<TaskDefinitionView> resultViews = jdbcTemplate.query(
                sql,
                new TaskDefinitionViewMapper(this, taskDefinitionDao),
                taskDefViewId
        );
        return resultViews.stream().findAny();
    }

    public List<TaskDefinitionView> fetchAllByTaskDefinitionId(Long taskDefinitionId) {
        String subQuerySQL = "SELECT DISTINCT view_id FROM " + VIEW_AND_TASK_DEF_RELATION_TABLE_NAME + " WHERE task_def_id = ?";  //NOSONAR
        String sql = getViewSelectSQL(TASK_DEF_VIEW_MODEL_NAME + ".id IN (" + subQuerySQL + ")");
        return jdbcTemplate.query(
                sql,
                new TaskDefinitionViewMapper(this, taskDefinitionDao),
                taskDefinitionId
        );
    }

    /**
     * Search and fetch list of task definition view value objects
     * @param searchParams search parameters in a value object
     * @return result list
     */
    public List<TaskDefinitionView> fetchListBySearchParams(TaskDefinitionViewSearchParams searchParams) {
        Preconditions.checkNotNull(searchParams);
        Preconditions.checkArgument(Objects.nonNull(searchParams.getPageNum()) && (searchParams.getPageNum() > 0));
        Preconditions.checkArgument(Objects.nonNull(searchParams.getPageSize()) && (searchParams.getPageSize() >= 0) && (searchParams.getPageSize() <= 100));

        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_DEF_VIEW_MODEL_NAME, viewCols);
        WhereClause whereClause = buildWhereClauseFromSearchParams(searchParams);

        String sql = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_DEF_VIEW_TABLE_NAME, TASK_DEF_VIEW_MODEL_NAME)
                .where(whereClause.getSqlSegment())
                .offset(searchParams.getPageSize() * (searchParams.getPageNum() - 1))
                .limit(searchParams.getPageSize())
                .orderBy(buildOrderByString(searchParams))
                .autoAliasColumns()
                .getSQL();

        Object[] params = whereClause.getParams();

        return jdbcTemplate.query(
                sql,
                new TaskDefinitionViewMapper(this, taskDefinitionDao),
                params
        );
    }

    private WhereClause buildWhereClauseFromSearchParams(TaskDefinitionViewSearchParams searchParams) {
        boolean keywordFilterActive = StringUtils.isNotBlank(searchParams.getKeyword());
        List<Object> paramsList = new ArrayList<>();

        StringBuilder whereClauseBuilder = new StringBuilder();
        if (keywordFilterActive) {
            whereClauseBuilder.append("(" + TASK_DEF_VIEW_MODEL_NAME + ".name LIKE CONCAT('%', CAST(? AS TEXT), '%')) AND ");
            paramsList.add(searchParams.getKeyword().trim());
        }
        if (Objects.nonNull(searchParams.getCreator())) {
            whereClauseBuilder.append("(" + TASK_DEF_VIEW_MODEL_NAME + ".creator = ?) AND ");
            paramsList.add(searchParams.getCreator());
        }
        if (Objects.nonNull(searchParams.getTaskDefinitionIds()) && (!searchParams.getTaskDefinitionIds().isEmpty())) {
            List<Long> taskDefIds = searchParams.getTaskDefinitionIds();
            String subSelectSql = DefaultSQLBuilder.newBuilder()
                    .select("view_id")
                    .from(VIEW_AND_TASK_DEF_RELATION_TABLE_NAME)
                    .where("task_def_id IN (" + SQLUtils.generateSqlInClausePlaceholders(taskDefIds) + ")")
                    .getSQL();
            paramsList.addAll(taskDefIds);
            whereClauseBuilder.append("(" + TASK_DEF_VIEW_MODEL_NAME + ".id IN (" + subSelectSql + ")) AND ");
        }
        String whereClauseString = whereClauseBuilder.append("(1 = 1)").toString();
        return new WhereClause(whereClauseString, paramsList.toArray(new Object[0]));
    }

    private String buildOrderByString(TaskDefinitionViewSearchParams searchParams) {
        TaskDefinitionViewSearchParams.SortKey sortKey = searchParams.getSortKey();
        SortOrder sortOrder = searchParams.getSortOrder();
        if (Objects.isNull(sortOrder)) {
            sortOrder = SortOrder.DESC;
        }
        if (Objects.isNull(sortKey)) {
            sortKey = TaskDefinitionViewSearchParams.SortKey.ID;
        }
        return TASK_DEF_VIEW_MODEL_NAME + "_" + sortKey.getFieldColumnName() + " " + sortOrder.getSqlString();
    }

    /**
     * Create a task definition view
     * @param createView model object of the view to create
     * @return created view model object
     * @throws IllegalArgumentException if view id already used
     * @throws IllegalStateException if create failed
     */
    @Transactional
    public TaskDefinitionView create(TaskDefinitionView createView) {
        Preconditions.checkNotNull(createView);
        if (fetchById(createView.getId()).isPresent()) {
            throw new IllegalArgumentException(
                    String.format("Cannot create task definition view with already existed id = %s", createView.getId())
            );
        }

        log.debug("Creating new task definition view with id: {}; name: {}; creator id = {}",
                createView.getId(), createView.getName(), createView.getCreator());
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(viewCols.toArray(new String[0]))
                .into(TASK_DEF_VIEW_TABLE_NAME)
                .autoAliasColumns()
                .asPrepared()
                .getSQL();
        OffsetDateTime currentTime = DateTimeUtils.now();
        Object[] params = {
                createView.getId(),
                createView.getName(),
                createView.getCreator(),
                createView.getCreator(),   // last_modifier
                currentTime,                 // create_at
                currentTime,                 // update_at
        };
        jdbcTemplate.update(sql, params);
        // insert task definition relations
        updateAllRelationsByViewId(createView.getId(), createView
                .getIncludedTaskDefinitions().stream()
                .map(TaskDefinition::getDefinitionId)
                .collect(Collectors.toList())
        );

        return fetchById(createView.getId()).orElseThrow(IllegalStateException::new);
    }

    /**
     * Update a task definition view
     * @param updateView task definition view model to update
     * @return affected rows
     * @throws NullPointerException if view is null or its id is null
     */
    @Transactional
    public int update(TaskDefinitionView updateView) {
        Preconditions.checkNotNull(updateView);
        Preconditions.checkNotNull(updateView.getId(), "Invalid view model with id = null");

        log.debug("Updating task definition view with id = {}", updateView.getId());

        String sql = DefaultSQLBuilder.newBuilder()
                .update(TASK_DEF_VIEW_TABLE_NAME)
                .set(viewCols.subList(1, viewCols.size()).toArray(new String[0]))
                .where("id = ?")
                .asPrepared()
                .getSQL();
        int affectedRows = jdbcTemplate.update(
                sql,
                updateView.getName(),
                updateView.getCreator(),
                updateView.getLastModifier(),
                updateView.getCreateTime(),
                DateTimeUtils.now(),
                updateView.getId()
        );
        if (affectedRows > 0) {
            updateAllRelationsByViewId(
                    updateView.getId(),
                    updateView.getIncludedTaskDefinitions().stream()
                            .map(TaskDefinition::getDefinitionId)
                            .collect(Collectors.toList())
            );
        }
        return affectedRows;
    }

    private void updateAllRelationsByViewId(Long viewId, List<Long> taskDefinitionIds) {
        Preconditions.checkNotNull(viewId);
        Preconditions.checkNotNull(taskDefinitionIds);

        // Remove old inclusion relations
        removeAllInclusiveTaskDefinitionsByViewId(viewId);

        // Insert updated inclusion relations
        String insertionSQL = DefaultSQLBuilder.newBuilder()
                .insert(viewTaskDefRelationCols.toArray(new String[0]))
                .into(VIEW_AND_TASK_DEF_RELATION_TABLE_NAME)
                .autoAliasColumns()
                .asPrepared()
                .getSQL();

        List<Object[]> batchParams = taskDefinitionIds.stream()
                .map(taskDefId -> new Object[]{viewId, taskDefId})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(insertionSQL, batchParams);
    }

    /**
     * Delete a task definition view by id
     * @param viewId task definition view id
     * @return true if found and removed successfully. false if target view not found.
     */
    @Transactional
    public boolean deleteById(Long viewId) {
        Optional<TaskDefinitionView> taskDefinitionViewOptional = fetchById(viewId);
        if (!taskDefinitionViewOptional.isPresent()) {
            return false;
        }
        // Remove inclusion relations of target view
        removeAllInclusiveTaskDefinitionsByViewId(viewId);
        // Remove view
        String deleteViewSQL = "DELETE FROM " + TASK_DEF_VIEW_TABLE_NAME + " WHERE id = ?";   //NOSONAR
        jdbcTemplate.update(deleteViewSQL, viewId);
        return true;
    }

    /**
     * Fetch total count of task definition view by given search parameter
     * @return total count number
     */
    public Integer fetchTotalCount(TaskDefinitionViewSearchParams searchParams) {
        String sql;
        WhereClause whereClause = buildWhereClauseFromSearchParams(searchParams);
        sql = DefaultSQLBuilder.newBuilder()
                .select("COUNT(*) AS total")
                .from(TASK_DEF_VIEW_TABLE_NAME, TASK_DEF_VIEW_MODEL_NAME)
                .where(whereClause.getSqlSegment())
                .asPrepared()
                .getSQL();
        List<Integer> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt("total"),
                whereClause.getParams()
        );
        return result.stream().findFirst().orElseThrow(IllegalStateException::new);
    }

    private void removeAllInclusiveTaskDefinitionsByViewId(Long viewId) {
        Preconditions.checkNotNull(viewId);
        String deleteRelationSQL = "DELETE FROM " + VIEW_AND_TASK_DEF_RELATION_TABLE_NAME + " WHERE view_id = ?";
        jdbcTemplate.update(deleteRelationSQL, viewId);
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
     * Provide id of a specific view, return ids of task definitions it contains.
     * @param viewId id of target task definition view
     * @return list of ids of the view's related task definitions
     */
    protected List<Long> fetchInclusiveTaskDefinitionIdsByViewId(Long viewId) {
        String whereClause = VIEW_AND_TASK_DEF_RELATION_MODEL_NAME + ".view_id = ?";
        String sql = getViewTaskDefRelationSelectSQL(whereClause);
        List<ViewAndTaskDefinitionRelationVO> results =
                jdbcTemplate.query(sql, ViewAndTaskDefinitionRelationVOMapper.INSTANCE, viewId);
        return results.stream()
                .map(ViewAndTaskDefinitionRelationVO::getTaskDefinitionId)
                .collect(Collectors.toList());
    }

    /**
     * Remove all inclusion relation of target definition view with all views.
     * Usually invokes when a task definition being removed.
     * @param taskDefinitionId id of target task definition
     * @return affected task definition view ids
     */
    @Transactional
    public Set<Long> removeAllRelationsOfTaskDefinition(Long taskDefinitionId) {
        String detectAffectedViewIdsSql = "SELECT DISTINCT view_id FROM " + VIEW_AND_TASK_DEF_RELATION_TABLE_NAME + " WHERE task_def_id = ?";
        List<Long> affectedViewIds = jdbcTemplate.query(detectAffectedViewIdsSql, (rs, rowNum) -> rs.getLong("view_id"), taskDefinitionId);
        log.debug("Removing task definition and view relation for task definition id = {}; Affected view ids = {};", taskDefinitionId, affectedViewIds);

        // Do deletion
        String sql = "DELETE FROM " + VIEW_AND_TASK_DEF_RELATION_TABLE_NAME + " WHERE task_def_id = ?";
        jdbcTemplate.update(sql, taskDefinitionId);
        return new HashSet<>(affectedViewIds);
    }

    public static class TaskDefinitionViewMapper implements RowMapper<TaskDefinitionView> {
        private final TaskDefinitionViewDao taskDefinitionViewDao;

        private final TaskDefinitionDao taskDefinitionDao;

        public TaskDefinitionViewMapper(TaskDefinitionViewDao taskDefinitionViewDao, TaskDefinitionDao taskDefinitionDao) {
            this.taskDefinitionViewDao = taskDefinitionViewDao;
            this.taskDefinitionDao = taskDefinitionDao;
        }

        @Override
        public TaskDefinitionView mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long viewId = rs.getLong(TASK_DEF_VIEW_MODEL_NAME + "_id");
            TaskDefinitionView.TaskDefinitionViewBuilder builder = TaskDefinitionView.newBuilder()
                    .withId(viewId)
                    .withName(rs.getString(TASK_DEF_VIEW_MODEL_NAME + "_name"))
                    .withCreator(rs.getLong(TASK_DEF_VIEW_MODEL_NAME + "_creator"))
                    .withLastModifier(rs.getLong(TASK_DEF_VIEW_MODEL_NAME + "_last_modifier"))
                    .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_DEF_VIEW_MODEL_NAME + "_create_time")))
                    .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_DEF_VIEW_MODEL_NAME + "_update_time")));

            // Fetch task definitions in detail
            List<Long> includedTaskDefinitionIds = taskDefinitionViewDao.fetchInclusiveTaskDefinitionIdsByViewId(viewId);
            List<TaskDefinition> includedTaskDefinitions = taskDefinitionDao.fetchByIds(includedTaskDefinitionIds);
            builder.withIncludedTaskDefinitions(includedTaskDefinitions);

            return builder.build();
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
