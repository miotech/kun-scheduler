package com.miotech.kun.metadata.common.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.vo.DatasetColumnSuggestRequest;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Singleton
public class MetadataDatasetDao {
    private static final Logger logger = LoggerFactory.getLogger(MetadataDatasetDao.class);

    private static final String[] DATASET_COLUMNS = {"gid", "name", "datasource_id", "schema", "data_store", "database_name", "deleted"};
    private static final String[] DATASET_FIELD_COLUMNS = {"name", "type", "description", "raw_type, is_primary_key, is_nullable"};

    private static final String DATASET_TABLE_NAME = "kun_mt_dataset";
    private static final String DATASET_FIELD_TABLE_NAME = "kun_mt_dataset_field";

    public static final String DATASET_MODEL_NAME = "dataset";
    public static final String DATASET_FIELD_MODEL_NAME = "dataset_field";

    @Inject
    DatabaseOperator dbOperator;

    /**
     * Fetch dataset by its global id and returns an optional object.
     *
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

    public List<String> suggestDatabase(Long dataSourceId, String prefix) {
        logger.debug("Suggest database by dataSourceId: {}, prefix: {}", dataSourceId, prefix);
        Pair<String, List<Object>> whereClause = generateWhereClauseForSuggestDatabase(dataSourceId, prefix);

        String fetchDatabaseNameSQL = DefaultSQLBuilder.newBuilder()
                .select("distinct(database_name)")
                .from(DATASET_TABLE_NAME)
                .where(whereClause.getLeft())
                .orderBy("database_name asc")
                .getSQL();
        return dbOperator.fetchAll(fetchDatabaseNameSQL, rs -> rs.getString(1), whereClause.getRight().toArray());
    }

    private Pair<String, List<Object>> generateWhereClauseForSuggestDatabase(Long dataSourceId, String prefix) {
        String result = "datasource_id = ? and deleted = false ";
        List<Object> params = Lists.newArrayList(dataSourceId);
        if (StringUtils.isBlank(prefix)) {
            return Pair.of(result, params);
        }

        params.add(prefix);
        return Pair.of(result + "and database_name like concat(cast(? as text), '%')", params);
    }

    public List<String> suggestTable(Long dataSourceId, String databaseName, String prefix) {
        logger.debug("Suggest table, dataSourceId: {}, databaseName: {}, prefix: {}", dataSourceId, databaseName, prefix);
        Pair<String, List<Object>> whereClause = generateWhereClauseForSuggestTable(dataSourceId, databaseName, prefix);

        String fetchTableNameSQL = DefaultSQLBuilder.newBuilder()
                .select("name")
                .from(DATASET_TABLE_NAME)
                .where(whereClause.getLeft())
                .orderBy("name asc")
                .getSQL();
        return dbOperator.fetchAll(fetchTableNameSQL, rs -> rs.getString(1), whereClause.getRight().toArray());
    }

    private Pair<String, List<Object>> generateWhereClauseForSuggestTable(Long dataSourceId, String databaseName, String prefix) {
        String sql = "datasource_id = ? and database_name = ? and deleted = false ";
        List<Object> params = Lists.newArrayList(dataSourceId, databaseName);
        if (StringUtils.isBlank(prefix)) {
            return Pair.of(sql, params);
        }

        params.add(prefix);
        return Pair.of(sql + "and name like concat(cast(? as text), '%')", params);
    }

    public List<String> suggestColumn(Long dataSourceId, DatasetColumnSuggestRequest request) {
        Pair<String, List<Object>> whereClause = generateWhereClauseForSuggestColumn(dataSourceId, request);

        String fetchColumnSQL = DefaultSQLBuilder.newBuilder()
                .select(DATASET_FIELD_MODEL_NAME + ".name")
                .from(DATASET_TABLE_NAME, DATASET_MODEL_NAME)
                .join("INNER", DATASET_FIELD_TABLE_NAME, DATASET_FIELD_MODEL_NAME)
                .on(DATASET_MODEL_NAME + ".gid = " + DATASET_FIELD_MODEL_NAME + ".dataset_gid")
                .where(whereClause.getLeft())
                .orderBy(DATASET_FIELD_MODEL_NAME + ".name asc")
                .getSQL();
        return dbOperator.fetchAll(fetchColumnSQL, rs -> rs.getString(1), whereClause.getRight().toArray());
    }

    private Pair<String, List<Object>> generateWhereClauseForSuggestColumn(Long dataSourceId, DatasetColumnSuggestRequest request) {
        String result = DATASET_MODEL_NAME + ".datasource_id = ? and "
                + DATASET_MODEL_NAME + ".database_name = ? and "
                + DATASET_MODEL_NAME + ".name = ? and "
                + DATASET_MODEL_NAME + ".deleted = false ";
        List<Object> params = Lists.newArrayList(dataSourceId, request.getDatabaseName(), request.getTableName());
        if (StringUtils.isBlank(request.getPrefix())) {
            return Pair.of(result, params);
        }

        params.add(request.getPrefix());
        return Pair.of(result + "and " + DATASET_FIELD_MODEL_NAME + ".name like concat(cast(? as text), '%')", params);
    }

    public Optional<Dataset> findByName(String tableName) {
        SQLBuilder sqlBuilder = new DefaultSQLBuilder();
        String sql = sqlBuilder.select(DATASET_COLUMNS)
                .from(DATASET_TABLE_NAME)
                .where("name = ?")
                .getSQL();
        logger.debug("Fetching dataset with tableName: {}", tableName);
        logger.debug("Dataset query sql: {}", sql);
        Dataset fetchedDataset = dbOperator.fetchOne(sql, MetadataDatasetMapper.INSTANCE, tableName);
        logger.debug("Fetched dataset: {} with tableName = {}", fetchedDataset, tableName);

        if (fetchedDataset == null) {
            return Optional.ofNullable(null);
        }

        SQLBuilder fieldsSQLBuilder = new DefaultSQLBuilder();
        String fieldsSQL = fieldsSQLBuilder.select(DATASET_FIELD_COLUMNS)
                .from(DATASET_FIELD_TABLE_NAME)
                .where("dataset_gid = ?")
                .getSQL();

        List<DatasetField> fields = dbOperator.fetchAll(fieldsSQL, MetadataDatasetFieldMapper.INSTANCE, fetchedDataset.getGid());
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
                    .withFieldType(new DatasetFieldType(DatasetFieldType.Type.valueOf(type), rawType))
                    .withIsPrimaryKey(isPrimaryKey)
                    .withIsNullable(isNullable)
                    .build();
        }
    }
}
