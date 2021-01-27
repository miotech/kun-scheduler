package com.miotech.kun.metadata.databuilder.extract.impl.postgresql;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.statistics.StatisticsExtractorTemplate;
import com.miotech.kun.metadata.databuilder.extract.template.DataWarehouseStatTemplate;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQLStatisticsExtractor extends StatisticsExtractorTemplate {

    @Override
    public DataWarehouseStatTemplate buildDataWarehouseStatTemplate(Dataset dataset, DataSource dataSource) {
        PostgresDataSource postgresDataSource = (PostgresDataSource) dataSource;
        String dbName = dataset.getDatabaseName().split("\\.")[0];
        String schemaName = dataset.getDatabaseName().split("\\.")[1];
        return new DataWarehouseStatTemplate(dbName, schemaName,
                dataset.getName(), DatabaseType.POSTGRES, postgresDataSource);
    }

    @Override
    public Long getTotalByteSize(Dataset dataset, DataSource dataSource) {
        PostgresDataSource postgresDataSource = (PostgresDataSource) dataSource;
        String dbName = dataset.getDatabaseName().split("\\.")[0];
        String schemaName = dataset.getDatabaseName().split("\\.")[1];
        Connection connection = null;
        Statement statisticsStatement = null;
        ResultSet statisticsResult = null;

        try {
            connection = JDBCClient.getConnection(postgresDataSource, dbName, schemaName);
            String statisticsSQL = "SELECT pg_total_relation_size(pc.oid) FROM pg_class pc JOIN pg_namespace pn on pc.relnamespace = pn.oid WHERE pc.relkind = 'r' AND pn.nspname = '%s' AND pc.relname = '%s'";
            statisticsStatement = connection.createStatement();

            statisticsResult = statisticsStatement.executeQuery(String.format(statisticsSQL, schemaName, dataset.getName()));
            long totalByteSize = 0L;
            while (statisticsResult.next()) {
                totalByteSize = statisticsResult.getLong(1);
            }

            return totalByteSize;
        } catch (SQLException sqlException) {
            throw ExceptionUtils.wrapIfChecked(sqlException);
        } finally {
            JDBCClient.close(connection, statisticsStatement, statisticsResult);
        }
    }

    @Override
    public boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode) {
        PostgreSQLExistenceExtractor postgreSQLExistenceExtractor = new PostgreSQLExistenceExtractor();
        return postgreSQLExistenceExtractor.judgeExistence(dataset, dataSource, DatasetExistenceJudgeMode.DATASET);
    }
}
