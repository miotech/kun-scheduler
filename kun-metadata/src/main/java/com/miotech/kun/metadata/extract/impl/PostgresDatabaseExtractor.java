package com.miotech.kun.metadata.extract.impl;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.extract.tool.UseDatabaseUtil;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.PostgresCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PostgresDatabaseExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(PostgresDatabaseExtractor.class);

    private final PostgresCluster cluster;
    private final String database;

    public PostgresDatabaseExtractor(PostgresCluster cluster, String database) {
        this.cluster = cluster;
        this.database = database;
    }

    @Override
    public Iterator<Dataset> extract() {
        List<String> schemas = new ArrayList<>();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.POSTGRES, UseDatabaseUtil.useDatabase(cluster.getUrl(), database),
                    cluster.getUsername(), cluster.getPassword());
            String scanDatabase = "SELECT SCHEMA_NAME FROM information_schema.schemata WHERE SCHEMA_NAME NOT LIKE 'pg_%' AND SCHEMA_NAME != 'information_schema'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(scanDatabase);

            while (resultSet.next()) {
                String schema = resultSet.getString(1);
                schemas.add(schema);
            }
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.POSTGRES.getName(), classNotFoundException);
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

        return Iterators.concat(schemas.stream().map((schema) -> new PostgresSchemaExtractor(cluster, database, schema).extract()).iterator());
    }

}
