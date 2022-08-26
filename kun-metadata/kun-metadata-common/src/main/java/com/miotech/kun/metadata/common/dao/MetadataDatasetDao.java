package com.miotech.kun.metadata.common.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.commons.utils.NumberUtils;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.metadata.core.model.constant.DatasetLifecycleStatus;
import com.miotech.kun.metadata.core.model.dataset.*;
import com.miotech.kun.metadata.core.model.vo.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class MetadataDatasetDao {
    private static final Logger logger = LoggerFactory.getLogger(MetadataDatasetDao.class);

    private static final String[] DATASET_COLUMNS = {"gid", "name", "datasource_id", "data_store", "database_name", "dsi", "deleted"};
    private static final String[] DATASET_FIELD_COLUMNS = {"name", "type", "description", "raw_type, is_primary_key, is_nullable"};

    private static final String DATASET_TABLE_NAME = "kun_mt_dataset";
    private static final String DATASET_FIELD_TABLE_NAME = "kun_mt_dataset_field";

    public static final String DATASET_MODEL_NAME = "dataset";
    public static final String DATASET_FIELD_MODEL_NAME = "dataset_field";

    private static final String[] DATASET_LIFE_CYCLE_COLUMNS = {"dataset_gid", "fields", "status", "create_at"};
    private static final String DATASET_LIFE_CYCLE_TABLE_NAME = "kun_mt_dataset_lifecycle";

    private static final Map<String, String> SORT_KEY_MAP = new HashMap<>();

    static {
        SORT_KEY_MAP.put("name", "name");
        SORT_KEY_MAP.put("databaseName", "database_name");
        SORT_KEY_MAP.put("datasourceName", "datasource_name");
        SORT_KEY_MAP.put("type", "type");
        SORT_KEY_MAP.put("highWatermark", "high_watermark");
    }

    @Inject
    DatabaseOperator dbOperator;

    @Inject
    TagDao tagDao;

    /**
     * Fetch dataset by its global id and returns an optional object.
     *
     * @param gid global id
     * @return dataset wrapped by optional object
     */
    public Optional<Dataset> fetchDatasetByGid(long gid) {
        Dataset fetchedDataset = fetchBasicDatasetByGid(gid);

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

    public Dataset fetchBasicDatasetByGid(long gid) {
        SQLBuilder sqlBuilder = new DefaultSQLBuilder();
        String sql = sqlBuilder.select(DATASET_COLUMNS)
                .from(DATASET_TABLE_NAME)
                .where("gid = ?")
                .getSQL();
        logger.debug("Fetching dataset with gid: {}", gid);
        logger.debug("Dataset query sql: {}", sql);
        Dataset fetchedDataset = dbOperator.fetchOne(sql, MetadataDatasetMapper.INSTANCE, gid);
        logger.debug("Fetched dataset: {} with gid = {}", fetchedDataset, gid);
        return fetchedDataset;
    }

    public List<Dataset> fetchBasicDatasetByGids(List<Long> gids) {
        if (gids.size() == 0) {
            return new ArrayList<>();
        }
        SQLBuilder sqlBuilder = new DefaultSQLBuilder();
        String sql = sqlBuilder.select(DATASET_COLUMNS)
                .from(DATASET_TABLE_NAME)
                .where("gid in (" + StringUtils.repeat("?", ",", gids.size()) + " )")
                .getSQL();
        logger.debug("Fetching dataset with gids: {}", gids);
        logger.debug("Dataset query sql: {}", sql);
        List<Dataset> fetchedDatasets = dbOperator.fetchAll(sql, MetadataDatasetMapper.INSTANCE, gids.toArray());
        return fetchedDatasets;
    }

    public Dataset createDataset(Dataset dataset) {
        String dsi = dataset.getDSI();
        logger.debug("fetching dataset by dsi = {}", dsi);
        Dataset fetchDatasetByDSI = fetchDatasetByDSI(dsi);
        if (fetchDatasetByDSI != null) {
            return fetchDatasetByDSI;
        }
        long gid = IdGenerator.getInstance().nextId();
        String datasetSql = new DefaultSQLBuilder()
                .insert(DATASET_COLUMNS)
                .into(DATASET_TABLE_NAME)
                .asPrepared()
                .getSQL();
        try {
            dbOperator.update(datasetSql,
                    gid, dataset.getName(),
                    dataset.getDatasourceId(),
                    DataStoreJsonUtil.toJson(dataset.getDataStore()),
                    dataset.getDatabaseName(),
                    dsi,
                    false
            );
        } catch (Exception e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                return fetchDatasetByDSI(dsi);
            }
            logger.error("insert into error", e);
            throw e;
        }
        SchemaSnapshot.Builder builder = SchemaSnapshot.newBuilder();
        if (dataset.getFields() != null) {
            builder.withFields(dataset.getFields().stream().map(field -> field.convert()).collect(Collectors.toList()));
        }
        SchemaSnapshot schemaSnapshot = builder.build();
        String lifecycleSql = new DefaultSQLBuilder()
                .insert(DATASET_LIFE_CYCLE_COLUMNS)
                .into(DATASET_LIFE_CYCLE_TABLE_NAME)
                .asPrepared()
                .getSQL();
        dbOperator.update(lifecycleSql,
                gid, JSONUtils.toJsonString(schemaSnapshot), DatasetLifecycleStatus.MANAGED.name(), DateTimeUtils.now());
        Optional<Dataset> datasetOptional = fetchDatasetByGid(gid);
        if (datasetOptional.isPresent()) {
            return datasetOptional.get();
        }
        return null;
    }

    public Dataset fetchDatasetByDSI(String dsi) {
        // Convert dataStore to JSON
        Preconditions.checkNotNull(dsi, "DSI cannot be null");
        String sql = new DefaultSQLBuilder().select(DATASET_COLUMNS)
                .from(DATASET_TABLE_NAME)
                .where("dsi = ?")
                .asPrepared()
                .getSQL();
        return dbOperator.fetchOne(
                sql,
                MetadataDatasetMapper.INSTANCE,
                dsi
        );
    }

    public Optional<Dataset> fetchDataSet(Long datasourceId, String database, String table) {
        String sql = new DefaultSQLBuilder()
                .select(DATASET_COLUMNS)
                .from(DATASET_TABLE_NAME)
                .where("datasource_id = ? AND database_name = ? AND name = ?")
                .asPrepared()
                .getSQL();
        Dataset fetchedDataset = dbOperator.fetchOne(sql, MetadataDatasetMapper.INSTANCE, datasourceId, database, table);
        logger.debug("Fetched dataset: {} with tableName = {}", fetchedDataset, table);

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


    public List<DatabaseBaseInfo> getDatabases(List<Long> dataSourceIds) {
        if (CollectionUtils.isEmpty(dataSourceIds)) {
            return Lists.newArrayList();
        }

        String whereClause = "kmd.database_name is not null and kmd.datasource_id in (" + StringUtils.repeat("?", ",", dataSourceIds.size()) + ")";
        SQLBuilder preSql = DefaultSQLBuilder.newBuilder()
                .select("distinct kmd.database_name")
                .from("kun_mt_dataset kmd")
                .where(whereClause);
        String finalSql = preSql.getSQL();
        return dbOperator.fetchAll(finalSql, rs -> {
            String databaseName = rs.getString("database_name");
            return new DatabaseBaseInfo(databaseName);
        }, dataSourceIds.toArray());
    }


    public DatasetDetail getDatasetDetail(Long id) {
        String sql = "select kmd.*, " +
                "kmdsrc.datasource_type as type, " +
                "kmdsrca.name as datasource_name, " +
                "kmda.description as dataset_description, " +
                "string_agg(distinct(kmdo.owner), ',') as owners, " +
                "string_agg(distinct(kmdt.tag), ',') as tags, " +
                "watermark.high_watermark as high_watermark, " +
                "watermark.low_watermark as low_watermark," +
                "kmd.deleted as deleted\n" +
                "from kun_mt_dataset kmd\n" +
                "         inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "         inner join kun_mt_datasource_attrs kmdsrca on kmdsrca.datasource_id = kmdsrc.id\n" +
                "         left join kun_mt_dataset_attrs kmda on kmd.gid = kmda.dataset_gid\n" +
                "         left join kun_mt_dataset_owners kmdo on kmd.gid = kmdo.dataset_gid\n" +
                "         left join kun_mt_dataset_tags kmdt on kmd.gid = kmdt.dataset_gid\n" +
                "         left join (select dataset_gid, max(last_updated_time) as high_watermark, min(last_updated_time) as low_watermark from kun_mt_dataset_stats group by dataset_gid) watermark on watermark.dataset_gid = kmd.gid\n";

        String whereClause = "where kmd.gid = ?\n";
        String groupByClause = "group by kmd.gid, type, datasource_name, dataset_description, high_watermark, low_watermark";

        sql = sql + whereClause + groupByClause;
        DatasetDetail datasetDetail = dbOperator.fetchOne(sql, rs -> {
            DatasetDetail detail = new DatasetDetail();
            setDatasetBasicField(detail, rs);
            detail.setDeleted(rs.getBoolean("deleted"));

            return detail;
        }, id);
        if (Objects.nonNull(datasetDetail)) {
            datasetDetail.setRowCount(getRowCount(id));
        }
        return datasetDetail;
    }

    public Long getRowCount(Long gid) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("row_count")
                .from("kun_mt_dataset_stats")
                .where("dataset_gid = ?")
                .orderBy("stats_date desc")
                .limit(1)
                .getSQL();

        return dbOperator.fetchOne(sql, rs -> rs.getLong("row_count"), gid);
    }

    public void updateDataset(Long id, DatasetUpdateRequest updateRequest) {
        String sql = "insert into kun_mt_dataset_attrs values (?, ?) " +
                "on conflict (dataset_gid) " +
                "do update set description = ?";
        dbOperator.update(sql, id, updateRequest.getDescription(), updateRequest.getDescription());
        overwriteOwners(id, updateRequest.getOwners());
        tagDao.save(updateRequest.getTags());
        tagDao.overwriteDatasetTags(id, updateRequest.getTags());
    }

    public void overwriteOwners(Long gid, List<String> owners) {
        String sql = "delete from kun_mt_dataset_owners where dataset_gid = ?";
        dbOperator.update(sql, gid);

        if (CollectionUtils.isEmpty(owners)) {
            return;
        }

        String addOwnerRefSql = "insert into kun_mt_dataset_owners values (?, ?, ?)";
        for (String owner : owners) {
            if (StringUtils.isBlank(owner)) {
                continue;
            }

            dbOperator.update(addOwnerRefSql, IdGenerator.getInstance().nextId(), gid, owner);
        }
    }

    public DatasetFieldPageInfo searchDatasetFields(Long id, DatasetColumnSearchRequest searchRequest) {
        Long rowCount = getRowCount(id);

        DatasetFieldPageInfo datasetFieldPage = new DatasetFieldPageInfo();
        StringBuilder sqlBuilder = new StringBuilder(
                "select distinct on (name, id) kmdf.id as id, kmdf.name as name, kmdf.type as type, kmdf.description as description, \n" +
                        "stats.distinct_count as distinct_count, stats.nonnull_count as nonnull_count, stats.stats_date as stats_date\n" +
                        "from kun_mt_dataset_field kmdf\n" +
                        "left join kun_mt_dataset_field_stats stats\n" +
                        "on kmdf.id = stats.field_id\n"
        );

        Pair<String, List<Object>> whereClauseAndParams = getWhereClauseAndArgumentsForFindByDatasetGid(id, searchRequest);
        String whereClause = whereClauseAndParams.getLeft();
        List<Object> preparedStmtArgs = whereClauseAndParams.getRight();
        sqlBuilder.append(whereClause);

        String totalCountSql = "select count(*) as total_count from kun_mt_dataset_field kmdf " + whereClause;
        Integer totalCount = dbOperator.fetchOne(totalCountSql, rs -> rs.getInt("total_count"), preparedStmtArgs.toArray());

        String orderByClause = "order by kmdf.name asc, kmdf.id desc, stats.stats_date desc\n";
        String limitSql = toLimitSql(searchRequest.getPageNumber(), searchRequest.getPageSize());

        sqlBuilder.append(orderByClause);
        sqlBuilder.append(limitSql);

        String sql = sqlBuilder.toString();

        List<DatasetFieldInfo> datasetFields = dbOperator.fetchAll(sql, rs -> {
            DatasetFieldInfo column = new DatasetFieldInfo();
            column.setId(rs.getLong("id"));
            column.setName(rs.getString("name"));
            column.setDescription(rs.getString("description"));
            column.setType(rs.getString("type"));
            Watermark watermark = new Watermark();
            column.setHighWatermark(watermark);
            column.setDistinctCount(rs.getLong("distinct_count"));
            column.setNotNullCount(rs.getLong("nonnull_count"));
            if (rowCount != null && !rowCount.equals(0L) && column.getNotNullCount() != null) {
                double nonnullPercentage = column.getNotNullCount() * 1.0 / rowCount;
                BigDecimal bigDecimal = BigDecimal.valueOf(nonnullPercentage);
                column.setNotNullPercentage(bigDecimal.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            return column;
        }, preparedStmtArgs.toArray());

        datasetFieldPage.setPageNumber(searchRequest.getPageNumber());
        datasetFieldPage.setPageSize(searchRequest.getPageSize());
        datasetFieldPage.setTotalCount(totalCount);
        datasetFieldPage.setColumns(datasetFields);

        return datasetFieldPage;
    }

    private Pair<String, List<Object>> getWhereClauseAndArgumentsForFindByDatasetGid(Long datasetGid, DatasetColumnSearchRequest searchRequest) {
        // init clause builder and parameter
        StringBuilder whereClauseBuilder = new StringBuilder("where kmdf.dataset_gid = ?\n");
        List<Object> whereArgs = new ArrayList<>();
        whereArgs.add(datasetGid);

        if (StringUtils.isNotEmpty(searchRequest.getKeyword())) {
            whereClauseBuilder.append("and kmdf.name ILIKE ?\n");
            whereArgs.add(toLikeSql(searchRequest.getKeyword().toUpperCase()));
        }

        return Pair.of(whereClauseBuilder.toString(), whereArgs);
    }

    public DatasetFieldInfo updateDatasetColumn(Long id, DatasetColumnUpdateRequest updateRequest) {
        String sql = "update kun_mt_dataset_field set description = ? where id = ?";
        dbOperator.update(sql, updateRequest.getDescription(), id);
        return findFieldById(id);
    }

    public DatasetFieldInfo findFieldById(Long id) {
        String sql = "select id, name, type, description from kun_mt_dataset_field where id = ?";

        return dbOperator.fetchOne(sql, rs -> {
            DatasetFieldInfo datasetField = new DatasetFieldInfo();
            if (rs.next()) {
                datasetField.setId(rs.getLong("id"));
                datasetField.setName(rs.getString("name"));
                datasetField.setType(rs.getString("type"));
                datasetField.setDescription(rs.getString("description"));
            }
            return datasetField;
        }, id);
    }

    public List<DatasetDetail> getDatasetDetailList(List<Long> idList) {
        String sql = "select kmd.*, " +
                "kmdsrc.datasource_type as type, " +
                "kmdsrca.name as datasource_name, " +
                "kmda.description as dataset_description, " +
                "string_agg(distinct(kmdo.owner), ',') as owners, " +
                "string_agg(distinct(kmdt.tag), ',') as tags, " +
                "watermark.high_watermark as high_watermark, " +
                "watermark.low_watermark as low_watermark," +
                "kmd.deleted as deleted\n" +
                "from kun_mt_dataset kmd\n" +
                "         inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "         inner join kun_mt_datasource_attrs kmdsrca on kmdsrca.datasource_id = kmdsrc.id\n" +
                "         left join kun_mt_dataset_attrs kmda on kmd.gid = kmda.dataset_gid\n" +
                "         left join kun_mt_dataset_owners kmdo on kmd.gid = kmdo.dataset_gid\n" +
                "         left join kun_mt_dataset_tags kmdt on kmd.gid = kmdt.dataset_gid\n" +
                "         left join kun_mt_dataset_stats kmds on kmd.gid = kmds.dataset_gid\n" +
                "         left join (select dataset_gid, max(last_updated_time) as high_watermark, min(last_updated_time) as low_watermark from kun_mt_dataset_stats group by dataset_gid) watermark on watermark.dataset_gid = kmd.gid\n";
        String whereClause = "where   kmd.gid in " + toColumnSql(idList.size());
        String groupByClause = "group by kmd.gid, type, datasource_name, dataset_description, high_watermark, low_watermark";

        sql = sql + whereClause + groupByClause;
        List<DatasetDetail> datasetDetailList = dbOperator.fetchAll(sql, rs -> {
            DatasetDetail dataset = new DatasetDetail();
            setDatasetBasicField(dataset, rs);
            return dataset;
        }, idList.toArray());

        if (CollectionUtils.isNotEmpty(datasetDetailList)) {
            datasetDetailList.forEach(datasetBasicInfo -> datasetBasicInfo.setRowCount(getRowCount(datasetBasicInfo.getGid())));
        }
        return datasetDetailList;
    }

    public void recordLifecycle(long gid, DatasetLifecycleStatus datasetLifecycleStatus) {
        // 记录MANAGED事件
        dbOperator.update("INSERT INTO kun_mt_dataset_lifecycle(dataset_gid, status, create_at) VALUES(?, ?, ?)", gid, datasetLifecycleStatus.name(), DateTimeUtils.now());
    }

    /**
     * Database result set mapper for {@link Dataset} object
     */
    private static class MetadataDatasetMapper implements ResultSetMapper<Dataset> {
        public static final ResultSetMapper<Dataset> INSTANCE = new MetadataDatasetMapper();

        @Override
        public Dataset map(ResultSet rs) throws SQLException {
            DataStore dataStore = DataStoreJsonUtil.toDataStore(rs.getString("data_store"));

            Dataset dataset = Dataset.newBuilder()
                    .withGid(rs.getLong("gid"))
                    .withName(rs.getString("name"))
                    .withDatasourceId(rs.getLong("datasource_id"))
                    .withDeleted(rs.getBoolean("deleted"))
                    // TODO: parse missing fields
                    .withTableStatistics(null)
                    .withFields(null)
                    .withFieldStatistics(null)
                    .withDataStore(dataStore)
                    .build();
            return dataset;
        }
    }

    public int updateStatus(long gid, Boolean deleted) {
        if (!deleted) {
            return dbOperator.update("UPDATE kun_mt_dataset SET deleted = false WHERE gid = ? AND deleted is true", gid);
        } else {
            return dbOperator.update("UPDATE kun_mt_dataset SET deleted = true WHERE gid = ? AND deleted is false", gid);
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

    public List<Dataset> getListByDatasource(Long datasourceId) {
        SQLBuilder sqlBuilder = new DefaultSQLBuilder();
        String sql = sqlBuilder.select(DATASET_COLUMNS)
                .from(DATASET_TABLE_NAME)
                .where("datasource_id = ?")
                .getSQL();
        return dbOperator.fetchAll(sql, MetadataDatasetMapper.INSTANCE, datasourceId);

    }

    private void setDatasetBasicField(DatasetBasicInfo datasetBasicInfo, ResultSet rs) throws SQLException {
        datasetBasicInfo.setGid(rs.getLong("gid"));
        datasetBasicInfo.setType(rs.getString("type"));
        datasetBasicInfo.setDatasource(rs.getString("datasource_name"));
        datasetBasicInfo.setDescription(rs.getString("dataset_description"));
        datasetBasicInfo.setName(rs.getString("name"));
        datasetBasicInfo.setDatabase(rs.getString("database_name"));
        datasetBasicInfo.setOwners(sqlToList(rs.getString("owners")));
        datasetBasicInfo.setTags(sqlToList(rs.getString("tags")));
        datasetBasicInfo.setDeleted(rs.getBoolean("deleted"));
        Watermark highWatermark = new Watermark();
        Timestamp highWatermarkTimestamp = rs.getTimestamp("high_watermark");
        if (highWatermarkTimestamp != null) {
            highWatermark.setTime(NumberUtils.toDouble(DateTimeUtils.fromTimestamp(highWatermarkTimestamp).toEpochSecond() * 1000));
        }
        datasetBasicInfo.setHighWatermark(highWatermark);

        Watermark lowWatermark = new Watermark();
        Timestamp lowWatermarkTimestamp = rs.getTimestamp("high_watermark");
        if (lowWatermarkTimestamp != null) {
            lowWatermark.setTime(NumberUtils.toDouble(DateTimeUtils.fromTimestamp(lowWatermarkTimestamp).toEpochSecond() * 1000));
        }
        datasetBasicInfo.setLowWatermark(lowWatermark);
    }

    private List<String> sqlToList(String sql) {
        if (StringUtils.isNotEmpty(sql)) {
            return Arrays.asList(sql.split(","));
        }
        return Collections.emptyList();
    }

    public String collectionToConditionSql(List<Object> pstmtArgs, Collection<?> collection) {
        if (CollectionUtils.isNotEmpty(collection)) {
            StringBuilder collectionSql = new StringBuilder("(");
            for (Object object : collection) {
                collectionSql.append("?").append(",");
                if (pstmtArgs != null) {
                    pstmtArgs.add(object);
                }
            }
            collectionSql.deleteCharAt(collectionSql.length() - 1);
            collectionSql.append(")");
            return collectionSql.toString();
        }
        return "";
    }

    public String toLikeSql(String keyword) {
        return "%" + keyword + "%";
    }

    public LocalDateTime millisToTimestamp(Long millis) {
        return ObjectUtils.defaultIfNull(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC), null);
    }

    public String toLimitSql(int pageNum, int pageSize) {
        StringBuilder pageSql = new StringBuilder();
        pageSql.append("limit ").append(pageSize).append(" offset ").append((pageNum - 1) * pageSize);
        return pageSql.toString();
    }

    public String toColumnSql(int length) {
        if (length == 0) {
            return "";
        }
        StringJoiner stringJoiner = new StringJoiner(",", "(", ")");
        for (int i = 0; i < length; i++) {
            stringJoiner.add("?");
        }
        return stringJoiner.toString();
    }

}
