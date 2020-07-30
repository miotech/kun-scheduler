package com.miotech.kun.dataplatform.common.deploy.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.StringUtils;
import com.miotech.kun.dataplatform.common.deploy.vo.DeploySearchRequest;
import com.miotech.kun.dataplatform.model.deploy.Deploy;
import com.miotech.kun.dataplatform.model.deploy.DeployCommit;
import com.miotech.kun.dataplatform.model.deploy.DeployStatus;
import com.miotech.kun.workflow.client.model.PaginationResult;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.apache.logging.log4j.util.Strings;
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

@Repository
public class DeployDao {
    public static final String DEPLOY_COMMIT_TABLE_NAME = "kun_dp_deploy_commits";

    public static final String DEPLOY_COMMIT_MODEL_NAME = "deploycommit";

    private static final List<String> deployCommitCols = ImmutableList.of("deploy_id", "commit_id", "deploy_status");

    public static final String DEPLOY_TABLE_NAME = "kun_dp_deploy";

    private static final String DEPLOY_MODEL_NAME = "deploy";

    private static final List<String> deployCols = ImmutableList.of("id", "name", "creator", "submitted_at", "deployer", "deployed_at", "status");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String getSelectSQL(String whereClause) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(DEPLOY_MODEL_NAME, deployCols);
        SQLBuilder builder =  DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(DEPLOY_TABLE_NAME, DEPLOY_MODEL_NAME)
                .autoAliasColumns()
                .where(DEPLOY_MODEL_NAME + ".id = ?")
                .asPrepared();

        if (Strings.isNotBlank(whereClause)) {
            builder.where(whereClause);
        }

        return builder.getSQL();
    }

    public Optional<Deploy> fetchById(Long deployId) {
        String sql = getSelectSQL(DEPLOY_MODEL_NAME + ".id = ?");
        List<Deploy> deploys = jdbcTemplate.query(sql, DeployMapper.INSTANCE, deployId );
        return deploys.stream()
                .map( d -> d.cloneBuilder()
                        .withCommits(fetchCommitsByDeployId(d.getId()))
                        .build())
                .findAny();
    }

    private List<DeployCommit> fetchCommitsByDeployId(Long deployId) {
        return fetchCommitsByDeployIds(Collections.singletonList(deployId));
    }

    private List<DeployCommit> fetchCommitsByDeployIds(List<Long> deployIds) {
        Preconditions.checkNotNull(deployIds);
        if (deployIds.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(DEPLOY_COMMIT_MODEL_NAME, deployCommitCols);
        String sql =  DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .from(DEPLOY_COMMIT_TABLE_NAME, DEPLOY_COMMIT_MODEL_NAME)
                .autoAliasColumns()
                .where(DEPLOY_COMMIT_MODEL_NAME + String.format(".deploy_id in (%s)", StringUtils.repeatJoin("?", ",", deployIds.size())))
                .asPrepared()
                .getSQL();

        return jdbcTemplate.query(sql, DeployCommitMapper.INSTANCE, deployIds.toArray());
    }

    public PaginationResult<Deploy> search(DeploySearchRequest searchRequest) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" 1 = 1");
        List<Object> params = new ArrayList();
        List<Long> creatorIds = searchRequest.getCreatorIds();
        if (!creatorIds.isEmpty()) {
            whereClause.append(" AND ");
            whereClause.append(String.format(DEPLOY_MODEL_NAME + ".creator in (%s)", StringUtils.repeatJoin("?", ",", creatorIds.size())));
            params.addAll(creatorIds);
        }

        List<Long> deployerIds = searchRequest.getDeployerIds();
        if (!deployerIds.isEmpty()) {
            whereClause.append(" AND ");
            whereClause.append(String.format(DEPLOY_MODEL_NAME + ".deployer in (%s)", StringUtils.repeatJoin("?", ",", deployerIds.size())));
            params.addAll(deployerIds);
        }

        Optional<OffsetDateTime> deployedAtFrom = searchRequest.getDepoyedAtFrom();
        if (deployedAtFrom.isPresent()) {
            whereClause.append(" AND ");
            whereClause.append(DEPLOY_MODEL_NAME + ".deployed_at >= ? ");
            params.add(deployedAtFrom.get());
        }

        Optional<OffsetDateTime> deployedAtTo = searchRequest.getDepoyedAtTo();
        if (deployedAtTo.isPresent()) {
            whereClause.append(" AND ");
            whereClause.append(DEPLOY_MODEL_NAME + ".deployed_at <= ? ");
            params.add(deployedAtTo.get());
        }

        Optional<OffsetDateTime> submittedAtFrom = searchRequest.getSubmittedAtFrom();
        if (submittedAtFrom.isPresent()) {
            whereClause.append(" AND ");
            whereClause.append(DEPLOY_MODEL_NAME + ".submitted_at >= ? ");
            params.add(submittedAtFrom.get());
        }

        Optional<OffsetDateTime> submittedAtTo = searchRequest.getSubmittedAtTo();
        if (submittedAtTo.isPresent()) {
            whereClause.append(" AND ");
            whereClause.append(DEPLOY_MODEL_NAME + ".submitted_at <= ? ");
            params.add(submittedAtTo.get());
        }

        String countSql = DefaultSQLBuilder.newBuilder()
                .select("COUNT(1)")
                .from(DEPLOY_TABLE_NAME, DEPLOY_MODEL_NAME)
                .where(whereClause.toString())
                .getSQL();
        // count
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
        // list
        List<Deploy> deploys = jdbcTemplate.query(sql, DeployMapper.INSTANCE, params.toArray());
        // bind deploy commits
        List<Long> deployIds = deploys.stream().map(Deploy::getId).collect(Collectors.toList());
        Map<Long, List<DeployCommit>> commits = fetchCommitsByDeployIds(deployIds)
                .stream()
                .collect(Collectors.groupingBy(DeployCommit::getDeployId));

        return new PaginationResult<>(
                searchRequest.getPageSize(),
                searchRequest.getPageNum(),
                totalCount,
                deploys.stream()
                        .map(x -> x.cloneBuilder()
                        .withCommits(commits.get(x.getId())).build())
                        .collect(Collectors.toList())
        );
    }

    @Transactional
    public void create(Deploy deploy) {
        Preconditions.checkArgument(!deploy.getCommits().isEmpty(), "Commits should not be empty");
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(deployCols.toArray(new String[0]))
                .into(DEPLOY_TABLE_NAME)
                .asPrepared()
                .getSQL();

        jdbcTemplate.update(
                sql,
                deploy.getId(),
                deploy.getName(),
                deploy.getCreator(),
                deploy.getSubmittedAt(),
                deploy.getDeployer(),
                deploy.getDeployedAt(),
                deploy.getStatus().toString()
        );
        createDeployCommits(deploy.getCommits());
    }

    private void createDeployCommits(List<DeployCommit> commits) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(deployCommitCols.toArray(new String[0]))
                .into(DEPLOY_COMMIT_TABLE_NAME)
                .asPrepared()
                .getSQL();

        List<Object[]> params = commits.stream().map( x -> {
            return new Object[]{x.getDeployId(), x.getCommit(), x.getDeployStatus().toString() };
        }).collect(Collectors.toList());
        jdbcTemplate.batchUpdate(sql, params);
    }

    @Transactional
    public void updateDeploy(Deploy deploy) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(DEPLOY_TABLE_NAME)
                .set( "deployer", "deployed_at", "status")
                .where(" id = ? ")
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(
                sql,
                deploy.getDeployer(),
                deploy.getDeployedAt(),
                deploy.getStatus().toString(),
                deploy.getId()
        );
        updateDeployCommits(deploy.getCommits());
    }

    private void updateDeployCommits(List<DeployCommit> commits) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(DEPLOY_COMMIT_TABLE_NAME)
                .set("deploy_status")
                .where(" deploy_id = ? AND commit_id = ? ")
                .asPrepared()
                .getSQL();

        List<Object[]> params = commits.stream().map( x -> {
            return new Object[]{x.getDeployStatus().toString(), x.getDeployId(), x.getCommit() };
        }).collect(Collectors.toList());
        jdbcTemplate.batchUpdate(sql, params);
    }

    public static class DeployMapper implements RowMapper<Deploy> {
        public static final DeployMapper INSTANCE = new DeployMapper();

        @Override
        public Deploy mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Deploy.newBuilder()
                    .withId(rs.getLong(DEPLOY_MODEL_NAME + "_id"))
                    .withName(rs.getString(DEPLOY_MODEL_NAME + "_name"))
                    .withCreator(rs.getLong(DEPLOY_MODEL_NAME + "_creator"))
                    .withSubmittedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(DEPLOY_MODEL_NAME + "_submitted_at")))
                    .withDeployer(rs.getLong(DEPLOY_MODEL_NAME + "_deployer"))
                    .withDeployedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(DEPLOY_MODEL_NAME + "_deployed_at")))
                    .withStatus(DeployStatus.resolve(rs.getString(DEPLOY_MODEL_NAME + "_status")))
                    .build();
        }
    }

    public static class DeployCommitMapper implements RowMapper<DeployCommit> {
        public static final DeployCommitMapper INSTANCE = new DeployCommitMapper();

        @Override
        public DeployCommit mapRow(ResultSet rs, int rowNum) throws SQLException {
            return DeployCommit.newBuilder()
                    .withDeployId(rs.getLong(DEPLOY_COMMIT_MODEL_NAME + "_deploy_id"))
                    .withCommit(rs.getLong(DEPLOY_COMMIT_MODEL_NAME + "_commit_id"))
                    .withDeployStatus(DeployStatus.resolve(rs.getString(DEPLOY_COMMIT_MODEL_NAME + "_deploy_status")))
                    .build();
        }
    }
}
