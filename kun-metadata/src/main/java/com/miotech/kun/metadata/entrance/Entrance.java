package com.miotech.kun.metadata.entrance;

import com.miotech.kun.metadata.extract.impl.hive.HiveExtractor;
import com.miotech.kun.metadata.extract.impl.mongo.MongoExtractor;
import com.miotech.kun.metadata.extract.impl.postgres.PostgresExtractor;
import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.model.*;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class Entrance {
    private static Logger logger = LoggerFactory.getLogger(Entrance.class);

    private final DatabaseOperator operator;

    private final Loader loader;

    public Entrance(DatabaseOperator operator, Loader loader) {
        this.operator = operator;
        this.loader = loader;
    }

    public void start() {
        String sql = "SELECT id, `type`, url, username, password FROM kun_mt_cluster";
        List<Cluster> clusters = operator.fetchAll(sql, rs -> buildCluster(rs));
        clusters.stream().forEach(cluster -> start(cluster));
    }

    public void start(long clusterId) {
        if (clusterId <= 0) {
            throw new RuntimeException("clusterId must be a positive long, clusterId: " + clusterId);
        }

        String sql = "SELECT id, `type`, url, username, password FROM kun_mt_cluster WHERE id = ?";
        Cluster cluster = operator.fetchOne(sql, rs -> buildCluster(rs), clusterId);
        start(cluster);
    }

    private void start(Cluster cluster) {
        if (cluster == null) {
            return;
        }

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
