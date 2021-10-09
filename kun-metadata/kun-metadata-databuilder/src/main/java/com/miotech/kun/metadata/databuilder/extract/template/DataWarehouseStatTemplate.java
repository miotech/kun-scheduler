package com.miotech.kun.metadata.databuilder.extract.template;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.core.model.dataset.FieldStatistics;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.tool.DatabaseIdentifierProcessor;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Clock;
import java.time.LocalDateTime;

public class DataWarehouseStatTemplate {
    private static Logger logger = LoggerFactory.getLogger(DataWarehouseStatTemplate.class);

    private final String dbName;
    private final String dbNameAfterEscape;
    private final String schemaName;
    private final String schemaNameAfterEscape;

    private final String tableName;
    private final String tableNameAfterEscape;
    private final DatabaseType databaseType;
    private final Connection connection;

    public DataWarehouseStatTemplate(String dbName, String schemaName, String tableName, DatabaseType databaseType, DataSource dataSource) {
        this.dbName = dbName;
        this.dbNameAfterEscape = DatabaseIdentifierProcessor.escape(dbName, databaseType);
        this.schemaName = schemaName;
        this.schemaNameAfterEscape = DatabaseIdentifierProcessor.escape(schemaName, databaseType);
        this.tableName = tableName;
        this.tableNameAfterEscape = DatabaseIdentifierProcessor.escape(tableName, databaseType);
        this.databaseType = databaseType;
        this.connection = JDBCClient.getConnection(dataSource, dbName, schemaName);
    }

    public FieldStatistics getFieldStats(DatasetField datasetField, String distinctCountSql, String nonNullCountSql) {
        if (logger.isDebugEnabled()) {
            logger.debug("DataWarehouseStatTemplate getFieldStats start. database: {}, table: {}, datasetField: {}", dbName,
                    tableName, JSONUtils.toJsonString(datasetField));
        }
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("DataWarehouseStatTemplate getFieldStats distinctCount sql: {}", distinctCountSql);
                logger.debug("DataWarehouseStatTemplate getFieldStats nonnullCount sql: {}", nonNullCountSql);
            }

            FieldStatistics.Builder fieldStatBuilder = FieldStatistics.newBuilder();
            fieldStatBuilder.withFieldName(datasetField.getName()).withStatDate(DateTimeUtils.now());

            if (isIgnored(datasetField.getFieldType().getType())) {
                fieldStatBuilder.withDistinctCount(0L).withNonnullCount(0L);
                return fieldStatBuilder.build();
            }

            // Temporarily close distinct count
            fieldStatBuilder.withDistinctCount(0L);

            statement = connection.prepareStatement(nonNullCountSql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                fieldStatBuilder.withNonnullCount(resultSet.getLong(1));
            }

            return fieldStatBuilder.build();
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            JDBCClient.close(null, statement, resultSet);
        }
    }

    public FieldStatistics getFieldStats(DatasetField datasetField, DataSource.Type type) {
        String fieldNameAfterEscape = DatabaseIdentifierProcessor.escape(datasetField.getName(), databaseType);

        switch (type) {
            case AWS:
            case HIVE:
                String dwDistinctCountSql = String.format("SELECT COUNT(*) FROM (SELECT %s FROM %s.%s GROUP BY %s) t1",
                        fieldNameAfterEscape, dbNameAfterEscape, tableNameAfterEscape, fieldNameAfterEscape);
                String dwNonNullCountSql = String.format("SELECT COUNT(*) FROM %s.%s WHERE %s IS NOT NULL",
                        dbNameAfterEscape, tableNameAfterEscape, fieldNameAfterEscape);

                return getFieldStats(datasetField, dwDistinctCountSql, dwNonNullCountSql);
            case POSTGRESQL:
                String postgreSQLDistinctCountSQL = String.format("SELECT COUNT(DISTINCT(%s)) FROM %s", fieldNameAfterEscape,
                        tableNameAfterEscape);
                if (isSpecialType(datasetField.getFieldType().getType())) {
                    postgreSQLDistinctCountSQL = String.format("SELECT COUNT(DISTINCT(CAST(%s AS VARCHAR))) FROM %s", fieldNameAfterEscape,
                            tableNameAfterEscape);
                }
                String postgreSQLNonNullCountSql = String.format("SELECT COUNT(*) FROM %s WHERE %s IS NOT NULL",
                        tableNameAfterEscape, fieldNameAfterEscape);

                return getFieldStats(datasetField, postgreSQLDistinctCountSQL, postgreSQLNonNullCountSql);
            default:
                throw new UnsupportedOperationException("Upsupported dataSourceType: " + type.name());
        }
    }

    public Long getRowCount(String rowCountSQL) {
        if (logger.isDebugEnabled()) {
            logger.debug("DataWarehouseStatTemplate getRowCount start. database: {}, table: {}", dbName, tableName);
            logger.debug("DataWarehouseStatTemplate rowCountSQL: {}", rowCountSQL);
        }

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            long rowCount = 0;
            statement = connection.prepareStatement(rowCountSQL);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                rowCount = resultSet.getLong(1);
            }

            return rowCount;
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            JDBCClient.close(null, statement, resultSet);
        }
    }

    public Long getRowCount(DataSource.Type type) {
        switch (type) {
            case AWS:
            case HIVE:
                String dwRowCountSQL = String.format("SELECT COUNT(*) FROM %s.%s", dbNameAfterEscape, tableNameAfterEscape);
                return getRowCount(dwRowCountSQL);
            case POSTGRESQL:
                String postgreSQLRowCountSQL = String.format("SELECT COUNT(*) FROM %s", tableName);
                return getRowCount(postgreSQLRowCountSQL);
            default:
                throw new UnsupportedOperationException("Upsupported dataSourceType: " + type.name());
        }
    }

    public void close() {
        JDBCClient.close(connection);
    }

    private boolean isIgnored(DatasetFieldType.Type type) {
        return type.equals(DatasetFieldType.Type.ARRAY) || type.equals(DatasetFieldType.Type.STRUCT);
    }

    private boolean isSpecialType(DatasetFieldType.Type type) {
        return type == DatasetFieldType.Type.JSON;
    }

}