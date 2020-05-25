package com.miotech.kun.metadata.entrance;

import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.impl.HiveExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class ClusterETLEntrance implements Entrance {
    private static Logger logger = LoggerFactory.getLogger(ClusterETLEntrance.class);

    private long clusterId;
    private final String url;
    private final String username;
    private final String password;

    public ClusterETLEntrance(long clusterId, String url, String username, String password) {
        this.clusterId = clusterId;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public void start() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.POSTGRES, url, username, password);
            String sql = "SELECT `type`, url, username, password FROM cluster WHERE id = ?";
            statement = connection.prepareStatement(sql);
            statement.setLong(1, clusterId);
            resultSet = statement.executeQuery();

            String type;
            String url;
            String username;
            String password;
            while (resultSet.next()) {
                type = resultSet.getString(1);
                url = resultSet.getString(2);
                username = resultSet.getString(3);
                password = resultSet.getString(4);
            }

//            new HiveExtractor().extract();

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
}
