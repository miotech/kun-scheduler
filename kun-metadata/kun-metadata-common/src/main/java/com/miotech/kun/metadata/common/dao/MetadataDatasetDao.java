package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Singleton
public class MetadataDatasetDao {
    private static final Logger logger = LoggerFactory.getLogger(MetadataDatasetDao.class);

    private static final String[] DATASET_COLUMNS = { "gid", "name", "datasource_id", "schema", "data_store", "database_name", "deleted" };
    private static final String[] DATASET_FIELD_COLUMNS = { "name", "type", "description", "raw_type, is_primary_key, is_nullable" };

    private static final String DATASET_TABLE_NAME = "kun_mt_dataset";
    private static final String DATASET_FIELD_TABLE_NAME = "kun_mt_dataset_field";

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

        if (fetchedDataset == null) {
            return Optional.ofNullable(null);
        }

        SQLBuilder fieldsSQLBuilder = new DefaultSQLBuilder();
        String fieldsSQL = fieldsSQLBuilder.select(DATASET_FIELD_COLUMNS)
                .from(DATASET_FIELD_TABLE_NAME)
                .where("dataset_gid = ?")
                .getSQL();

        List<DatasetField> fields = dbOperator.fetchAll(fieldsSQL, MetadataDatasetFieldMapper.INSTANCE, gid);
        Dataset dataset = fetchedDataset.cloneBuilder()
                .withFields(fields).build();

        return Optional.ofNullable(dataset);
    }

    /**
     * Database result set mapper for {@link Dataset} object
     */
    private static class MetadataDatasetMapper implements ResultSetMapper<Dataset> {
        public static final ResultSetMapper<Dataset> INSTANCE = new MetadataDatasetMapper();

        @Override
        public Dataset map(ResultSet rs) throws SQLException {
            DataStore dataStore = DataStoreJsonUtil.toDataStore(rs.getString(5));

            Dataset dataset = Dataset.newBuilder()
                    .withGid(rs.getLong(1))
                    .withName(rs.getString(2))
                    .withDatasourceId(rs.getLong(3))
                    .withDeleted(rs.getBoolean(7))
                    // TODO: parse missing fields
                    .withTableStatistics(null)
                    .withFields(null)
                    .withFieldStatistics(null)
                    .withDataStore(dataStore)
                    .build();
            return dataset;
        }
    }

    private static class MetadataDatasetFieldMapper implements ResultSetMapper<DatasetField> {
        public static final ResultSetMapper<DatasetField> INSTANCE = new MetadataDatasetFieldMapper();


        @Override
        public DatasetField map(ResultSet rs) throws SQLException {
            String name = rs.getString(1);
            String type = rs.getString(2);
            String description = rs.getString(3);
            String rawType = rs.getString(4);
            boolean isPrimaryKey = rs.getBoolean(5);
            boolean isNullable = rs.getBoolean(6);
            return DatasetField.newBuilder()
                    .withName(name)
                    .withComment(description)
                    .withFieldType(new DatasetFieldType(DatasetFieldType.convertRawType(type), rawType))
                    .withIsPrimaryKey(isPrimaryKey)
                    .withIsNullable(isNullable)
                    .build();
        }
    }
}
