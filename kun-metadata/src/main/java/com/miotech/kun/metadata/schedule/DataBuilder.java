package com.miotech.kun.metadata.schedule;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.miotech.kun.metadata.extract.impl.ConfigurableExtractor;
import com.miotech.kun.metadata.extract.impl.mongo.MongoExtractor;
import com.miotech.kun.metadata.extract.impl.postgres.PostgresExtractor;
import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.load.impl.PostgresLoader;
import com.miotech.kun.metadata.model.*;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class DataBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DataBuilder.class);

    private final AtomicBoolean scheduled;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            1,
            new ThreadFactoryBuilder().setNameFormat("data-builder-scheduler").build()
    );

    private final DatabaseOperator operator;

    private final Loader loader;

    @Inject
    public DataBuilder(DatabaseOperator operator) {
        this.scheduled = new AtomicBoolean(false);
        this.operator = operator;
        this.loader = new PostgresLoader(operator);
    }

    public void build(long clusterId) {
        Preconditions.checkArgument(clusterId > 0L, "clusterId must be a positive long, clusterId: %s", clusterId);

        String sql = "SELECT kmc.id, kmct.name, kmc.connection_info FROM kun_mt_cluster kmc join kun_mt_cluster_type kmct on kmc.type_id = kmct.id WHERE kmc.id = ?";
        DataSource dataSource = operator.fetchOne(sql, rs -> buildDataSource(rs), clusterId);
        build(dataSource);
    }

    private void build(DataSource dataSource) {
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        try {
            Iterator<Dataset> datasetIterator = null;
            if (dataSource instanceof ConfigurableDataSource) {
                datasetIterator = new ConfigurableExtractor((ConfigurableDataSource) dataSource).extract();
            } else if (dataSource instanceof PostgresDataSource) {
                datasetIterator = new PostgresExtractor((PostgresDataSource) dataSource).extract();
            } else if (dataSource instanceof MongoDataSource) {
                datasetIterator = new MongoExtractor((MongoDataSource) dataSource).extract();
            }
            // TODO add others Extractor

            if (datasetIterator != null) {
                while (datasetIterator.hasNext()) {
                    try {
                        Dataset dataset = datasetIterator.next();
                        loader.load(dataset);
                    } catch (Exception e) {
                        logger.error("etl next error: ", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("start etl error: ", e);
        }

    }

    public void buildAll() {
        String sql = "SELECT kmc.id, kmct.name, kmc.connection_info FROM kun_mt_cluster kmc JOIN kun_mt_cluster_type kmct ON kmc.type_id = kmct.id";
        List<DataSource> dataSources = operator.fetchAll(sql, rs -> buildDataSource(rs));
        dataSources.stream().forEach(dataSource -> build(dataSource));
    }

    public void scheduleAtRate(long initialDelay, long period, TimeUnit unit) {
        if (scheduled.compareAndSet(false, true)) {
            logger.info("Start scheduling buildAll task. period={}, unit={}", period, unit);
            scheduler.scheduleAtFixedRate(this::buildAll, initialDelay, period, unit);
        } else {
            throw new IllegalStateException("BuildAll task is already scheduled.");
        }
    }

    private DataSource buildDataSource(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong(1);
        DataSource.Type type = DataSource.Type.valueOf(resultSet.getString(2));

        String connStr = resultSet.getString(3);
        ClusterConnection connection = JSONUtils.jsonToObject(connStr, ClusterConnection.class);

        switch (type) {
            case Configurable:
                ConfigurableDataSource.Builder configurableDataSourceBuilder = ConfigurableDataSource.newBuilder();
                configurableDataSourceBuilder.withId(id)
                        .withCatalog(convertToCatalog(connection))
                        .withQueryEngine(convertToQueryEngine(connection));
                return configurableDataSourceBuilder.build();
            case Postgres:
                PostgresDataSource.Builder postgresClusterBuilder = PostgresDataSource.newBuilder();
                postgresClusterBuilder.withId(id)
                        .withUrl(connection.getDataStoreUrl())
                        .withUsername(connection.getDataStoreUsername())
                        .withPassword(connection.getDataStorePassword());
                return postgresClusterBuilder.build();
            case Mongo:
                MongoDataSource.Builder mongoClusterBuilder = MongoDataSource.newBuilder();
                mongoClusterBuilder.withId(id)
                        .withUrl(connection.getDataStoreUrl())
                        .withUsername(connection.getDataStoreUsername())
                        .withPassword(connection.getDataStorePassword());
                return mongoClusterBuilder.build();
            //TODO add other cluster builder
            default:
                logger.error("invalid cluster type: {}", type);
                throw new RuntimeException("Invalid cluster type: " + type);
        }
    }

    private QueryEngine convertToQueryEngine(ClusterConnection connection) {
        String url = connection.getDataStoreUrl();
        String username = connection.getDataStoreUsername();
        String password = connection.getDataStorePassword();

        QueryEngine.Type queryEngineType = parseQueryEngineTypeFromUrl(url);
        switch (queryEngineType) {
            case Athena:
                AthenaQueryEngine.Builder athenaQueryEngineBuilder = AthenaQueryEngine.newBuilder();
                athenaQueryEngineBuilder.withUrl(url).withUsername(username).withPassword(password);
                return athenaQueryEngineBuilder.build();
            default:
                throw new IllegalArgumentException("Invalid QueryEngine.Type: " + queryEngineType);
        }

    }

    private QueryEngine.Type parseQueryEngineTypeFromUrl(String url) {
        String[] infos = url.split(":");
        if (infos.length <= 1) {
            throw new RuntimeException("Invalid dataStoreUrl: " + url);
        }

        switch (infos[1]) {
            case "awsathena":
                return QueryEngine.Type.Athena;
            default:
                throw new IllegalArgumentException("Unsupported data source type:" + infos[1]);
        }
    }

    private Catalog convertToCatalog(ClusterConnection connection) {
        String url = connection.getMetaStoreUrl();
        String username = connection.getMetaStoreUsername();
        String password = connection.getMetaStorePassword();

        Catalog.Type catalogType = parseCatalogTypeFromUrl(url);
        switch (catalogType) {
            case Glue:
                GlueCatalog.Builder glueCatalogBuilder = GlueCatalog.newBuilder();
                glueCatalogBuilder.withRegion(url).withAccessKey(username).withSecretKey(password);
                return glueCatalogBuilder.build();
            case MetaStore:
                MetaStoreCatalog.Builder metaStoreCatalogBuilder = MetaStoreCatalog.newBuilder();
                metaStoreCatalogBuilder.withUrl(url).withUsername(username).withPassword(password);
                return metaStoreCatalogBuilder.build();
            default:
                throw new IllegalArgumentException("Invalid Catalog.Type: " + catalogType);
        }

    }

    private Catalog.Type parseCatalogTypeFromUrl(String url) {
        if (url.startsWith("jdbc")) {
            return Catalog.Type.MetaStore;
        } else {
            return Catalog.Type.Glue;
        }
    }

}
