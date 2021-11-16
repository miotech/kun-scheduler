package com.miotech.kun.metadata.databuilder.extract.template;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.common.connector.ConnectorFactory;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import com.miotech.kun.metadata.common.connector.Connector;
import com.miotech.kun.metadata.common.connector.Query;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.core.model.dataset.FieldStatistics;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.databuilder.extract.tool.DatabaseIdentifierProcessor;
import com.miotech.kun.metadata.databuilder.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

public class DataWarehouseStatTemplate {
    private static Logger logger = LoggerFactory.getLogger(DataWarehouseStatTemplate.class);

    private final String dbName;
    private final String dbNameAfterEscape;
    private final String schemaName;
    private final ConnectionType connectionType;
    private final String tableName;
    private final String tableNameAfterEscape;
    private final Connector connector;

    public DataWarehouseStatTemplate(String dbName, String schemaName, String tableName, DataSource dataSource) {
        connectionType =  dataSource.getConnectionConfig().getDataConnection().getConnectionType();
        this.dbName = dbName;
        this.dbNameAfterEscape = DatabaseIdentifierProcessor.escape(dbName, connectionType);
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.tableNameAfterEscape = DatabaseIdentifierProcessor.escape(tableName, connectionType);
        this.connector = ConnectorFactory.generateConnector(dataSource);
    }

    public FieldStatistics getFieldStats(DatasetField datasetField, String distinctCountSql, String nonNullCountSql) {
        if (logger.isDebugEnabled()) {
            logger.debug("DataWarehouseStatTemplate getFieldStats start. database: {}, table: {}, datasetField: {}", dbName,
                    tableName, JSONUtils.toJsonString(datasetField));
        }
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

            Query query = new Query(dbName,schemaName,nonNullCountSql);
            resultSet = connector.query(query);
            while (resultSet.next()) {
                fieldStatBuilder.withNonnullCount(resultSet.getLong(1));
            }

            return fieldStatBuilder.build();
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public FieldStatistics getFieldStats(DatasetField datasetField) {
        String fieldNameAfterEscape = DatabaseIdentifierProcessor.escape(datasetField.getName(), connectionType);

        switch (connectionType) {
            case ATHENA:
            case HIVE_THRIFT:
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
                throw new UnsupportedOperationException("Upsupported dataSourceType: " + connectionType.name());
        }
    }

    public Long getRowCount(String rowCountSQL) {
        if (logger.isDebugEnabled()) {
            logger.debug("DataWarehouseStatTemplate getRowCount start. database: {}, table: {}", dbName, tableName);
            logger.debug("DataWarehouseStatTemplate rowCountSQL: {}", rowCountSQL);
        }

        ResultSet resultSet = null;
        try {
            long rowCount = 0;
            Query query = new Query(dbName,schemaName,rowCountSQL);
            resultSet = connector.query(query);
            while (resultSet.next()) {
                rowCount = resultSet.getLong(1);
            }

            return rowCount;
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public Long getRowCount(ConnectionType type) {
        switch (type) {
            case ATHENA:
            case HIVE_THRIFT:
                String dwRowCountSQL = String.format("SELECT COUNT(*) FROM %s.%s", dbNameAfterEscape, tableNameAfterEscape);
                return getRowCount(dwRowCountSQL);
            case POSTGRESQL:
                String postgreSQLRowCountSQL = String.format("SELECT COUNT(*) FROM %s", tableName);
                return getRowCount(postgreSQLRowCountSQL);
            default:
                throw new UnsupportedOperationException("Upsupported dataSourceType: " + type.name());
        }
    }

    private boolean isIgnored(DatasetFieldType.Type type) {
        return type.equals(DatasetFieldType.Type.ARRAY) || type.equals(DatasetFieldType.Type.STRUCT);
    }

    private boolean isSpecialType(DatasetFieldType.Type type) {
        return type == DatasetFieldType.Type.JSON;
    }

}