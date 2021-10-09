package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.metadata.core.model.dataset.DatasetSnapshot;
import com.miotech.kun.metadata.core.model.dataset.SchemaSnapshot;
import com.miotech.kun.metadata.core.model.dataset.StatisticsSnapshot;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Singleton
public class DatasetSnapshotDao {

    private static final String TABLE_NAME = "kun_mt_dataset_snapshot";

    private static final String[] TABLE_COLUMNS = { "id", "dataset_gid", "schema_snapshot", "statistics_snapshot", "schema_at", "statistics_at" };
    private static final String[] INSERT_COLUMNS = { "dataset_gid", "schema_snapshot", "statistics_snapshot", "schema_at", "statistics_at" };

    @Inject
    private DatabaseOperator dbOperator;

    public List<DatasetSnapshot> findByDatasetGid(Long gid) {
        SQLBuilder sqlBuilder = new DefaultSQLBuilder();
        String sql = sqlBuilder.select(TABLE_COLUMNS)
                .from(TABLE_NAME)
                .where("dataset_gid = ?")
                .getSQL();
        return dbOperator.fetchAll(sql, DatasetSnapshotDao.DatasetSnapshotMapper.INSTANCE, gid);
    }

    public void create(DatasetSnapshot datasetSnapshot) {
        String insertSQL = DefaultSQLBuilder.newBuilder()
                .insert(INSERT_COLUMNS)
                .into(TABLE_NAME)
                .asPrepared()
                .getSQL();
        dbOperator.update(insertSQL, datasetSnapshot.getDatasetGid(), JSONUtils.toJsonString(datasetSnapshot.getSchemaSnapshot()),
                JSONUtils.toJsonString(datasetSnapshot.getStatisticsSnapshot()), datasetSnapshot.getSchemaAt(), datasetSnapshot.getStatisticsAt());
    }

    public static class DatasetSnapshotMapper implements ResultSetMapper<DatasetSnapshot> {
        public static final DatasetSnapshotDao.DatasetSnapshotMapper INSTANCE = new DatasetSnapshotDao.DatasetSnapshotMapper();

        @Override
        public DatasetSnapshot map(ResultSet rs) throws SQLException {

            return DatasetSnapshot.newBuilder()
                    .withId(rs.getLong("id"))
                    .withDatasetGid(rs.getLong("dataset_gid"))
                    .withSchemaSnapshot(JSONUtils.jsonToObject(rs.getString("schema_snapshot"), SchemaSnapshot.class))
                    .withStatisticsSnapshot(parseStatisticsSnapshot(rs))
                    .withSchemaAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("schema_at")))
                    .withStatisticsAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("statistics_at")))
                    .build();
        }

        private StatisticsSnapshot parseStatisticsSnapshot(ResultSet rs) throws SQLException {
            String statisticsSnapshot = rs.getString("statistics_snapshot");
            if (StringUtils.isBlank(statisticsSnapshot)) {
                return null;
            }

            return JSONUtils.jsonToObject(statisticsSnapshot, StatisticsSnapshot.class);
        }
    }

}
