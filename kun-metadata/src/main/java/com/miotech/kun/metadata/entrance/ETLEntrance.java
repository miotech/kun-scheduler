package com.miotech.kun.metadata.entrance;

import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ETLEntrance implements Entrance {
    private static Logger logger = LoggerFactory.getLogger(ETLEntrance.class);

    private final String url;
    private final String username;
    private final String password;

    public ETLEntrance(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public void start() {
        List<Long> clusterIds = new ArrayList<>();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.POSTGRES, url, username, password);
            statement = connection.createStatement();

            String sql = "SELECT id FROM cluster";
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                long id = resultSet.getLong(1);
                clusterIds.add(id);
            }

            clusterIds.stream().forEach(id -> new ClusterETLEntrance(id, url, username, password).start());
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
