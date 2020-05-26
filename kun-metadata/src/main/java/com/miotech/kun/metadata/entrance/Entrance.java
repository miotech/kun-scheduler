package com.miotech.kun.metadata.entrance;

import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.impl.HiveExtractor;
import com.miotech.kun.metadata.extract.impl.PostgresExtractor;
import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.load.impl.PrintLoader;
import com.miotech.kun.metadata.model.Cluster;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.HiveCluster;
import com.miotech.kun.metadata.model.PostgresCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Entrance {
    private static Logger logger = LoggerFactory.getLogger(Entrance.class);

    private final String url;
    private final String username;
    private final String password;

    private final Loader loader;

    public Entrance(String url, String username, String password) {
        this(url, username, password, new PrintLoader());
    }

    public Entrance(String url, String username, String password, Loader loader) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.loader = loader;
    }

    public void start() {
        List<Cluster> clusters = new ArrayList<>();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.MYSQL, url, username, password);
            statement = connection.createStatement();
            String sql = "SELECT id, `type`, url, username, password FROM kun_mt_cluster";
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Cluster cluster = buildCluster(resultSet);
                clusters.add(cluster);
            }

            clusters.stream().forEach(cluster -> start(cluster));
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.POSTGRES.getName(), classNotFoundException);
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } catch (Exception e) {
            logger.error("etl error, url: {}", url, e);
            throw new RuntimeException(e);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }

    public void start(long clusterId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.POSTGRES, url, username, password);
            String sql = "SELECT id, `type`, url, username, password FROM kun_mt_cluster WHERE id = ?";
            statement = connection.prepareStatement(sql);
            statement.setLong(1, clusterId);
            resultSet = statement.executeQuery();

            Cluster cluster = null;
            while (resultSet.next()) {
                cluster = buildCluster(resultSet);
            }

            start(cluster);
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.POSTGRES.getName(), classNotFoundException);
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } catch (Exception e) {
            logger.error("etl error, url: {}", url, e);
            throw new RuntimeException(e);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }

    private void start(Cluster cluster) {
        if (cluster == null) {
            return;
        }

        if (cluster instanceof HiveCluster) {
            Iterator<Dataset> datasetIterator = new HiveExtractor((HiveCluster) cluster).extract();
            while (datasetIterator.hasNext()) {
                Dataset dataset = datasetIterator.next();
                loader.load(dataset);
            }
        } else if (cluster instanceof PostgresCluster) {
            new PostgresExtractor((PostgresCluster) cluster).extract();
        }
        // TODO add others Extractor
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
                //TODO add PostgresCluster builder
                return null;
            default:
                return null;
        }
    }

}
