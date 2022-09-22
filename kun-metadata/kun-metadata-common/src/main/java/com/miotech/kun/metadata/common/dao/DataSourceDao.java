package com.miotech.kun.metadata.common.dao;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.common.utils.DatasourceDsiFormatter;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.metadata.core.model.datasource.DataSourceBasicInfo;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceSearchFilter;
import com.miotech.kun.metadata.core.model.vo.DatasourceTemplate;
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

    public static final String DATASOURCE_MODEL_NAME = "datasource";
    public static final String DATASOURCE_ATTR_MODEL_NAME = "datasource_attrs";

    private static final String[] DATASOURCE_TABLE_COLUMNS = {"id", "datasource_type", "datasource_config", "dsi"};
    private static final String[] DATASOURCE_ATTR_TABLE_COLUMNS = {"datasource_id", "name", "create_user", "create_time", "update_user", "update_time"};
    private static final String[] DATASOURCE_TAG_TABLE_COLUMNS = {"datasource_id", "tag"};
    private static final String[] DATASOURCE_TYPE_TABLE_COLUMNS = {"id", "name"};

    @Inject
    DatabaseOperator dbOperator;

    public List<Long> fetchDataSourceIdByType(String typeName) {
        logger.debug("Fetch dataSourceId with type: {}", typeName);
        String dataSourceIdSQL = DefaultSQLBuilder.newBuilder()
                .select("id")
                .from(DATASOURCE_TABLE_NAME)
                .where("datasource_type = ?")
                .getSQL();
        return dbOperator.fetchAll(dataSourceIdSQL, rs -> rs.getLong(1), typeName);
    }

    public Optional<DataSourceBasicInfo> findById(Long id) {
        logger.debug("Fetch dataSourceBasicInfo with id: {}", id);
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

    public List<DataSourceBasicInfo> fetchWithFilter(DataSourceSearchFilter filter) {
        int pageNum = filter.getPageNum();
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

    public List<DataSourceBasicInfo> fetchList() {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(DATASOURCE_MODEL_NAME, Arrays.asList(DATASOURCE_TABLE_COLUMNS));
        columnsMap.put(DATASOURCE_ATTR_MODEL_NAME, Arrays.asList(DATASOURCE_ATTR_TABLE_COLUMNS));

        String dataSourcesSQL = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .autoAliasColumns()
                .from(DATASOURCE_TABLE_NAME, DATASOURCE_MODEL_NAME)
                .join("INNER", DATASOURCE_ATTR_TABLE_NAME, DATASOURCE_ATTR_MODEL_NAME)
                .on(DATASOURCE_MODEL_NAME + ".id = " + DATASOURCE_ATTR_MODEL_NAME + ".datasource_id")
                .asPrepared()
                .getSQL();

        return fetchDataSourcesJoiningTag(dataSourcesSQL);
    }

    public boolean isDatasourceExistByDsi(DataSourceBasicInfo dataSourceBasicInfo) {
        String dsi = dataSourceBasicInfo.getDsi();
        if (StringUtils.isBlank(dataSourceBasicInfo.getDsi())) {
            dsi = DatasourceDsiFormatter.getDsi(dataSourceBasicInfo.getDatasourceType(), dataSourceBasicInfo.getDatasourceConfigInfo());
        }
        String dataSourceSQL = DefaultSQLBuilder.newBuilder()
                .select("COUNT(1)")
                .from(DATASOURCE_TABLE_NAME)
                .where("dsi = ?")
                .getSQL();
        Integer count = dbOperator.fetchOne(dataSourceSQL, rs -> rs.getInt(1), dsi);
        return count > 0;
    }

    private DataSourceBasicInfo fetchDataSourceJoiningTag(String sql, Object... params) {
        DataSourceBasicInfo dataSourceBasicInfo = dbOperator.fetchOne(sql, DataSourceResultMapper.INSTANCE, params);
        if (dataSourceBasicInfo == null) {
            return null;
        }
        List<String> tags = fetchTags(dataSourceBasicInfo.getId());
        return dataSourceBasicInfo.cloneBuilder()
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

    private List<DataSourceBasicInfo> fetchDataSourcesJoiningTag(String sql, Object... params) {
        List<DataSourceBasicInfo> dataSourceBasicInfoList = dbOperator.fetchAll(sql, DataSourceResultMapper.INSTANCE, params);
        return dataSourceBasicInfoList.stream().map(dataSourceBasicInfo -> {
            List<String> tags = fetchTags(dataSourceBasicInfo.getId());
            return dataSourceBasicInfo.cloneBuilder().withTags(tags).build();
        }).collect(Collectors.toList());
    }

    public DataSourceBasicInfo create(DataSourceBasicInfo dataSourceBasicInfo) {
        // Update tag
        updateTag(dataSourceBasicInfo);

        //compatible old data
        dbOperator.transaction(() -> {
                    String insertDataSourceSQL = DefaultSQLBuilder.newBuilder()
                            .insert(DATASOURCE_TABLE_COLUMNS)
                            .into(DATASOURCE_TABLE_NAME)
                            .asPrepared()
                            .getSQL();
                    dbOperator.update(insertDataSourceSQL,
                            dataSourceBasicInfo.getId(),
                            dataSourceBasicInfo.getDatasourceType().name(),
                            JSONUtils.toJsonString(dataSourceBasicInfo.getDatasourceConfigInfo()),
                            dataSourceBasicInfo.getDsi());

                    String insertDataSourceAttrSQL = DefaultSQLBuilder.newBuilder()
                            .insert(DATASOURCE_ATTR_TABLE_COLUMNS)
                            .into(DATASOURCE_ATTR_TABLE_NAME)
                            .asPrepared()
                            .getSQL();
                    dbOperator.update(insertDataSourceAttrSQL, dataSourceBasicInfo.getId(), dataSourceBasicInfo.getName(), dataSourceBasicInfo.getCreateUser(),
                            dataSourceBasicInfo.getCreateTime(), dataSourceBasicInfo.getUpdateUser(), dataSourceBasicInfo.getUpdateTime());

                    if (CollectionUtils.isNotEmpty(dataSourceBasicInfo.getTags())) {
                        String insertDataSourceTagSQL = DefaultSQLBuilder.newBuilder()
                                .insert(DATASOURCE_TAG_TABLE_COLUMNS)
                                .into(DATASOURCE_TAG_TABLE_NAME)
                                .asPrepared()
                                .getSQL();
                        Object[][] params = dataSourceBasicInfo.getTags().stream().map(tag -> new Object[]{dataSourceBasicInfo.getId(), tag}).toArray(Object[][]::new);
                        dbOperator.batch(insertDataSourceTagSQL, params);
                    }

                    return dataSourceBasicInfo;
                }
        );
        return dataSourceBasicInfo;
    }

    public void update(DataSourceBasicInfo dataSourceBasicInfo) {

        dbOperator.transaction(() -> {
                    updateTag(dataSourceBasicInfo);
                    String insertDataSourceSQL = DefaultSQLBuilder.newBuilder()
                            .update(DATASOURCE_TABLE_NAME)
                            .set("datasource_type", "datasource_config", "dsi")
                            .where("id = ?")
                            .asPrepared()
                            .getSQL();
                    dbOperator.update(insertDataSourceSQL,
                            dataSourceBasicInfo.getDatasourceType().name(),
                            JSONUtils.toJsonString(dataSourceBasicInfo.getDatasourceConfigInfo()),
                            dataSourceBasicInfo.getDsi(),
                            dataSourceBasicInfo.getId());

                    String insertDataSourceAttrSQL = DefaultSQLBuilder.newBuilder()
                            .update(DATASOURCE_ATTR_TABLE_NAME)
                            .set("name", "update_user", "update_time")
                            .where("datasource_id = ?")
                            .asPrepared()
                            .getSQL();
                    dbOperator.update(insertDataSourceAttrSQL, dataSourceBasicInfo.getName(), dataSourceBasicInfo.getUpdateUser(), dataSourceBasicInfo.getUpdateTime(), dataSourceBasicInfo.getId());

                    String deleteDataSourceTagSQL = DefaultSQLBuilder.newBuilder()
                            .delete()
                            .from(DATASOURCE_TAG_TABLE_NAME)
                            .where("datasource_id = ?")
                            .getSQL();
                    dbOperator.update(deleteDataSourceTagSQL, dataSourceBasicInfo.getId());

                    if (CollectionUtils.isNotEmpty(dataSourceBasicInfo.getTags())) {
                        String insertDataSourceTagSQL = DefaultSQLBuilder.newBuilder()
                                .insert(DATASOURCE_TAG_TABLE_COLUMNS)
                                .into(DATASOURCE_TAG_TABLE_NAME)
                                .asPrepared()
                                .getSQL();
                        Object[][] params = dataSourceBasicInfo.getTags().stream().map(tag -> new Object[]{dataSourceBasicInfo.getId(), tag}).toArray(Object[][]::new);
                        dbOperator.batch(insertDataSourceTagSQL, params);
                    }

                    return null;
                }
        );
    }

    private void updateTag(DataSourceBasicInfo dataSourceBasicInfo) {
        if (CollectionUtils.isNotEmpty(dataSourceBasicInfo.getTags())) {
            dataSourceBasicInfo.getTags().stream()
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

    public Optional<DataSourceBasicInfo> fetchByTypeAndName(DatasourceType datasourceType, String name) {
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(DATASOURCE_MODEL_NAME, Arrays.asList(DATASOURCE_TABLE_COLUMNS));
        columnsMap.put(DATASOURCE_ATTR_MODEL_NAME, Arrays.asList(DATASOURCE_ATTR_TABLE_COLUMNS));
        String dataSourceSQL = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .autoAliasColumns()
                .from(DATASOURCE_TABLE_NAME, DATASOURCE_MODEL_NAME)
                .join("INNER", DATASOURCE_ATTR_TABLE_NAME, DATASOURCE_ATTR_MODEL_NAME)
                .on(DATASOURCE_MODEL_NAME + ".id = " + DATASOURCE_ATTR_MODEL_NAME + ".datasource_id")
                .where("datasource_type = ? AND  name=?")
                .getSQL();
        return Optional.ofNullable(fetchDataSourceJoiningTag(dataSourceSQL, datasourceType.name(), name));
    }

    private static class DataSourceResultMapper implements ResultSetMapper<DataSourceBasicInfo> {
        public static final DataSourceDao.DataSourceResultMapper INSTANCE = new DataSourceDao.DataSourceResultMapper();

        @Override
        public DataSourceBasicInfo map(ResultSet rs) throws SQLException {
            return DataSourceBasicInfo.newBuilder()
                    .withId(rs.getLong(DATASOURCE_MODEL_NAME + "_id"))
                    .withName(rs.getString(DATASOURCE_ATTR_MODEL_NAME + "_name"))
                    .withDatasourceType(DatasourceType.valueOf(rs.getString(DATASOURCE_MODEL_NAME + "_datasource_type")))
                    .withDatasourceConfigInfo(JSONUtils.jsonToObject(rs.getString(DATASOURCE_MODEL_NAME + "_datasource_config"), Map.class))
                    .withDsi(rs.getString(DATASOURCE_MODEL_NAME + "_dsi"))
                    .withCreateUser(rs.getString(DATASOURCE_ATTR_MODEL_NAME + "_create_user"))
                    .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(DATASOURCE_ATTR_MODEL_NAME + "_create_time")))
                    .withUpdateUser(rs.getString(DATASOURCE_ATTR_MODEL_NAME + "_update_user"))
                    .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(DATASOURCE_ATTR_MODEL_NAME + "_update_time")))
                    .build();
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
