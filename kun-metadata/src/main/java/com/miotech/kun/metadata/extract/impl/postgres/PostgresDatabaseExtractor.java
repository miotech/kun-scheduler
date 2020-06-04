package com.miotech.kun.metadata.extract.impl.postgres;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.extract.tool.UseDatabaseUtil;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.PostgresDataSource;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

public class PostgresDatabaseExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(PostgresDatabaseExtractor.class);

    private final PostgresDataSource dataSource;
    private final String database;

    public PostgresDatabaseExtractor(PostgresDataSource dataSource, String database) {
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
        this.database = database;
    }

    @Override
    public Iterator<Dataset> extract() {
        logger.debug("PostgresDatabaseExtractor extract start. dataSource: {}, database: {}",
                JSONUtils.toJsonString(dataSource), database);
        List<String> schemas = Lists.newArrayList();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(DatabaseType.POSTGRES, UseDatabaseUtil.useDatabase(dataSource.getUrl(), database),
                    dataSource.getUsername(), dataSource.getPassword());
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
        return Iterators.concat(schemas.stream().map((schema) -> new PostgresSchemaExtractor(dataSource, database, schema).extract()).iterator());
    }

}
