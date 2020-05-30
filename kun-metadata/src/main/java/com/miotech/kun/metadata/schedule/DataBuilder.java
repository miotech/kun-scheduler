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

        String sql = "SELECT id, type, url, username, password FROM kun_mt_cluster WHERE id = ?";
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
                    Dataset dataset = datasetIterator.next();
                    loader.load(dataset);
                }
            }
        } catch (Exception e) {
            logger.error("start etl error: ", e);
        }
    }

    public void buildAll() {
        String sql = "SELECT id, type, url, username, password FROM kun_mt_cluster";
        List<Cluster> clusters = operator.fetchAll(sql, rs -> buildCluster(rs));
        clusters.stream().forEach(cluster -> build(cluster));
    }

    public void scheduleAtRate(long period, TimeUnit unit) {
        if (scheduled.compareAndSet(false, true)) {
            logger.info("Start scheduling buildAll task. period={}, unit={}", period, unit);
            scheduler.scheduleAtFixedRate(this::buildAll, 0, period, unit);
        } else {
            throw new IllegalStateException("BuildAll task is already scheduled.");
        }
    }

    private Cluster buildCluster(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong(1);
        String type = resultSet.getString(2);
        String url = resultSet.getString(3);
        String username = resultSet.getString(4);
        String password = resultSet.getString(5);

        switch (type) {
            case "hive":
                HiveCluster.Builder hiveClusterBuilder = HiveCluster.newBuilder();
                hiveClusterBuilder.withClusterId(id)
                        .withMetaStoreUrl(url.split(";")[1])
                        .withMetaStoreUsername(username.split(";")[1])
                        .withMetaStorePassword(password.split(";")[1])
                        .withDataStoreUrl(url.split(";")[0])
                        .withDataStoreUsername(username.split(";")[0])
                        .withDataStorePassword(password.split(";")[0]);
                return hiveClusterBuilder.build();
            case "postgres":
                PostgresCluster.Builder postgresClusterBuilder = PostgresCluster.newBuilder();
                postgresClusterBuilder.withClusterId(id)
                        .withUrl(url)
                        .withUsername(username)
                        .withPassword(password);
                return postgresClusterBuilder.build();
            case "mongo":
                MongoCluster.Builder mongoClusterBuilder = MongoCluster.newBuilder();
                mongoClusterBuilder.withClusterId(id)
                        .withUrl(url)
                        .withUsername(username)
                        .withPassword(password);
                return mongoClusterBuilder.build();
            //TODO add other cluster builder
            default:
                logger.error("invalid cluster type: {}", type);
                throw new RuntimeException("invalid cluster type: " + type);
        }
    }

}
