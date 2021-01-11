package com.miotech.kun.metadata.databuilder.extract.impl.postgres;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetExistenceExtractor;
import com.miotech.kun.metadata.databuilder.extract.tool.UseDatabaseUtil;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PostgreSQLExistenceExtractor implements DatasetExistenceExtractor {

    @Override
    public boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode) {
        PostgresDataSource postgresDataSource = (PostgresDataSource) dataSource;
        String dbName = dataset.getDatabaseName().split("\\.")[0];
        String schemaName = dataset.getDatabaseName().split("\\.")[1];

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = JDBCClient.getConnection(UseDatabaseUtil.useDatabase(postgresDataSource.getUrl(),
                    dbName), postgresDataSource.getUsername(), postgresDataSource.getPassword(), DatabaseType.POSTGRES);
            statement = connection.prepareStatement("SELECT COUNT(1) FROM pg_tables WHERE schemaname = ? and tablename = ?");

            statement.setString(1, schemaName);
            statement.setString(2, dataset.getName());

            resultSet = statement.executeQuery();
            long count = 0;
            while (resultSet.next()) {
                count = resultSet.getLong(1);
            }

            return count == 1;
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }
    }

}
