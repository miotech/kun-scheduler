package com.miotech.kun.metadata.extract.impl.postgres;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class PostgresSchemaExtractor implements Extractor {

    private static Logger logger = LoggerFactory.getLogger(PostgresSchemaExtractor.class);

    private final PostgresDataSource dataSource;
    private final String database;
    private final String schema;

    public PostgresSchemaExtractor(PostgresDataSource dataSource, String database, String schema) {
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
        this.database = database;
        this.schema = schema;
    }

    @Override
    public Iterator<Dataset> extract() {
        List<String> tables = Lists.newArrayList();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            logger.debug("PostgresSchemaExtractor extract start. dataSource: {}, database: {}, schema: {}",
                JSONUtils.toJsonString(dataSource), database, schema);
            connection = JDBCClient.getConnection(DatabaseType.POSTGRES, UseDatabaseUtil.useDatabase(dataSource.getUrl(), database),
                    dataSource.getUsername(), dataSource.getPassword());
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
            throw ExceptionUtils.wrapIfChecked(classNotFoundException);
        } catch (SQLException sqlException) {
            throw ExceptionUtils.wrapIfChecked(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

        logger.debug("PostgresSchemaExtractor extract end. tables: {}", JSONUtils.toJsonString(tables));
        return Iterators.concat(tables.stream().map((table) -> new PostgresTableExtractor(dataSource, database, schema, table).extract()).iterator());
    }
}
