package com.miotech.kun.metadata.common.dao;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DataSourceSearchFilter;
import com.miotech.kun.metadata.core.model.datasource.DataSourceType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class DataSourceDao {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceDao.class);

    private static final String METADATA_TAG_TABLE_NAME = "kun_mt_tag";
    private static final String DATASOURCE_TABLE_NAME = "kun_mt_datasource";
    private static final String DATASOURCE_ATTR_TABLE_NAME = "kun_mt_datasource_attrs";
    private static final String DATASOURCE_TAG_TABLE_NAME = "kun_mt_datasource_tags";
    private static final String DATASOURCE_TYPE_TABLE_NAME = "kun_mt_datasource_type";
    private static final String DATASOURCE_TYPE_FIELD_TABLE_NAME = "kun_mt_datasource_type_fields";

    public static final String DATASOURCE_MODEL_NAME = "datasource";
    public static final String DATASOURCE_ATTR_MODEL_NAME = "datasource_attrs";
    public static final String DATASOURCE_TYPE_MODEL_NAME = "datasource_type";
    public static final String DATASOURCE_TYPE_FIELD_MODEL_NAME = "datasource_type_field";

    private static final String[] DATASOURCE_TABLE_COLUMNS = {"id", "connection_info", "type_id"};
    private static final String[] DATASOURCE_ATTR_TABLE_COLUMNS = {"datasource_id", "name", "create_user", "create_time", "update_user", "update_time"};
    private static final String[] DATASOURCE_TAG_TABLE_COLUMNS = {"datasource_id", "tag"};
    private static final String[] DATASOURCE_TYPE_TABLE_COLUMNS = {"id", "name"};
    private static final String[] DATASOURCE_TYPE_FIELD_TABLE_COLUMNS = {"id", "type_id", "name", "sequence_order", "format", "require"};

    @Inject
    DatabaseOperator dbOperator;

    public Optional<DataSource> findById(Long id) {
        logger.debug("Fetch dataSource with id: {}", id);
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(DATASOURCE_MODEL_NAME, Arrays.asList(DATASOURCE_TABLE_COLUMNS));
        columnsMap.put(DATASOURCE_ATTR_MODEL_NAME, Arrays.asList(DATASOURCE_ATTR_TABLE_COLUMNS));

        String dataSourceSQL = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .autoAliasColumns()
                .from(DATASOURCE_TABLE_NAME, DATASOURCE_MODEL_NAME)
                .join("INNER", DATASOURCE_ATTR_TABLE_NAME, DATASOURCE_ATTR_MODEL_NAME)
                .on(DATASOURCE_MODEL_NAME + ".id = " + DATASOURCE_ATTR_MODEL_NAME + ".datasource_id")
                .where("id = ?")
                .getSQL();

        return Optional.ofNullable(fetchDataSourceJoiningTag(dataSourceSQL, id));
    }

    public Integer fetchTotalCountWithFilter(DataSourceSearchFilter filter) {
        Pair<String, List<Object>> whereClauseAndParams = generateWhereClauseAndParamsFromFilter(filter);
        SQLBuilder sqlBuilder = new DefaultSQLBuilder();
        String dataSourcesSQL = sqlBuilder.select("COUNT(1)")
                .from(DATASOURCE_TABLE_NAME, DATASOURCE_MODEL_NAME)
                .join("INNER", DATASOURCE_ATTR_TABLE_NAME, DATASOURCE_ATTR_MODEL_NAME)
                .on(DATASOURCE_MODEL_NAME + ".id = " + DATASOURCE_ATTR_MODEL_NAME + ".datasource_id")
                .where(whereClauseAndParams.getLeft())
                .getSQL();

        return dbOperator.fetchOne(dataSourcesSQL, rs -> rs.getInt(1), whereClauseAndParams.getRight().toArray());
    }

    public List<DataSource> fetchWithFilter(DataSourceSearchFilter filter) {
        Integer pageNum = filter.getPageNum();
        Integer pageSize = filter.getPageSize();
        Integer offset = (pageNum - 1) * pageSize;

        Pair<String, List<Object>> whereClauseAndParams = generateWhereClauseAndParamsFromFilter(filter);
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(DATASOURCE_MODEL_NAME, Arrays.asList(DATASOURCE_TABLE_COLUMNS));
        columnsMap.put(DATASOURCE_ATTR_MODEL_NAME, Arrays.asList(DATASOURCE_ATTR_TABLE_COLUMNS));

        String dataSourcesSQL = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .autoAliasColumns()
                .from(DATASOURCE_TABLE_NAME, DATASOURCE_MODEL_NAME)
                .join("INNER", DATASOURCE_ATTR_TABLE_NAME, DATASOURCE_ATTR_MODEL_NAME)
                .on(DATASOURCE_MODEL_NAME + ".id = " + DATASOURCE_ATTR_MODEL_NAME + ".datasource_id")
                .where(whereClauseAndParams.getLeft())
                .orderBy(DATASOURCE_ATTR_MODEL_NAME + ".update_time DESC")
                .limit(pageSize)
                .offset(offset)
                .asPrepared()
                .getSQL();

        Collections.addAll(whereClauseAndParams.getRight(), pageSize, offset);
        return fetchDataSourcesJoiningTag(dataSourcesSQL, whereClauseAndParams.getRight().toArray());
    }

    private DataSource fetchDataSourceJoiningTag(String sql, Object... params) {
        DataSource dataSource = dbOperator.fetchOne(sql, DataSourceResultMapper.INSTANCE, params);
        if (dataSource == null) {
            return null;
        }

        List<String> tags = fetchTags(dataSource.getId());
        return dataSource.cloneBuilder()
                .withTags(tags)
                .build();
    }

    private List<String> fetchTags(Long dataSourceId) {
        String dataSourceTagSQL = DefaultSQLBuilder.newBuilder()
                .select("tag")
                .from(DATASOURCE_TAG_TABLE_NAME)
                .where("datasource_id = ?")
                .getSQL();
        return dbOperator.fetchAll(dataSourceTagSQL, rs -> rs.getString("tag"), dataSourceId);
    }

    private List<DataSource> fetchDataSourcesJoiningTag(String sql, Object... params) {
        List<DataSource> dataSources = dbOperator.fetchAll(sql, DataSourceResultMapper.INSTANCE, params);
        return dataSources.stream().map(dataSource -> {
            List<String> tags = fetchTags(dataSource.getId());
            return dataSource.cloneBuilder().withTags(tags).build();
        }).collect(Collectors.toList());
    }

    public void create(DataSource dataSource) {
        // Update tag
        updateTag(dataSource);

        dbOperator.transaction(() -> {
                    String insertDataSourceSQL = DefaultSQLBuilder.newBuilder()
                            .insert(DATASOURCE_TABLE_COLUMNS)
                            .into(DATASOURCE_TABLE_NAME)
                            .asPrepared()
                            .getSQL();
                    dbOperator.update(insertDataSourceSQL,
                            dataSource.getId(),
                            JSONUtils.toJsonString(dataSource.getConnectionInfo()),
                            dataSource.getTypeId());

                    String insertDataSourceAttrSQL = DefaultSQLBuilder.newBuilder()
                            .insert(DATASOURCE_ATTR_TABLE_COLUMNS)
                            .into(DATASOURCE_ATTR_TABLE_NAME)
                            .asPrepared()
                            .getSQL();
                    dbOperator.update(insertDataSourceAttrSQL, dataSource.getId(), dataSource.getName(), dataSource.getCreateUser(),
                            dataSource.getCreateTime(), dataSource.getUpdateUser(), dataSource.getUpdateTime());

                    if (CollectionUtils.isNotEmpty(dataSource.getTags())) {
                        String insertDataSourceTagSQL = DefaultSQLBuilder.newBuilder()
                                .insert(DATASOURCE_TAG_TABLE_COLUMNS)
                                .into(DATASOURCE_TAG_TABLE_NAME)
                                .asPrepared()
                                .getSQL();
                        Object[][] params = dataSource.getTags().stream().map(tag -> new Object[]{dataSource.getId(), tag}).toArray(Object[][]::new);
                        dbOperator.batch(insertDataSourceTagSQL, params);
                    }

                    return null;
                }
        );
    }

    public void update(DataSource dataSource) {
        // Update tag
        updateTag(dataSource);

        dbOperator.transaction(() -> {
                    String insertDataSourceSQL = DefaultSQLBuilder.newBuilder()
                            .update(DATASOURCE_TABLE_NAME)
                            .set("connection_info", "type_id")
                            .where("id = ?")
                            .asPrepared()
                            .getSQL();
                    dbOperator.update(insertDataSourceSQL, JSONUtils.toJsonString(dataSource.getConnectionInfo()), dataSource.getTypeId(), dataSource.getId());

                    String insertDataSourceAttrSQL = DefaultSQLBuilder.newBuilder()
                            .update(DATASOURCE_ATTR_TABLE_NAME)
                            .set("name", "update_user", "update_time")
                            .where("datasource_id = ?")
                            .asPrepared()
                            .getSQL();
                    dbOperator.update(insertDataSourceAttrSQL, dataSource.getName(), dataSource.getUpdateUser(), dataSource.getUpdateTime(), dataSource.getId());

                    String deleteDataSourceTagSQL = DefaultSQLBuilder.newBuilder()
                            .delete()
                            .from(DATASOURCE_TAG_TABLE_NAME)
                            .where("datasource_id = ?")
                            .getSQL();
                    dbOperator.update(deleteDataSourceTagSQL, dataSource.getId());

                    if (CollectionUtils.isNotEmpty(dataSource.getTags())) {
                        String insertDataSourceTagSQL = DefaultSQLBuilder.newBuilder()
                                .insert(DATASOURCE_TAG_TABLE_COLUMNS)
                                .into(DATASOURCE_TAG_TABLE_NAME)
                                .asPrepared()
                                .getSQL();
                        Object[][] params = dataSource.getTags().stream().map(tag -> new Object[]{dataSource.getId(), tag}).toArray(Object[][]::new);
                        dbOperator.batch(insertDataSourceTagSQL, params);
                    }

                    return null;
                }
        );
    }

    private void updateTag(DataSource dataSource) {
        if (CollectionUtils.isNotEmpty(dataSource.getTags())) {
            dataSource.getTags().stream()
                    .filter(tag -> fetchMetadataTagCount(tag) == 0L)
                    .forEach(tag -> {
                        String tagSql = "INSERT INTO kun_mt_tag VALUES(?)";
                        dbOperator.update(tagSql, tag);
                    });

        }
    }

    private long fetchMetadataTagCount(String tag) {
        String fetchTagCountSQL = DefaultSQLBuilder.newBuilder()
                .select("COUNT(1)")
                .from(METADATA_TAG_TABLE_NAME)
                .where("tag = ?")
                .getSQL();
        return dbOperator.fetchOne(fetchTagCountSQL, rs -> rs.getLong(1), tag);
    }

    public void delete(Long id) {
        String deleteDataSourceSQL = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(DATASOURCE_TABLE_NAME)
                .where("id = ?")
                .getSQL();
        dbOperator.update(deleteDataSourceSQL, id);
    }

    public List<DataSourceType> getAllTypes() {
        String dataSourceTypeSQL = DefaultSQLBuilder.newBuilder()
                .select(DATASOURCE_TYPE_TABLE_COLUMNS)
                .from(DATASOURCE_TYPE_TABLE_NAME)
                .getSQL();
        List<DataSourceType> dataSourceTypes = dbOperator.fetchAll(dataSourceTypeSQL, DataSourceTypeResultMapper.INSTANCE);
        return dataSourceTypes.stream().map(dataSourceType -> fetchTypeField(dataSourceType)).collect(Collectors.toList());
    }

    private DataSourceType fetchTypeField(DataSourceType dataSourceType) {
        String dataSourceTypeFieldSQL = DefaultSQLBuilder.newBuilder()
                .select(DATASOURCE_TYPE_FIELD_TABLE_COLUMNS)
                .from(DATASOURCE_TYPE_FIELD_TABLE_NAME)
                .where("type_id = ?")
                .getSQL();
        List<DataSourceType.Field> fields = dbOperator.fetchAll(dataSourceTypeFieldSQL, DataSourceTypeFieldResultMapper.INSTANCE, dataSourceType.getId());
        return dataSourceType.cloneBuilder().withFields(fields).build();
    }

    private static class DataSourceResultMapper implements ResultSetMapper<DataSource> {
        public static final DataSourceDao.DataSourceResultMapper INSTANCE = new DataSourceDao.DataSourceResultMapper();

        @Override
        public DataSource map(ResultSet rs) throws SQLException {
            return DataSource.newBuilder()
                    .withId(rs.getLong(DATASOURCE_MODEL_NAME + "_id"))
                    .withName(rs.getString(DATASOURCE_ATTR_MODEL_NAME + "_name"))
                    .withConnectionInfo(JSONUtils.jsonStringToMap(rs.getString(DATASOURCE_MODEL_NAME + "_connection_info")))
                    .withTypeId(rs.getLong(DATASOURCE_MODEL_NAME + "_type_id"))
                    .withCreateUser(rs.getString(DATASOURCE_ATTR_MODEL_NAME + "_create_user"))
                    .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(DATASOURCE_ATTR_MODEL_NAME + "_create_time")))
                    .withUpdateUser(rs.getString(DATASOURCE_ATTR_MODEL_NAME + "_update_user"))
                    .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(DATASOURCE_ATTR_MODEL_NAME + "_update_time")))
                    .build();
        }
    }

    private static class DataSourceTypeResultMapper implements ResultSetMapper<DataSourceType> {
        public static final DataSourceDao.DataSourceTypeResultMapper INSTANCE = new DataSourceDao.DataSourceTypeResultMapper();

        @Override
        public DataSourceType map(ResultSet rs) throws SQLException {
            return DataSourceType.newBuilder()
                    .withId(rs.getLong("id"))
                    .withName(rs.getString("name"))
                    .build();
        }
    }

    private static class DataSourceTypeFieldResultMapper implements ResultSetMapper<DataSourceType.Field> {
        public static final DataSourceDao.DataSourceTypeFieldResultMapper INSTANCE = new DataSourceDao.DataSourceTypeFieldResultMapper();

        @Override
        public DataSourceType.Field map(ResultSet rs) throws SQLException {
            return new DataSourceType.Field(rs.getString("format"),
                    rs.getBoolean("require"),
                    rs.getString("name"),
                    rs.getInt("sequence_order"));
        }
    }

    private Pair<String, List<Object>> generateWhereClauseAndParamsFromFilter(DataSourceSearchFilter filters) {
        Preconditions.checkNotNull(filters, "Invalid argument `filters`: null");

        String whereClause = "(1 = 1)";
        List<Object> params = new ArrayList<>();

        boolean filterHasKeyword = StringUtils.isNotBlank(filters.getName());

        if (filterHasKeyword) {
            whereClause = "(" + DATASOURCE_ATTR_MODEL_NAME + ".name LIKE CONCAT('%', CAST(? AS TEXT), '%'))";
            params.add(filters.getName());
        }

        return Pair.of(whereClause, params);
    }

}
