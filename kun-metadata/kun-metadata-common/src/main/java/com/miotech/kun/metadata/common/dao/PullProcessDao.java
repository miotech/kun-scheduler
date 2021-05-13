package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.core.model.process.PullDataSourceProcess;
import com.miotech.kun.metadata.core.model.process.PullDatasetProcess;
import com.miotech.kun.metadata.core.model.process.PullProcess;
import com.miotech.kun.metadata.core.model.process.PullProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

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

    public Optional<PullDataSourceProcess> findLatestPullDataSourceProcessByDataSourceId(String dataSourceId) {
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

    public Optional<PullDatasetProcess> findLatestPullDatasetProcessByDataSetId(String datasetId) {
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

    public boolean create(PullProcess pullProcess) {
        String sql;
        int affectedRows;
        switch (pullProcess.getProcessType()) {
            case DATASOURCE:
                PullDataSourceProcess processDataSource = (PullDataSourceProcess) pullProcess;
                logger.info("Creating pull process for datasource id = {}, mce task run id = {}", processDataSource.getDataSourceId(), processDataSource.getMceTaskRunId());
                sql = "INSERT INTO " + PULL_PROCESS_TABLE_NAME + " (process_id, process_type, datasource_id, mce_task_run_id, created_at) VALUES (?, ?, ?, ?, ?)";
                affectedRows = dbOperator.update(sql,
                        processDataSource.getProcessId(),
                        PullProcessType.DATASOURCE.toString(),
                        processDataSource.getDataSourceId(),
                        processDataSource.getMceTaskRunId(),
                        processDataSource.getCreatedAt()
                );
                break;
            case DATASET:
                PullDatasetProcess processDataset = (PullDatasetProcess) pullProcess;
                logger.info("Creating pull process for dataset id = {}, mce task run id = {}", processDataset.getDatasetId(), processDataset.getMceTaskRunId());
                sql = "INSERT INTO " + PULL_PROCESS_TABLE_NAME + " (process_id, process_type, dataset_id, mce_task_run_id, mse_task_run_id, created_at) VALUES (?, ?, ?, ?, ?, ?)";
                affectedRows = dbOperator.update(sql,
                        processDataset.getProcessId(),
                        PullProcessType.DATASET.toString(),
                        processDataset.getDatasetId(),
                        processDataset.getMceTaskRunId(),
                        processDataset.getMseTaskRunId(),
                        processDataset.getCreatedAt()
                );
                break;
            default:
                throw new IllegalStateException();
        }
        return affectedRows > 0;
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
                            .withDataSourceId(rs.getString("datasource_id"))
                            .build();
                case DATASET:
                    return PullDatasetProcess.newBuilder()
                            .withProcessId(rs.getLong("process_id"))
                            .withMceTaskRunId(rs.getLong("mce_task_run_id"))
                            .withMseTaskRunId(rs.getLong("mse_task_run_id"))
                            .withCreatedAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("created_at")))
                            .withDatasetId(rs.getString("dataset_id"))
                            .build();
                default:
                    throw new IllegalStateException(String.format("Cannot map pull process row with undefined process type = \"%s\"", processType));
            }
        }
    }
}
