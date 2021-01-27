package com.miotech.kun.metadata.databuilder.extract.impl.postgresql;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.metadata.databuilder.extract.tool.UseDatabaseUtil;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

public class PostgreSQLSchemaInfoExtractor implements Extractor {

    private final PostgresDataSource postgresDataSource;
    private final String dbName;
    private final String schemaName;

    public PostgreSQLSchemaInfoExtractor(PostgresDataSource postgresDataSource, String dbName, String schemaName) {
        Preconditions.checkNotNull(postgresDataSource, "dataSource should not be null.");
        this.postgresDataSource = postgresDataSource;
        this.dbName = dbName;
        this.schemaName = schemaName;
    }

    @Override
    public Iterator<Dataset> extract() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(UseDatabaseUtil.useDatabase(postgresDataSource.getUrl(), dbName),
                    this.postgresDataSource.getUsername(), this.postgresDataSource.getPassword(), DatabaseType.POSTGRES);
            String showTables = "SELECT tablename FROM pg_tables WHERE schemaname = ?";
            statement = connection.prepareStatement(showTables);
            statement.setString(1, schemaName);

            resultSet = statement.executeQuery();
            List<String> tables = Lists.newArrayList();
            while (resultSet.next()) {
                String table = resultSet.getString(1);
                tables.add(table);
            }

            return Iterators.concat(tables.stream().map(table ->
                    new PostgreSQLTableSchemaExtractor(postgresDataSource, dbName, schemaName, table).extract()).iterator());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }
}
