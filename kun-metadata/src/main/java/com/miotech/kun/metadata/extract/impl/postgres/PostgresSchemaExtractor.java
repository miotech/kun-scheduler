package com.miotech.kun.metadata.extract.impl.postgres;

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

public class PostgresSchemaExtractor implements Extractor {

    private static Logger logger = LoggerFactory.getLogger(PostgresSchemaExtractor.class);

    private final PostgresCluster cluster;
    private final String database;
    private final String schema;

    public PostgresSchemaExtractor(PostgresCluster cluster, String database, String schema) {
        this.cluster = cluster;
        this.database = database;
        this.schema = schema;
    }

    @Override
    public Iterator<Dataset> extract() {
        List<String> tables = new ArrayList<>();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.POSTGRES, UseDatabaseUtil.useDatabase(cluster.getUrl(), database),
                    cluster.getUsername(), cluster.getPassword());
            String scanDatabase = "SELECT tablename FROM pg_tables WHERE schemaname = ?";
            statement = connection.prepareStatement(scanDatabase);
            statement.setString(1, schema);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String table = resultSet.getString(1);
                tables.add(table);
            }
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.POSTGRES.getName(), classNotFoundException);
            throw new RuntimeException(classNotFoundException);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

        return Iterators.concat(tables.stream().map((table) -> new PostgresTableExtractor(cluster, database, schema, table).extract()).iterator());
    }
}
