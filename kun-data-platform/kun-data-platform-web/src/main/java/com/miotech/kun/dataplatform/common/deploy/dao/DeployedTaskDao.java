package com.miotech.kun.dataplatform.common.deploy.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.dataplatform.common.commit.dao.TaskCommitDao;
import com.miotech.kun.dataplatform.common.deploy.vo.DeployedTaskSearchRequest;
import com.miotech.kun.dataplatform.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.model.deploy.DeployedTask;
import com.miotech.kun.workflow.client.model.PaginationResult;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.miotech.kun.dataplatform.common.commit.dao.TaskCommitDao.*;

@Repository
public class DeployedTaskDao {

    public static final String DEPLOYED_TASK_TABLE_NAME = "kun_dp_deployed_task";

    private static final String DEPLOYED_TASK_MODEL_NAME = "deployedtask";

    private static final List<String> deployedTaskCols = ImmutableList.of("id", "definition_id", "name", "task_template_name", "wf_task_id", "owner", "commit_id", "is_archived");

    private static final String DEFINITION_ID = "definition_id";

    private static final String WORKFLOW_TASK_ID = "wf_task_id";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String getSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(DEPLOYED_TASK_MODEL_NAME, deployedTaskCols);
        columnsMap.put(TASK_COMMIT_MODEL_NAME, taskCommitCols);

        SQLBuilder builder = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(DEPLOYED_TASK_TABLE_NAME, DEPLOYED_TASK_MODEL_NAME)
                .join("INNER",TASK_COMMIT_TABLE_NAME, TASK_COMMIT_MODEL_NAME)
                .on(String.format("%s.commit_id = %s.id", DEPLOYED_TASK_MODEL_NAME, TASK_COMMIT_MODEL_NAME))
                .autoAliasColumns();

        if (StringUtils.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }

        return builder.getSQL();
    }

    public Optional<DeployedTask> fetchById(Long definitionId) {
        String sql = getSelectSQL(String.format(" %s.%s= ?", DEPLOYED_TASK_MODEL_NAME, DEFINITION_ID));
        List<DeployedTask> deployedTasks = jdbcTemplate.query(sql, DeployedTaskMapper.INSTANCE, definitionId);
        return deployedTasks.stream().findAny();
    }

    public List<DeployedTask> fetchByIds(List<Long> definitionIds) {
        String idInClause = String.format("%s.%s in (%s)", DEPLOYED_TASK_MODEL_NAME, DEFINITION_ID,
                com.miotech.kun.commons.utils.StringUtils.repeatJoin("?", ",", definitionIds.size()));
        String sql = getSelectSQL(idInClause);
        return jdbcTemplate.query(sql, DeployedTaskMapper.INSTANCE, definitionIds.toArray());
    }

    public List<Long> fetchWorkflowTaskId(List<Long> definitionIds) {
        Preconditions.checkNotNull(definitionIds);
        if (definitionIds.isEmpty()) return ImmutableList.of();
        String filter = String.format("%s in (%s)", DEFINITION_ID, com.miotech.kun.commons.utils.StringUtils
                .repeatJoin("?", ",", definitionIds.size()));
        String sql = DefaultSQLBuilder.newBuilder()
                .select("wf_task_id")
                .from(DEPLOYED_TASK_TABLE_NAME)
                .where(filter)
                .asPrepared()
                .getSQL();

        return jdbcTemplate.queryForList(sql, definitionIds.toArray(), Long.class);
    }

    public Optional<DeployedTask> fetchByWorkflowTaskId(Long workflowTaskId) {
        String sql = getSelectSQL(String.format(" %s.%s= ?", DEPLOYED_TASK_MODEL_NAME, WORKFLOW_TASK_ID));
        List<DeployedTask> deployedTasks = jdbcTemplate.query(sql, DeployedTaskMapper.INSTANCE, workflowTaskId);
        return deployedTasks.stream().findAny();
    }


    public void create(DeployedTask deployedTask) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(deployedTaskCols.toArray(new String[0]))
                .into(DEPLOYED_TASK_TABLE_NAME)
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(
                sql,
                deployedTask.getId(),
                deployedTask.getDefinitionId(),
                deployedTask.getName(),
                deployedTask.getTaskTemplateName(),
                deployedTask.getWorkflowTaskId(),
                deployedTask.getOwner(),
                deployedTask.getTaskCommit().getId(),
                deployedTask.isArchived()
        );
    }

    public PaginationResult<DeployedTask> search(DeployedTaskSearchRequest searchRequest) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" 1 = 1");
        List<Object> params = new ArrayList();
        List<Long> ownerIds = searchRequest.getOwnerIds();
        if (!ownerIds.isEmpty()) {
            whereClause.append(" AND ");
            whereClause.append(String.format(DEPLOYED_TASK_MODEL_NAME + ".owner in (%s)", StringUtils.repeat("?", ",", ownerIds.size())));
            params.addAll(ownerIds);
        }

        List<Long> definitionIds = searchRequest.getDefinitionIds();
        if (!definitionIds.isEmpty()) {
            whereClause.append(" AND ");
            whereClause.append(String.format( "%s.%s in (%s)", DEPLOYED_TASK_MODEL_NAME, DEFINITION_ID, StringUtils.repeat("?", ",", definitionIds.size())));
            params.addAll(definitionIds);
        }

        if (StringUtils.isNoneBlank(searchRequest.getName())) {
            whereClause.append(" AND ");
            whereClause.append(DEPLOYED_TASK_MODEL_NAME + ".name LIKE CONCAT('%', CAST(? AS TEXT) , '%')");
            params.add(searchRequest.getName());
        }

        if (StringUtils.isNoneBlank(searchRequest.getTaskTemplateName())) {
            whereClause.append(" AND ");
            whereClause.append(DEPLOYED_TASK_MODEL_NAME + ".task_template_name = ?");
            params.add(searchRequest.getTaskTemplateName());
        }

        List<Long> workflowTaskIds = searchRequest.getWorkflowTaskIds();
        if (Objects.nonNull(workflowTaskIds) && (!workflowTaskIds.isEmpty())) {
            whereClause.append(" AND ");
            whereClause.append(String.format( "%s.%s in (%s)", DEPLOYED_TASK_MODEL_NAME, WORKFLOW_TASK_ID, StringUtils.repeat("?", ",", workflowTaskIds.size())));
            params.addAll(workflowTaskIds);
        }

        String countSql = DefaultSQLBuilder.newBuilder()
                .select("COUNT(*)")
                .from(DEPLOYED_TASK_TABLE_NAME, DEPLOYED_TASK_MODEL_NAME)
                .where(whereClause.toString())
                .getSQL();
        // count
        Integer totalCount = jdbcTemplate.query(
                countSql,
                (rse) -> rse.next() ? rse.getInt(1): 0,
                params.toArray());
        String sql = DefaultSQLBuilder.newBuilder()
                .select(getSelectSQL(whereClause.toString()))
                .orderBy(DEPLOYED_TASK_MODEL_NAME + ".id DESC")
                .limit(searchRequest.getPageSize())
                .offset(searchRequest.getPageSize() * (searchRequest.getPageNum() - 1))
                .getSQL();
        // list
        List<DeployedTask> tasks = jdbcTemplate.query(sql, DeployedTaskMapper.INSTANCE, params.toArray());

        return new PaginationResult<>(
                searchRequest.getPageSize(),
                searchRequest.getPageNum(),
                totalCount,
                tasks
        );
    }

    public void update(DeployedTask deployedTask) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(DEPLOYED_TASK_TABLE_NAME)
                .set("name", "wf_task_id", "owner", "commit_id", "is_archived")
                .asPrepared()
                .where(DEFINITION_ID + "  = ?")
                .getSQL();
        jdbcTemplate.update(
                sql,
                deployedTask.getName(),
                deployedTask.getWorkflowTaskId(),
                deployedTask.getOwner(),
                deployedTask.getTaskCommit().getId(),
                deployedTask.isArchived(),
                deployedTask.getDefinitionId()
        );
    }

    public static class DeployedTaskMapper implements RowMapper<DeployedTask> {
        public static final DeployedTaskMapper INSTANCE = new DeployedTaskMapper();

        private static final TaskCommitDao.TaskCommitMapper taskCommitMapper = TaskCommitDao.TaskCommitMapper.INSTANCE;

        @Override
        public DeployedTask mapRow(ResultSet rs, int rowNum) throws SQLException {
            TaskCommit commit = taskCommitMapper.mapRow(rs, rowNum);
            return DeployedTask.newBuilder()
                    .withId(rs.getLong(DEPLOYED_TASK_MODEL_NAME + "_id"))
                    .withName(rs.getString(DEPLOYED_TASK_MODEL_NAME + "_name"))
                    .withDefinitionId(rs.getLong(DEPLOYED_TASK_MODEL_NAME + "_definition_id"))
                    .withTaskTemplateName(rs.getString(DEPLOYED_TASK_MODEL_NAME + "_task_template_name"))
                    .withWorkflowTaskId(rs.getLong(DEPLOYED_TASK_MODEL_NAME + "_wf_task_id"))
                    .withOwner(rs.getLong(DEPLOYED_TASK_MODEL_NAME + "_owner"))
                    .withTaskCommit(commit)
                    .withArchived(rs.getBoolean(DEPLOYED_TASK_MODEL_NAME + "_is_archived"))
                    .build();
        }
    }
}
