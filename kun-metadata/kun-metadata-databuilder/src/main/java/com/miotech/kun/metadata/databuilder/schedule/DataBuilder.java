package com.miotech.kun.metadata.databuilder.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.client.GlueClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.impl.arango.ArangoCollectionExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.arango.ArangoExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch.ElasticSearchIndexExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch.ElasticsearchExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.glue.GlueExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.glue.GlueTableExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.mongo.MongoCollectionExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.mongo.MongoExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.postgres.PostgresExtractor;
import com.miotech.kun.metadata.databuilder.extract.impl.postgres.PostgresTableExtractor;
import com.miotech.kun.metadata.databuilder.extract.tool.ConnectUrlUtil;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.load.impl.PostgresLoader;
import com.miotech.kun.metadata.databuilder.model.*;
import com.miotech.kun.workflow.core.model.lineage.*;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class DataBuilder {
    private static final Logger logger = LoggerFactory.getLogger(DataBuilder.class);

    private final ExecutorService threadPool = Executors.newFixedThreadPool(5);
    private final DatabaseOperator operator;
    private final Loader loader;
    private final Props props;

    @Inject
    public DataBuilder(DatabaseOperator operator, Props props) {
        this.operator = operator;
        this.loader = new PostgresLoader(operator);
        this.props = props;
    }

    public void buildAll() {
        String sql = "SELECT kmd.id, kmdt.name, kmd.connection_info FROM kun_mt_datasource kmd JOIN kun_mt_datasource_type kmdt ON kmd.type_id = kmdt.id";
        List<DataSource> dataSources = operator.fetchAll(sql, rs -> generateDataSource(rs.getLong(1), rs.getString(2), rs.getString(3)));
        CountDownLatch countDownLatch = new CountDownLatch(dataSources.size());
        for (DataSource dataSource : dataSources) {
            threadPool.submit(() -> {
                try {
                    Map<String, Long> latestStates = Maps.newHashMap();
                    build(dataSource, latestStates);
                    sweep(latestStates);
                } catch (Exception e) {
                    logger.error("DataBuilder buildAll build fail, dataSource: {}", JSONUtils.toJsonString(dataSource), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("DataBuilder buildAll await error");
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public void buildDatasource(long datasourceId) {
        try {
            Preconditions.checkArgument(datasourceId > 0L, "datasourceId must be a positive long, datasourceId: %s", datasourceId);

            String sql = "SELECT kmd.id, kmdt.name, kmd.connection_info FROM kun_mt_datasource kmd JOIN kun_mt_datasource_type kmdt ON kmd.type_id = kmdt.id WHERE kmd.id = ?";
            DataSource dataSource = operator.fetchOne(sql, rs -> generateDataSource(rs.getLong(1), rs.getString(2), rs.getString(3)), datasourceId);
            Map<String, Long> latestStates = Maps.newHashMap();
            build(dataSource, latestStates);
            sweep(latestStates);
        } catch (Exception e) {
            logger.error("DataBuilder buildDatasource build fail, dataSourceId: {}", datasourceId, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public void buildDataset(Long gid) {
        try {
            Long datasourceId = operator.fetchOne("SELECT datasource_id FROM kun_mt_dataset WHERE gid = ?", rs -> rs.getLong(1), gid);
            Preconditions.checkNotNull(datasourceId, "Invalid param `gid`: " + gid + " No corresponding datasource found");

            String sql = "SELECT kmds.id, kmdst.name, kmds.connection_info, kmd.data_store, kmd.gid FROM kun_mt_dataset kmd JOIN kun_mt_datasource kmds ON kmd.datasource_id = kmds.id JOIN kun_mt_datasource_type kmdst ON kmds.type_id = kmdst.id WHERE kmd.gid = ?";
            DatasetConnDto datasetConnDto = operator.fetchOne(sql, this::buildDatasetConnDto, gid);
            Map<String, Long> latestStates = Maps.newHashMap();
            build(datasetConnDto, latestStates);
            sweep(latestStates);
        } catch (Exception e) {
            logger.error("DataBuilder buildDataset build fail, gid: {}", gid, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private DataSource generateDataSource(long id, String datasourceType, String connStr) throws SQLException {
        DataSource.Type type = DataSource.Type.valueOf(datasourceType.toUpperCase());

        if (type.equals(DataSource.Type.AWS)) {
            AWSDataSource awsConnection = JSONUtils.jsonToObject(connStr, AWSDataSource.class);
            return AWSDataSource.clone(awsConnection).withId(id).build();
        }

        JDBCConnection jdbcConnection = JSONUtils.jsonToObject(connStr, JDBCConnection.class);
        switch (type) {
            case POSTGRESQL:
                PostgresDataSource.Builder postgresDataSourceBuilder = PostgresDataSource.newBuilder();
                postgresDataSourceBuilder.withId(id)
                        .withUrl(ConnectUrlUtil.convertToConnectUrl(jdbcConnection.getHost(), jdbcConnection.getPort(),
                                jdbcConnection.getUsername(), jdbcConnection.getPassword(), DatabaseType.POSTGRES))
                        .withUsername(jdbcConnection.getUsername())
                        .withPassword(jdbcConnection.getPassword());
                return postgresDataSourceBuilder.build();
            case MONGODB:
                MongoDataSource.Builder mongoDataSourceBuilder = MongoDataSource.newBuilder();
                mongoDataSourceBuilder.withId(id)
                        .withUrl(ConnectUrlUtil.convertToConnectUrl(jdbcConnection.getHost(), jdbcConnection.getPort(),
                                jdbcConnection.getUsername(), jdbcConnection.getPassword(), DatabaseType.MONGO))
                        .withUsername(jdbcConnection.getUsername())
                        .withPassword(jdbcConnection.getPassword());
                return mongoDataSourceBuilder.build();
            case ELASTICSEARCH:
                ElasticSearchDataSource elasticSearchDataSource = ElasticSearchDataSource.newBuilder()
                        .withId(id)
                        .withUrl(ConnectUrlUtil.convertToConnectUrl(jdbcConnection.getHost(), jdbcConnection.getPort(),
                                jdbcConnection.getUsername(), jdbcConnection.getPassword(), DatabaseType.ELASTICSEARCH))
                        .withUsername(jdbcConnection.getUsername())
                        .withPassword(jdbcConnection.getPassword())
                        .build();
                return elasticSearchDataSource;
            case ARANGO:
                ArangoDataSource arangoDataSource = ArangoDataSource.newBuilder()
                        .withId(id)
                        .withUrl(ConnectUrlUtil.convertToConnectUrl(jdbcConnection.getHost(), jdbcConnection.getPort(),
                                jdbcConnection.getUsername(), jdbcConnection.getPassword(), DatabaseType.ARANGO))
                        .withUsername(jdbcConnection.getUsername())
                        .withPassword(jdbcConnection.getPassword())
                        .build();
                return arangoDataSource;
            default:
                logger.error("Invalid datasource type: {}", type);
                throw new UnsupportedOperationException("Invalid datasource type: " + type);
        }
    }

    private DatasetConnDto buildDatasetConnDto(ResultSet resultSet) throws SQLException {
        DatasetConnDto.Builder datasetConnDtoBuilder = DatasetConnDto.newBuilder();
        datasetConnDtoBuilder.withGid(resultSet.getLong(5));

        DataSource dataSource = generateDataSource(resultSet.getLong(1), resultSet.getString(2), resultSet.getString(3));
        datasetConnDtoBuilder.withDataSource(dataSource);

        try {
            String dataStoreStr = resultSet.getString(4);
            datasetConnDtoBuilder.withDataStore(DataStoreJsonUtil.toDataStore(dataStoreStr));
        } catch (JsonProcessingException jsonProcessingException) {
            logger.error("DataStoreJsonUtil.toDataStore error: ", jsonProcessingException);
            throw ExceptionUtils.wrapIfChecked(jsonProcessingException);
        }
        return datasetConnDtoBuilder.build();
    }

    private void build(DatasetConnDto datasetConnDto, Map<String, Long> latestStates) {
        Preconditions.checkNotNull(datasetConnDto, "datasetConnDto should not be null.");
        recordStates(datasetConnDto.getDataStore(), datasetConnDto.getGid(), latestStates);
        DataSource dataSource = datasetConnDto.getDataSource();
        DataStore dataStore = datasetConnDto.getDataStore();
        Dataset dataset = null;
        if (dataSource instanceof AWSDataSource) {
            AWSDataSource awsDataSource = (AWSDataSource) dataSource;
            HiveTableStore hiveTableStore = (HiveTableStore) dataStore;
            dataset = new GlueTableExtractor(props, awsDataSource, GlueClient.searchTable(awsDataSource,
                    hiveTableStore.getDatabase(), hiveTableStore.getTable())).extract().next();
        } else if (dataSource instanceof PostgresDataSource) {
            PostgresDataSource pgDataSource = (PostgresDataSource) dataSource;
            PostgresDataStore pgDataStore = (PostgresDataStore) dataStore;
            dataset = new PostgresTableExtractor(props, pgDataSource,
                    pgDataStore.getDatabase(),
                    pgDataStore.getSchema(),
                    pgDataStore.getTableName()
            ).extract().next();
        } else if (dataSource instanceof MongoDataSource) {
            dataset = new MongoCollectionExtractor(props, ((MongoDataSource) dataSource),
                    ((MongoDataStore) dataStore).getDatabase(),
                    ((MongoDataStore) dataStore).getCollection()
            ).extract().next();
        } else if (dataSource instanceof ElasticSearchDataSource) {
            ElasticSearchDataSource elasticSearchDataSource = (ElasticSearchDataSource) dataSource;
            dataset = new ElasticSearchIndexExtractor(props, elasticSearchDataSource,
                    ((ElasticSearchIndexStore) dataStore).getIndex()
            ).extract().next();
        } else if (dataSource instanceof ArangoDataSource) {
            dataset = new ArangoCollectionExtractor(props, ((ArangoDataSource) dataSource),
                    ((ArangoCollectionStore) dataStore).getDatabase(),
                    ((ArangoCollectionStore) dataStore).getCollection()
            ).extract().next();
        }

        try {
            loader.load(dataset);
            mark(dataset, latestStates);
        } catch (Exception e) {
            logger.error("load error: ", e);
        }
    }


    private void build(DataSource dataSource, Map<String, Long> latestStates) {
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        recordStates(dataSource.getId(), latestStates);
        Iterator<Dataset> datasetIterator = null;
        if (dataSource instanceof AWSDataSource) {
            datasetIterator = new GlueExtractor(props, (AWSDataSource) dataSource).extract();
        } else if (dataSource instanceof PostgresDataSource) {
            datasetIterator = new PostgresExtractor(props, (PostgresDataSource) dataSource).extract();
        } else if (dataSource instanceof MongoDataSource) {
            datasetIterator = new MongoExtractor(props, (MongoDataSource) dataSource).extract();
        } else if (dataSource instanceof ElasticSearchDataSource) {
            ElasticSearchDataSource elasticSearchDataSource = (ElasticSearchDataSource) dataSource;
            datasetIterator = new ElasticsearchExtractor(props, elasticSearchDataSource).extract();
        } else if (dataSource instanceof ArangoDataSource) {
            datasetIterator = new ArangoExtractor(props, (ArangoDataSource) dataSource).extract();
        }

        if (datasetIterator != null) {
            while (datasetIterator.hasNext()) {
                try {
                    Dataset dataset = datasetIterator.next();
                    loader.load(dataset);

                    mark(dataset, latestStates);
                } catch (Exception e) {
                    logger.error("etl next error: ", e);
                }
            }
        }
    }

    private void recordStates(long datasourceId, Map<String, Long> latestStates) {
        operator.fetchAll("SELECT data_store, gid FROM kun_mt_dataset WHERE datasource_id = ?",
                rs -> {
                    try {
                        recordStates(DataStoreJsonUtil.toDataStore(rs.getString(1)), rs.getLong(2), latestStates);
                    } catch (JsonProcessingException jsonProcessingException) {
                        throw ExceptionUtils.wrapIfChecked(jsonProcessingException);
                    }
                    return null;
                }, datasourceId);
    }

    private void recordStates(DataStore dataStore, long gid, Map<String, Long> latestStates) {
        try {
            latestStates.put(DataStoreJsonUtil.toJson(dataStore), gid);
        } catch (JsonProcessingException jsonProcessingException) {
            throw ExceptionUtils.wrapIfChecked(jsonProcessingException);
        }
    }

    private void mark(Dataset dataset, Map<String, Long> latestStates) throws JsonProcessingException {
        if (dataset.getDataStore() == null) {
            return;
        }

        String dataStoreJson = DataStoreJsonUtil.toJson(dataset.getDataStore());
        if (latestStates.containsKey(dataStoreJson)) {
            latestStates.remove(dataStoreJson);
        }
    }

    private void sweep(Map<String, Long> latestStates) {
        if (!latestStates.isEmpty()) {
            logger.info("prepare to delete dataset: {}", JSONUtils.toJsonString(latestStates.values()));
            Object[][] params = latestStates.values().stream().map(gid -> new Object[]{gid}).toArray(Object[][]::new);
            operator.batch("DELETE FROM kun_mt_dataset WHERE gid = ?", params);
        }
    }
}
