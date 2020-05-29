package com.miotech.kun.metadata.extract.impl.postgres;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.extract.tool.UseDatabaseUtil;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.PostgresCluster;
import com.miotech.kun.workflow.utils.JSONUtils;
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
        Preconditions.checkNotNull(cluster, "cluster should not be null.");
        this.cluster = cluster;
        this.database = database;
    }

    @Override
    public Iterator<Dataset> extract() {
        logger.debug("PostgresDatabaseExtractor extract start. cluster: {}, database: {}",
                JSONUtils.toJsonString(cluster), database);
        List<String> schemas = Lists.newArrayList();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.POSTGRES, UseDatabaseUtil.useDatabase(cluster.getUrl(), database),
                    cluster.getUsername(), cluster.getPassword());
            String scanDatabase = "SELECT schema_name FROM information_schema.schemata WHERE schema_name NOT LIKE 'pg_%' AND schema_name != 'information_schema'";
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

        logger.debug("PostgresDatabaseExtractor extract end. schemas: {}", JSONUtils.toJsonString(schemas));
        return Iterators.concat(schemas.stream().map((schema) -> new PostgresSchemaExtractor(cluster, database, schema).extract()).iterator());
    }

}
