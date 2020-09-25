package com.miotech.kun.dataplatform.common.commit.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.commons.utils.StringUtils;
import com.miotech.kun.dataplatform.common.commit.vo.CommitSearchRequest;
import com.miotech.kun.dataplatform.common.deploy.dao.DeployDao;
import com.miotech.kun.dataplatform.common.utils.VersionUtil;
import com.miotech.kun.dataplatform.model.commit.CommitStatus;
import com.miotech.kun.dataplatform.model.commit.CommitType;
import com.miotech.kun.dataplatform.model.commit.TaskCommit;
import com.miotech.kun.dataplatform.model.commit.TaskSnapshot;
import com.miotech.kun.dataplatform.model.deploy.DeployStatus;
import com.miotech.kun.workflow.client.model.PaginationResult;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class TaskCommitDao {
    public static final String TASK_COMMIT_TABLE_NAME = "kun_dp_task_commit";

    public static final String TASK_COMMIT_MODEL_NAME = "taskcommit";

    public static final List<String> taskCommitCols = ImmutableList.of("id", "task_def_id", "version", "message", "snapshot", "committer", "committed_at", "commit_type", "commit_status", "is_latest");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String getSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(TASK_COMMIT_MODEL_NAME, taskCommitCols);
        SQLBuilder builder =  DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(TASK_COMMIT_TABLE_NAME, TASK_COMMIT_MODEL_NAME)
                .autoAliasColumns();

        if (Strings.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }

        return builder.getSQL();
    }

    public Optional<DeployStatus> fetchCommitStatus(Long commitId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("deploy_status")
                .from(DeployDao.DEPLOY_COMMIT_TABLE_NAME)
                .where("commit_id = ?")
                .asPrepared()
                .getSQL();

        DeployStatus deployStatus = jdbcTemplate.query(sql, new Object[]{commitId},  (rse) -> {
            return rse.next() ? DeployStatus.valueOf(rse.getString(1)): null;
        });
        return Optional.ofNullable(deployStatus);
    }

    /**
     * check whether latest commit is deployed
     * @param definitionIds
     * @return
     */
    public Map<Long, Boolean> getLatestCommitStatus(List<Long> definitionIds) {
        Map<Long, Boolean> commitStatus = new HashMap<>();
        definitionIds.forEach(x -> commitStatus.put(x, false));

        if (CollectionUtils.isEmpty(definitionIds)) {
            return commitStatus;
        }
        SQLBuilder builder = DefaultSQLBuilder.newBuilder()
                .select(TASK_COMMIT_MODEL_NAME+ ".task_def_id", DeployDao.DEPLOY_COMMIT_MODEL_NAME + ".deploy_status")
                .from(TASK_COMMIT_TABLE_NAME, TASK_COMMIT_MODEL_NAME)
                .join("LEFT", DeployDao.DEPLOY_COMMIT_TABLE_NAME, DeployDao.DEPLOY_COMMIT_MODEL_NAME)
                .on(String.format("%s.id = %s.commit_id", TASK_COMMIT_MODEL_NAME,  DeployDao.DEPLOY_COMMIT_MODEL_NAME))
                .where(String.format("%s.task_def_id in (%s) AND %s.is_latest = ? AND %s.deploy_status = ?",
                        TASK_COMMIT_MODEL_NAME,
                        StringUtils.repeatJoin("?", ",", definitionIds.size()),
                        TASK_COMMIT_MODEL_NAME,
                        DeployDao.DEPLOY_COMMIT_MODEL_NAME ))
                .asPrepared()
                .autoAliasColumns();
        List<Object> params = new ArrayList(definitionIds);
        params.add(true);
        params.add(DeployStatus.SUCCESS.name());
        jdbcTemplate.query(builder.getSQL(), params.toArray(), (rs)-> {
            commitStatus.put(rs.getLong(1), rs.getString(2) != null);
        } );
        return commitStatus;
    }

    public Optional<TaskCommit> fetchById(Long id) {
        String sql = getSelectSQL(TASK_COMMIT_MODEL_NAME + ".id = ?");
        List<TaskCommit> taskCommits = jdbcTemplate.query(sql, TaskCommitMapper.INSTANCE, id);
        return taskCommits.stream().findAny().map ( x -> {
            Optional<DeployStatus> commitDeployStatus =  fetchCommitStatus(x.getId());
            return x.cloneBuilder()
                    .withCommitStatus(resolveCommitStatus(x, commitDeployStatus))
                    .build();
        });
    }

    public List<TaskCommit> fetchByIds(List<Long> ids) {
        Preconditions.checkNotNull(ids);
        if (ids.isEmpty()) return ImmutableList.of();
        String sql = getSelectSQL(TASK_COMMIT_MODEL_NAME + String.format(".id in (%s)", StringUtils.repeatJoin("?", ",", ids.size())));
        return jdbcTemplate.query(sql, TaskCommitMapper.INSTANCE, ids.toArray());
    }

    public PaginationResult<TaskCommit> search(CommitSearchRequest searchRequest) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" 1 = 1");
        List<Object> params = new ArrayList();
        List<Long> commitIds = searchRequest.getCommiterIds();
        if (!commitIds.isEmpty()) {
            whereClause.append(" AND ");
            whereClause.append(String.format(TASK_COMMIT_MODEL_NAME + ".id in (%s)", StringUtils.repeatJoin("?", ",", commitIds.size())));
            params.addAll(commitIds);
        }

        List<Long> definitionIds = searchRequest.getDefinitionIds();
        if (!definitionIds.isEmpty()) {
            whereClause.append(" AND ");
            whereClause.append(String.format(TASK_COMMIT_MODEL_NAME + ".task_def_id in (%s)", StringUtils.repeatJoin("?", ",", definitionIds.size())));
            params.addAll(definitionIds);
        }

        Optional<Boolean> isLatest = searchRequest.getIsLatest();
        if (isLatest.isPresent()) {
            whereClause.append(" AND ");
            whereClause.append(TASK_COMMIT_MODEL_NAME + ".is_latest = ? ");
            params.add(isLatest.get());
        }

        String countSql = DefaultSQLBuilder.newBuilder()
                .select("COUNT(1)")
                .from(TASK_COMMIT_TABLE_NAME, TASK_COMMIT_MODEL_NAME)
                .where(whereClause.toString())
                .getSQL();
        Long totalCount = jdbcTemplate.query(
                countSql,
                (rse) -> rse.next() ? rse.getLong(1): 0,
                params.toArray());
        String sql = DefaultSQLBuilder.newBuilder()
                .select(getSelectSQL(whereClause.toString()))
                .orderBy("id")
                .limit(searchRequest.getPageSize())
                .offset(searchRequest.getPageSize() * (searchRequest.getPageNum()-1))
                .getSQL();
        List<TaskCommit> taskCommits = jdbcTemplate.query(sql, TaskCommitMapper.INSTANCE, params.toArray());
        return new PaginationResult<>(
                searchRequest.getPageSize(),
                searchRequest.getPageNum(),
                totalCount,
                taskCommits);
    }

    private CommitStatus resolveCommitStatus(TaskCommit taskCommit, Optional<DeployStatus> deployStatusOptional) {
        if (deployStatusOptional.isPresent()) {
            return CommitStatus.DEPLOYED;
        } else if (!taskCommit.isLatestCommit()) {
            return CommitStatus.MIDDLE_VERSIONED;
        }
        return CommitStatus.SUBMITTED;
    }

    public Optional<VersionProps> fetchLatestVersionByTask(Long taskId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("id", "version", "task_def_id")
                .from(TASK_COMMIT_TABLE_NAME)
                .where("task_def_id = ? and is_latest = ?")
                .asPrepared()
                .getSQL();

        VersionProps versionProps = jdbcTemplate.query(sql, new Object[]{taskId, true}, (rse) -> {
            return rse.next() ? new VersionProps(rse.getLong(1), rse.getInt(2), rse.getLong(3)): null;
        });
        return Optional.ofNullable(versionProps);
    }

    public TaskCommit create(TaskCommit taskCommit) {

        // check latest version
        String createSQL = DefaultSQLBuilder.newBuilder()
                .insert(taskCommitCols.toArray(new String[0]))
                .into(TASK_COMMIT_TABLE_NAME)
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(
                createSQL,
                taskCommit.getId(),
                taskCommit.getDefinitionId(),
                VersionUtil.parseVersionNumber(taskCommit.getVersion()),
                taskCommit.getMessage(),
                JSONUtils.toJsonString(taskCommit.getSnapshot()),
                taskCommit.getCommitter(),
                taskCommit.getCommittedAt(),
                taskCommit.getCommitType().toString(),
                taskCommit.getCommitStatus().toString(),
                taskCommit.isLatestCommit()
        );

        return taskCommit;
    }

    public int updateCommitLatestFlag(Long commitId, boolean isLatest) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(TASK_COMMIT_TABLE_NAME)
                .set("is_latest")
                .where("id = ?")
                .asPrepared()
                .getSQL();
        return jdbcTemplate.update(sql, isLatest, commitId);
    }

    public static class TaskCommitMapper implements RowMapper<TaskCommit> {
        public static final TaskCommitMapper INSTANCE = new TaskCommitMapper();

        @Override
        public TaskCommit mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TaskCommit.newBuilder()
                    .withId(rs.getLong(TASK_COMMIT_MODEL_NAME + "_id"))
                    .withDefinitionId(rs.getLong(TASK_COMMIT_MODEL_NAME + "_task_def_id"))
                    .withVersion(VersionUtil.getVersion(rs.getInt(TASK_COMMIT_MODEL_NAME + "_version")))
                    .withMessage(rs.getString(TASK_COMMIT_MODEL_NAME + "_message"))
                    .withSnapshot(JSONUtils.toJavaObject(rs.getString(TASK_COMMIT_MODEL_NAME + "_snapshot"), TaskSnapshot.class))
                    .withCommitter(rs.getLong(TASK_COMMIT_MODEL_NAME + "_committer"))
                    .withCommittedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_COMMIT_MODEL_NAME + "_committed_at")))
                    .withCommitType(CommitType.resolve(rs.getString(TASK_COMMIT_MODEL_NAME + "_commit_type")))
                    .withCommitStatus(CommitStatus.resolve(rs.getString(TASK_COMMIT_MODEL_NAME + "_commit_status")))
                    .withLatestCommit(rs.getBoolean(TASK_COMMIT_MODEL_NAME + "_is_latest"))
                    .build();
        }
    }
}
