package com.miotech.kun.metadata.schedule;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.miotech.kun.metadata.extract.impl.hive.HiveExtractor;
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
        Cluster cluster = operator.fetchOne(sql, rs -> buildCluster(rs), clusterId);
        build(cluster);
    }

    private void build(Cluster cluster) {
        Preconditions.checkNotNull(cluster, "cluster should not be null.");

        try {
            Iterator<Dataset> datasetIterator = null;
            if (cluster instanceof HiveCluster) {
                datasetIterator = new HiveExtractor((HiveCluster) cluster).extract();
            } else if (cluster instanceof PostgresCluster) {
                datasetIterator = new PostgresExtractor((PostgresCluster) cluster).extract();
            } else if (cluster instanceof MongoCluster) {
                datasetIterator = new MongoExtractor((MongoCluster) cluster).extract();
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
        String sql = "SELECT kmc.id, kmct.name, kmc.connection_info FROM kun_mt_cluster kmc join kun_mt_cluster_type kmct on kmc.type_id = kmct.id";
        List<Cluster> clusters = operator.fetchAll(sql, rs -> buildCluster(rs));
        clusters.stream().forEach(cluster -> build(cluster));
    }

    public void scheduleAtRate(long initialDelay, long period, TimeUnit unit) {
        if (scheduled.compareAndSet(false, true)) {
            logger.info("Start scheduling buildAll task. period={}, unit={}", period, unit);
            scheduler.scheduleAtFixedRate(this::buildAll, initialDelay, period, unit);
        } else {
            throw new IllegalStateException("BuildAll task is already scheduled.");
        }
    }

    private Cluster buildCluster(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong(1);
        String type = resultSet.getString(2);
        String connStr = resultSet.getString(3);
        ClusterConnection connection = JSONUtils.jsonToObject(connStr, ClusterConnection.class);

        switch (type.toLowerCase()) {
            case "hive":
                HiveCluster.Builder hiveClusterBuilder = HiveCluster.newBuilder();
                hiveClusterBuilder.withClusterId(id)
                        .withDataStoreUrl(connection.getDataStoreUrl())
                        .withDataStoreUsername(connection.getDataStoreUsername())
                        .withDataStorePassword(connection.getDataStorePassword())
                        .withMetaStoreUrl(connection.getMetaStoreUrl())
                        .withMetaStoreUsername(connection.getMetaStoreUsername())
                        .withMetaStorePassword(connection.getMetaStorePassword());
                return hiveClusterBuilder.build();
            case "postgres":
                PostgresCluster.Builder postgresClusterBuilder = PostgresCluster.newBuilder();
                postgresClusterBuilder.withClusterId(id)
                        .withUrl(connection.getDataStoreUrl())
                        .withUsername(connection.getDataStoreUsername())
                        .withPassword(connection.getDataStorePassword());
                return postgresClusterBuilder.build();
            case "mongo":
                MongoCluster.Builder mongoClusterBuilder = MongoCluster.newBuilder();
                mongoClusterBuilder.withClusterId(id)
                        .withUrl(connection.getDataStoreUrl())
                        .withUsername(connection.getDataStoreUsername())
                        .withPassword(connection.getDataStorePassword());
                return mongoClusterBuilder.build();
            //TODO add other cluster builder
            default:
                logger.error("invalid cluster type: {}", type);
                throw new RuntimeException("invalid cluster type: " + type);
        }
    }

}
