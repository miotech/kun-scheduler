package com.miotech.kun.metadata.extract.impl;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.PostgresDatabase;
import com.miotech.kun.metadata.models.Table;
import com.miotech.kun.workflow.core.model.entity.PostgresCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PostgresExtractor extends JDBCExtractor {
    private static Logger logger = LoggerFactory.getLogger(HiveExtractor.class);

    private PostgresCluster cluster;

    private PostgresExtractor() {};

    public PostgresExtractor(PostgresCluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public Iterator<Dataset> extract() {
        List<String> databases = new ArrayList<>();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.POSTGRES, cluster.getUrl(),
                    cluster.getUsername(), cluster.getPassword());
            String scanDatabase = "SELECT datname FROM pg_database WHERE datistemplate = FALSE";
            statement = connection.prepareStatement(scanDatabase);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String databaseName = resultSet.getString(1);
                databases.add(databaseName);
            }
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.MYSQL.getName(), classNotFoundException);
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

        return Iterators.concat(databases.stream().map((databasesName) -> new PostgresDatabaseExtractor(cluster, databasesName).extract()).iterator());
    }

}
