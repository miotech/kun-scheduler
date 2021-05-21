package com.miotech.kun.metadata.common.dao;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.commons.utils.StringUtils;
import com.miotech.kun.metadata.core.model.process.PullDataSourceProcess;
import com.miotech.kun.metadata.core.model.process.PullDatasetProcess;
import com.miotech.kun.metadata.core.model.process.PullProcess;
import com.miotech.kun.metadata.core.model.process.PullProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * DAO layer of metadata pull process entities.
 * A pull process represents a historical record on fetching metadata from data sources, datasets, etc.
 * See db migration scripts for persistence schema: resources/sql/V11__pull_process.sql
 */
@Singleton
@SuppressWarnings("java:S1192")
public class PullProcessDao {
    private static final Logger logger = LoggerFactory.getLogger(PullProcessDao.class);

    private static final String PULL_PROCESS_TABLE_NAME = "kun_mt_pull_process";

    private static final String[] TABLE_COLUMNS = { "process_id", "process_type", "datasource_id", "dataset_id", "mce_task_run_id", "mse_task_run_id", "created_at" };

    @Inject
    DatabaseOperator dbOperator;

    public Optional<PullProcess> findById(Long processId) {
        SQLBuilder sqlBuilder = new DefaultSQLBuilder();
        String sql = sqlBuilder.select(TABLE_COLUMNS)
                .from(PULL_PROCESS_TABLE_NAME)
                .where("process_id = ?")
                .getSQL();
        return Optional.ofNullable(dbOperator.fetchOne(sql, PullProcessResultMapper.INSTANCE, processId));
    }

    public Optional<PullDataSourceProcess> findLatestPullDataSourceProcessByDataSourceId(Long dataSourceId) {
        SQLBuilder sqlBuilder = new DefaultSQLBuilder();
        String sql = sqlBuilder.select(TABLE_COLUMNS)
                .from(PULL_PROCESS_TABLE_NAME)
                .where("datasource_id = ? AND process_type = ?")
                .limit(1)
                .orderBy("created_at DESC")
                .getSQL();
        return Optional.ofNullable((PullDataSourceProcess) dbOperator.fetchOne(
                        sql,
                        PullProcessResultMapper.INSTANCE,
                        dataSourceId, PullProcessType.DATASOURCE.toString())
        );
    }

    public Map<Long, PullDataSourceProcess> findLatestPullDataSourceProcessesByDataSourceIds(Collection<Long> dataSourceIds) {
        Preconditions.checkNotNull(dataSourceIds, "Argument `dataSourceIds` cannot be null.");
        if (dataSourceIds.isEmpty()) {
            return new HashMap<>();
        }
        final String sql = String.format("SELECT * FROM (\n" +
                "SELECT %s, (rank() OVER (PARTITION BY datasource_id ORDER BY created_at DESC)) AS rnk\n" +
                "FROM %s\n" +
                "WHERE datasource_id IN (%s) AND process_type = 'DATASOURCE'\n" +
                "ORDER BY rnk, created_at DESC\n" +
                ") t WHERE t.rnk = 1",
                String.join(", ", TABLE_COLUMNS),
                PULL_PROCESS_TABLE_NAME,
                StringUtils.repeatJoin("?", ", ", dataSourceIds.size())
        );
        List<Object> params = new ArrayList<>(dataSourceIds.size());
        params.addAll(dataSourceIds);
        List<PullProcess> fetchedRows = dbOperator.fetchAll(sql, PullProcessResultMapper.INSTANCE, params.toArray());
        Map<Long, PullDataSourceProcess> results = new HashMap<>();
        for (PullProcess row : fetchedRows) {
            PullDataSourceProcess r = (PullDataSourceProcess) row;
            Long dataSourceId = r.getDataSourceId();
            Preconditions.checkState(dataSourceId != null, "Unexpected pull process id = {} where data source id = null.", row.getProcessId());
            results.put(dataSourceId, r);
        }

        return results;
    }

    public Optional<PullDatasetProcess> findLatestPullDatasetProcessByDataSetId(Long datasetId) {
        SQLBuilder sqlBuilder = new DefaultSQLBuilder();
        String sql = sqlBuilder.select(TABLE_COLUMNS)
                .from(PULL_PROCESS_TABLE_NAME)
                .where("dataset_id = ? AND process_type = ?")
                .limit(1)
                .orderBy("created_at DESC")
                .getSQL();
        return Optional.ofNullable((PullDatasetProcess) dbOperator.fetchOne(
                sql,
                PullProcessResultMapper.INSTANCE,
                datasetId, PullProcessType.DATASET.toString())
        );
    }

    public PullProcess create(PullProcess pullProcess) {
        Long id = (pullProcess.getProcessId() == null) ? IdGenerator.getInstance().nextId() : pullProcess.getProcessId();
        String sql;
        Optional<PullProcess> createdPullProcess;
        switch (pullProcess.getProcessType()) {
            case DATASOURCE:
                PullDataSourceProcess processDataSource = (PullDataSourceProcess) pullProcess;
                Preconditions.checkArgument(processDataSource.getDataSourceId() != null, "Cannot save a pull datasource process model without datasource id.");
                logger.info("Creating pull process for datasource id = {}, mce task run id = {}", processDataSource.getDataSourceId(), processDataSource.getMceTaskRunId());
                sql = "INSERT INTO " + PULL_PROCESS_TABLE_NAME + " (process_id, process_type, datasource_id, mce_task_run_id, created_at) VALUES (?, ?, ?, ?, ?)";
                dbOperator.update(sql,
                        id,
                        PullProcessType.DATASOURCE.toString(),
                        processDataSource.getDataSourceId(),
                        processDataSource.getMceTaskRunId(),
                        processDataSource.getCreatedAt()
                );
                // fetch created instance
                createdPullProcess = findById(id);
                break;
            case DATASET:
                PullDatasetProcess processDataset = (PullDatasetProcess) pullProcess;
                Preconditions.checkArgument(processDataset.getDatasetId() != null, "Cannot save a pull dataset process model without dataset id.");
                logger.info("Creating pull process for dataset id = {}, mce task run id = {}", processDataset.getDatasetId(), processDataset.getMceTaskRunId());
                sql = "INSERT INTO " + PULL_PROCESS_TABLE_NAME + " (process_id, process_type, dataset_id, mce_task_run_id, mse_task_run_id, created_at) VALUES (?, ?, ?, ?, ?, ?)";
                dbOperator.update(sql,
                        id,
                        PullProcessType.DATASET.toString(),
                        processDataset.getDatasetId(),
                        processDataset.getMceTaskRunId(),
                        processDataset.getMseTaskRunId(),
                        processDataset.getCreatedAt()
                );
                // fetch created instance
                createdPullProcess = findById(id);
                break;
            default:
                throw new IllegalStateException();
        }
        if (!createdPullProcess.isPresent()) {
            throw new IllegalStateException(String.format("Cannot find created pulling process of id = %s", id));
        }
        return createdPullProcess.get();
    }

    public static class PullProcessResultMapper implements ResultSetMapper<PullProcess> {
        public static final PullProcessResultMapper INSTANCE = new PullProcessResultMapper();

        @Override
        public PullProcess map(ResultSet rs) throws SQLException {
            PullProcessType processType = PullProcessType.from(rs.getString("process_type"));
            switch (processType) {
                case DATASOURCE:
                    return PullDataSourceProcess.newBuilder()
                            .withProcessId(rs.getLong("process_id"))
                            .withMceTaskRunId(rs.getLong("mce_task_run_id"))
                            .withCreatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("created_at")))
                            .withDataSourceId(rs.getLong("datasource_id"))
                            .build();
                case DATASET:
                    return PullDatasetProcess.newBuilder()
                            .withProcessId(rs.getLong("process_id"))
                            .withMceTaskRunId(rs.getLong("mce_task_run_id"))
                            .withMseTaskRunId(rs.getLong("mse_task_run_id"))
                            .withCreatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("created_at")))
                            .withDatasetId(rs.getLong("dataset_id"))
                            .build();
                default:
                    throw new IllegalStateException(String.format("Cannot map pull process row with undefined process type = \"%s\"", processType));
            }
        }
    }
}
