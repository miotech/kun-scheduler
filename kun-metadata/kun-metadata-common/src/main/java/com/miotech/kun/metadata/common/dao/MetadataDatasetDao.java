package com.miotech.kun.metadata.common.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetBaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Singleton
public class MetadataDatasetDao {
    private static final Logger logger = LoggerFactory.getLogger(MetadataDatasetDao.class);

    private static final String[] DATASET_COLUMNS = { "gid", "name", "datasource_id", "schema", "data_store", "database_name" };

    private static final String DATASET_TABLE_NAME = "kun_mt_dataset";

    @Inject
    DatabaseOperator dbOperator;

    /**
     * Fetch dataset by its global id and returns an optional object.
     * @param gid global id
     * @return dataset wrapped by optional object
     */
    public Optional<Dataset> fetchDatasetByGid(long gid) {
        SQLBuilder sqlBuilder = new DefaultSQLBuilder();
        String sql = sqlBuilder.select(DATASET_COLUMNS)
                .from(DATASET_TABLE_NAME)
                .where("gid = ?")
                .getSQL();
        logger.debug("Fetching dataset with gid: {}", gid);
        logger.debug("Dataset query sql: {}", sql);
        Dataset fetchedDataset = dbOperator.fetchOne(sql, MetadataDatasetMapper.INSTANCE, gid);
        logger.debug("Fetched dataset: {} with gid = {}", fetchedDataset, gid);
        return Optional.ofNullable(fetchedDataset);
    }

    public List<DatasetBaseInfo> fetchDatasetsByDatasourceAndNameLike(Long datasourceId, String name) {
        SQLBuilder sqlBuilder = new DefaultSQLBuilder();
        String sql = sqlBuilder.select(DATASET_COLUMNS)
                .from(DATASET_TABLE_NAME)
                .where("datasource_id = ? AND name LIKE CONCAT('%', CAST(? AS TEXT), '%')")
                .getSQL();
        return dbOperator.fetchAll(sql, MetadataDatasetBaseInfoMapper.INSTANCE, datasourceId, name);
    }

    /**
     * Database result set mapper for {@link Dataset} object
     */
    private static class MetadataDatasetMapper implements ResultSetMapper<Dataset> {
        public static final ResultSetMapper<Dataset> INSTANCE = new MetadataDatasetMapper();

        @Override
        public Dataset map(ResultSet rs) throws SQLException {
            DataStore dataStore;

            try {
                dataStore = DataStoreJsonUtil.toDataStore(rs.getString(5));
            } catch (JsonProcessingException e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }

            Dataset dataset = Dataset.newBuilder()
                    .withName(rs.getString(2))
                    .withDatasourceId(rs.getLong(3))
                    // TODO: parse missing fields
                    .withDatasetStat(null)
                    .withFields(null)
                    .withFieldStats(null)
                    .withDataStore(dataStore)
                    .build();
            dataset.setGid(rs.getLong(1));
            return dataset;
        }
    }

    private static class MetadataDatasetBaseInfoMapper implements ResultSetMapper<DatasetBaseInfo> {
        public static final ResultSetMapper<DatasetBaseInfo> INSTANCE = new MetadataDatasetBaseInfoMapper();

        @Override
        public DatasetBaseInfo map(ResultSet rs) throws SQLException {
            DataStore dataStore;

            try {
                dataStore = DataStoreJsonUtil.toDataStore(rs.getString(5));
            } catch (JsonProcessingException e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }

            DatasetBaseInfo datasetBaseInfo = DatasetBaseInfo.newBuilder()
                    .withGid(rs.getLong(1))
                    .withName(rs.getString(2))
                    .withDatasourceId(rs.getLong(3))
                    .withDataStore(dataStore)
                    .withDatabaseName(rs.getString(6))
                    .build();
            return datasetBaseInfo;
        }
    }
}
