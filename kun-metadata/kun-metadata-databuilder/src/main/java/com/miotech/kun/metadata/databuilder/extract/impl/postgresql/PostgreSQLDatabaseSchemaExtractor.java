package com.miotech.kun.metadata.databuilder.extract.impl.postgresql;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
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

public class PostgreSQLDatabaseSchemaExtractor implements Extractor {

    private final PostgresDataSource postgresDataSource;
    private final String dbName;

    public PostgreSQLDatabaseSchemaExtractor(PostgresDataSource postgresDataSource, String dbName) {
        Preconditions.checkNotNull(postgresDataSource, "dataSource should not be null.");
        this.postgresDataSource = postgresDataSource;
        this.dbName = dbName;
    }

    @Override
    public Iterator<Dataset> extract() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(UseDatabaseUtil.useDatabase(postgresDataSource.getUrl(), dbName),
                    this.postgresDataSource.getUsername(), this.postgresDataSource.getPassword(), DatabaseType.POSTGRES);
            String showSchemas = "SELECT schema_name FROM information_schema.schemata WHERE schema_name NOT LIKE 'pg_%' AND schema_name != 'information_schema'";
            statement = connection.prepareStatement(showSchemas);

            resultSet = statement.executeQuery();
            List<String> schemas = Lists.newArrayList();
            while (resultSet.next()) {
                String schema = resultSet.getString(1);
                schemas.add(schema);
            }

            return Iterators.concat(schemas.stream().map(schema ->
                    new PostgreSQLSchemaInfoExtractor(postgresDataSource, dbName, schema).extract()).iterator());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }

}
