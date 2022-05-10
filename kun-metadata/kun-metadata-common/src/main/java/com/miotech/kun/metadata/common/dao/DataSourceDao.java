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
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
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
    private static final String DATASOURCE_TYPE_TABLE_NAME = "kun_mt_datasource_type";
    private static final String DATASOURCE_TYPE_FIELD_TABLE_NAME = "kun_mt_datasource_type_fields";

    public static final String DATASOURCE_MODEL_NAME = "datasource";
    public static final String DATASOURCE_ATTR_MODEL_NAME = "datasource_attrs";
    public static final String DATASOURCE_TYPE_MODEL_NAME = "datasource_type";
    public static final String DATASOURCE_TYPE_FIELD_MODEL_NAME = "datasource_type_field";

    private static final String[] DATASOURCE_TABLE_COLUMNS = {"id", "connection_info", "type_id", "connection_config", "datasource_type"};
    private static final String[] DATASOURCE_ATTR_TABLE_COLUMNS = {"datasource_id", "name", "create_user", "create_time", "update_user", "update_time"};
    private static final String[] DATASOURCE_TAG_TABLE_COLUMNS = {"datasource_id", "tag"};
    private static final String[] DATASOURCE_TYPE_TABLE_COLUMNS = {"id", "name"};
    private static final String[] DATASOURCE_TYPE_FIELD_TABLE_COLUMNS = {"id", "type_id", "name", "sequence_order", "format", "require"};

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

    public DataSource fetchDataSourceByConnectionInfo(DatasourceType datasourceType, ConnectionInfo connectionInfo) {
        logger.debug("Fetch dataSourceId with type: {},connectionInfo:{}", datasourceType, connectionInfo);
        Map<String, List<String>> columnsMap = new HashMap<>();
        columnsMap.put(DATASOURCE_MODEL_NAME, Arrays.asList(DATASOURCE_TABLE_COLUMNS));
        columnsMap.put(DATASOURCE_ATTR_MODEL_NAME, Arrays.asList(DATASOURCE_ATTR_TABLE_COLUMNS));
        String dataSourceIdSQL = DefaultSQLBuilder.newBuilder()
                .columns(columnsMap)
                .autoAliasColumns()
                .from(DATASOURCE_TABLE_NAME, DATASOURCE_MODEL_NAME)
                .join("INNER", DATASOURCE_ATTR_TABLE_NAME, DATASOURCE_ATTR_MODEL_NAME)
                .on(DATASOURCE_MODEL_NAME + ".id = " + DATASOURCE_ATTR_MODEL_NAME + ".datasource_id")
                .where(DATASOURCE_MODEL_NAME + ".datasource_type = ?")
                .getSQL();

        List<DataSource> dataSourceList = dbOperator.fetchAll(dataSourceIdSQL, DataSourceResultMapper.INSTANCE, datasourceType.name());
        if (datasourceType.equals(DatasourceType.HIVE)) {
            return fetchHiveDatasource(dataSourceList);
        }
        for (DataSource dataSource : dataSourceList) {
            if (checkConnectionInfo(dataSource, connectionInfo)) {
                return dataSource;
            }
        }
        return null;
    }

    public boolean isDatasourceExist(DataSource dataSource) {
        ConnectionInfo connectionInfo = dataSource.getConnectionConfig().getDataConnection();
        DatasourceType type = dataSource.getDatasourceType();
        String dataSourceIdSQL = DefaultSQLBuilder.newBuilder()
                .select(DATASOURCE_TABLE_COLUMNS)
                .from(DATASOURCE_TABLE_NAME)
                .where("datasource_type = ?")
                .asPrepared()
                .getSQL();
        List<DataSource> dataSourceList = dbOperator.fetchAll(dataSourceIdSQL, DataSourceBasicMapper.INSTANCE, type.name());
        for (DataSource savedSource : dataSourceList) {
            if (checkConnectionInfo(savedSource, connectionInfo)) {
                return true;
            }
        }
        return false;
    }

    private DataSource fetchHiveDatasource(List<DataSource> dataSources) {
        DataSource athena = null;
        for (DataSource dataSource : dataSources) {
            ConnectionType connectionType = dataSource.getConnectionConfig().getDataConnection().getConnectionType();
            if (connectionType.equals(ConnectionType.HIVE_SERVER)) {
                return dataSource;
            }
            if (connectionType.equals(ConnectionType.ATHENA)) {
                athena = dataSource;
            }
        }
        return athena;
    }

    private boolean checkConnectionInfo(DataSource dataSource, ConnectionInfo searchConnectionInfo) {
        logger.debug("check datasource : {},searchConnectionInfo:{}", dataSource, searchConnectionInfo);
        ConnectionInfo dataSourceConnectionInfo = dataSource.getConnectionConfig().getDataConnection();
        //hashcode and equals method of connectionInfo should ignore user and password
        return dataSourceConnectionInfo.equals(searchConnectionInfo);
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

        //compatible old data
        ConnectionInfoV1 connectionInfoV1 = generateConnectionInfoV1(dataSource);
        Long typeId = generateTypeId(dataSource);

        dbOperator.transaction(() -> {
                    String insertDataSourceSQL = DefaultSQLBuilder.newBuilder()
                            .insert(DATASOURCE_TABLE_COLUMNS)
                            .into(DATASOURCE_TABLE_NAME)
                            .asPrepared()
                            .getSQL();
                    dbOperator.update(insertDataSourceSQL,
                            dataSource.getId(),
                            JSONUtils.toJsonString(connectionInfoV1.getValues()),
                            typeId,
                            JSONUtils.toJsonString(dataSource.getConnectionConfig()),
                            dataSource.getDatasourceType().name());

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

        //compatible old data
        ConnectionInfoV1 connectionInfoV1 = generateConnectionInfoV1(dataSource);
        Long typeId = generateTypeId(dataSource);

        dbOperator.transaction(() -> {
                    String insertDataSourceSQL = DefaultSQLBuilder.newBuilder()
                            .update(DATASOURCE_TABLE_NAME)
                            .set("connection_info","type_id","connection_config", "datasource_type")
                            .where("id = ?")
                            .asPrepared()
                            .getSQL();
                    dbOperator.update(insertDataSourceSQL,
                            JSONUtils.toJsonString(connectionInfoV1.getValues()),
                            typeId,
                            JSONUtils.toJsonString(dataSource.getConnectionConfig()),
                            dataSource.getDatasourceType().name(), dataSource.getId());

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

    public List<DatasourceTemplate> getAllTypes() {
        String dataSourceTypeSQL = DefaultSQLBuilder.newBuilder()
                .select(DATASOURCE_TYPE_TABLE_COLUMNS)
                .from(DATASOURCE_TYPE_TABLE_NAME)
                .getSQL();
        List<DatasourceTemplate> datasourceTemplates = dbOperator.fetchAll(dataSourceTypeSQL, DataSourceTemplateMapper.INSTANCE);
        return datasourceTemplates;
    }

    private static class DataSourceResultMapper implements ResultSetMapper<DataSource> {
        public static final DataSourceDao.DataSourceResultMapper INSTANCE = new DataSourceDao.DataSourceResultMapper();

        @Override
        public DataSource map(ResultSet rs) throws SQLException {
            ConnectionInfoV1 connectionInfoV1 = new ConnectionInfoV1(JSONUtils.jsonStringToMap(rs.getString(DATASOURCE_MODEL_NAME + "_connection_info"), String.class, Object.class));
            ConnectionConfig connectionConfig = JSONUtils.jsonToObject(rs.getString(DATASOURCE_MODEL_NAME + "_connection_config"), ConnectionConfig.class);
            connectionConfig.setValues(connectionInfoV1.getValues());
            return DataSource.newBuilder()
                    .withId(rs.getLong(DATASOURCE_MODEL_NAME + "_id"))
                    .withName(rs.getString(DATASOURCE_ATTR_MODEL_NAME + "_name"))
                    .withTypeId(rs.getLong(DATASOURCE_MODEL_NAME + "_type_id"))
                    .withConnectionConfig(connectionConfig)
                    .withDatasourceType(DatasourceType.valueOf(rs.getString(DATASOURCE_MODEL_NAME + "_datasource_type")))
                    .withCreateUser(rs.getString(DATASOURCE_ATTR_MODEL_NAME + "_create_user"))
                    .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(DATASOURCE_ATTR_MODEL_NAME + "_create_time")))
                    .withUpdateUser(rs.getString(DATASOURCE_ATTR_MODEL_NAME + "_update_user"))
                    .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(DATASOURCE_ATTR_MODEL_NAME + "_update_time")))
                    .build();
        }
    }

    private static class DataSourceBasicMapper implements ResultSetMapper<DataSource> {
        public static final DataSourceDao.DataSourceBasicMapper INSTANCE = new DataSourceDao.DataSourceBasicMapper();

        @Override
        public DataSource map(ResultSet rs) throws SQLException {
            return DataSource.newBuilder()
                    .withId(rs.getLong("id"))
                    .withConnectionConfig(JSONUtils.jsonToObject(rs.getString("connection_config"), ConnectionConfig.class))
                    .withDatasourceType(DatasourceType.valueOf(rs.getString("datasource_type")))
                    .build();
        }
    }

    private static class DataSourceTemplateMapper implements ResultSetMapper<DatasourceTemplate> {
        public static final DataSourceDao.DataSourceTemplateMapper INSTANCE = new DataSourceDao.DataSourceTemplateMapper();


        @Override
        public DatasourceTemplate map(ResultSet rs) throws SQLException {
            return DatasourceTemplate.newBuilder()
                    .withType(rs.getString("name"))
                    .withId(rs.getLong("id"))
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


    /**
     * just used to compatible old data
     * will be removed after discovery refactor
     */
    private Long generateTypeId(DataSource dataSource){
        DatasourceType datasourceType = dataSource.getDatasourceType();
        ConnectionInfo userConnection = dataSource.getConnectionConfig().getUserConnection();
        ConnectionType userConnectionType = userConnection.getConnectionType();
        switch (datasourceType){
            case POSTGRESQL:
                return 3l;
            case MONGODB:
                return 2l;
            case ARANGO:
                return 5l;
            case ELASTICSEARCH:
                return 4l;
            case MYSQL:
                return 7l;
            case HIVE:
                if(userConnectionType.equals(ConnectionType.ATHENA)){
                    return 1l;
                }else {
                    return 6l;
                }
            default:
                throw new IllegalStateException(datasourceType + " is not supported");
        }
    }

    /**
     * just used to compatible old data
     * will be removed after discovery refactor
     */
    private ConnectionInfoV1 generateConnectionInfoV1(DataSource dataSource) {
        Map<String, Object> oldConfig = new HashMap<>();
        ConnectionConfig connectionConfig = dataSource.getConnectionConfig();
        ConnectionInfo userConnection = connectionConfig.getUserConnection();
        ConnectionInfo metaConnection = connectionConfig.getMetadataConnection();
        ConnectionInfo storageConnection = connectionConfig.getStorageConnection();
        if (dataSource.getDatasourceType().equals(DatasourceType.HIVE)) {
            if (userConnection instanceof HiveServerConnectionInfo) {
                oldConfig.put("dataStoreHost", ((HiveServerConnectionInfo) userConnection).getHost());
                oldConfig.put("dataStorePort", ((HiveServerConnectionInfo) userConnection).getPort());
                oldConfig.put("dataStoreUsername", ((HiveServerConnectionInfo) userConnection).getUsername());
                oldConfig.put("dataStorePassword", ((HiveServerConnectionInfo) userConnection).getPassword());
                if (metaConnection != null && metaConnection instanceof HiveMetaStoreConnectionInfo) {
                    oldConfig.put("metaStoreUris", ((HiveMetaStoreConnectionInfo) metaConnection).getMetaStoreUris());
                }
            } else if (userConnection instanceof AthenaConnectionInfo) {
                oldConfig.put("athenaUrl", ((AthenaConnectionInfo) userConnection).getAthenaUrl());
                oldConfig.put("athenaUsername", ((AthenaConnectionInfo) userConnection).getAthenaUsername());
                oldConfig.put("athenaPassword", ((AthenaConnectionInfo) userConnection).getAthenaPassword());
                if (metaConnection != null && metaConnection instanceof GlueConnectionInfo) {
                    oldConfig.put("glueRegion", ((GlueConnectionInfo) metaConnection).getGlueRegion());
                    oldConfig.put("glueAccessKey", ((GlueConnectionInfo) metaConnection).getGlueAccessKey());
                    oldConfig.put("glueSecretKey", ((GlueConnectionInfo) metaConnection).getGlueSecretKey());
                }
                if (storageConnection != null && storageConnection instanceof S3ConnectionInfo) {
                    oldConfig.put("s3Region", ((S3ConnectionInfo) storageConnection).getS3Region());
                    oldConfig.put("s3AccessKey", ((S3ConnectionInfo) storageConnection).getS3AccessKey());
                    oldConfig.put("s3SecretKey", ((S3ConnectionInfo) storageConnection).getS3SecretKey());
                }
            }
        } else if (userConnection instanceof PostgresConnectionInfo) {
            PostgresConnectionInfo connection = (PostgresConnectionInfo) userConnection;
            oldConfig.put("host", connection.getHost());
            oldConfig.put("port", connection.getPort());
            oldConfig.put("username", connection.getUsername());
            oldConfig.put("password", connection.getPassword());
        }

        return new ConnectionInfoV1(oldConfig);
    }


}
