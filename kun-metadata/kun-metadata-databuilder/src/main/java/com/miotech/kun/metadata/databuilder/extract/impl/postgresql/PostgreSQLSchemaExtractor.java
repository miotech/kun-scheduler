package com.miotech.kun.metadata.databuilder.extract.impl.postgresql;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetSchemaExtractor;
import com.miotech.kun.metadata.databuilder.extract.tool.UseDatabaseUtil;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class PostgreSQLSchemaExtractor extends PostgreSQLExistenceExtractor implements DatasetSchemaExtractor {

    @Override
    public List<DatasetField> extract(Dataset dataset, DataSource dataSource) {
        PostgresDataSource postgresDataSource = (PostgresDataSource) dataSource;
        String dbName = dataset.getDatabaseName().split("\\.")[0];
        String schemaName = dataset.getDatabaseName().split("\\.")[1];
        PostgreSQLTableSchemaExtractor postgreSQLTableSchemaExtractor =
                new PostgreSQLTableSchemaExtractor(postgresDataSource, dbName, schemaName, dataset.getName());
        return postgreSQLTableSchemaExtractor.getSchema();
    }

    @Override
    public Iterator<Dataset> extract(DataSource dataSource) {
        PostgresDataSource postgresDataSource = (PostgresDataSource) dataSource;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = JDBCClient.getConnection(UseDatabaseUtil.useDatabase(postgresDataSource.getUrl(), "postgres"),
                    postgresDataSource.getUsername(), postgresDataSource.getPassword(), DatabaseType.POSTGRES);
            String showDatabases = "SELECT datname FROM pg_database WHERE datistemplate = FALSE";
            statement = connection.prepareStatement(showDatabases);
            resultSet = statement.executeQuery();

            List<String> databases = Lists.newArrayList();
            while (resultSet.next()) {
                String databaseName = resultSet.getString("datname");
                databases.add(databaseName);
            }

            return Iterators.concat(databases.stream().map(databasesName ->
                    new PostgreSQLDatabaseSchemaExtractor(postgresDataSource, databasesName).extract()).iterator());
        } catch (SQLException sqlException) {
            throw ExceptionUtils.wrapIfChecked(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }

}
