package com.miotech.kun.metadata.databuilder.extract.template;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
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
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class DataWarehouseStatTemplate {
    private static Logger logger = LoggerFactory.getLogger(DataWarehouseStatTemplate.class);

    private final String dbName;
    private final String dbNameAfterEscape;
    private final String schemaName;
    private final ConnectionType connectionType;
    private final String tableName;
    private final String tableNameAfterEscape;
    private final Connector connector;
    private static final String PREFIX_COUNT_STRING = "count_";
    private static final String PREFIX_DIS_COUNT_STRING = "dis_count_";
    public static final Set ignoreTypeSet = ImmutableSet.of(DatasetFieldType.Type.ARRAY, DatasetFieldType.Type.STRUCT);
    public static final Set ignoreRowTypeSet = ImmutableSet.of("array<string>");

    public DataWarehouseStatTemplate(String dbName, String schemaName, String tableName, DataSource dataSource) {
        connectionType = dataSource.getDatasourceConnection().getDataConnection().getConnectionType();
        this.dbName = dbName;
        this.dbNameAfterEscape = DatabaseIdentifierProcessor.escape(dbName, connectionType);
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.tableNameAfterEscape = DatabaseIdentifierProcessor.escape(tableName, connectionType);
        this.connector = ConnectorFactory.generateConnector(dataSource);
    }

    public List<FieldStatistics> getFieldStats(List<DatasetField> datasetFieldList) {
        List<FieldStatistics> resultFieldStatistics = Lists.newArrayList();
        String countSql = concatCountSql(datasetFieldList, connectionType);
        if (logger.isDebugEnabled()) {
            logger.debug("DataWarehouseStatTemplate getFieldStats start. database: {}, table: {}, datasetField: {}", dbName,
                    tableName, JSONUtils.toJsonString(datasetFieldList));
        }
        ResultSet resultSet = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("DataWarehouseStatTemplate getFieldStats countsql sql: {}", countSql);
            }
            Query query = new Query(dbName, schemaName, countSql);
            resultSet = connector.query(query);
            while (resultSet.next()) {

                for (int i = 0; i < datasetFieldList.size(); i++) {
                    FieldStatistics statistics = FieldStatistics.newBuilder()
                            .withFieldName(datasetFieldList.get(i).getName())
                            .withStatDate(DateTimeUtils.now())
                            .withNonnullCount(resultSet.getLong(i * 2 + 1))
                            .withDistinctCount(resultSet.getLong((i + 1) * 2))
                            .build();
                    resultFieldStatistics.add(statistics);
                }
            }
            return resultFieldStatistics;
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public List<FieldStatistics> extractFieldStatistics(List<DatasetField> datasetFieldList) {
        List<FieldStatistics> baseFieldStatistics = Lists.newArrayList();
        List<FieldStatistics> ignoredFieldStatistics = datasetFieldList.stream()
                .filter(datasetField -> isIgnored(datasetField.getFieldType()))
                .map(datasetField -> FieldStatistics.newBuilder().withFieldName(datasetField.getName()).withStatDate(DateTimeUtils.now()).withDistinctCount(0L).withNonnullCount(0L).build())
                .collect(Collectors.toList());
        baseFieldStatistics.addAll(ignoredFieldStatistics);
        List<DatasetField> datasetFields = datasetFieldList.stream()
                .filter(datasetField -> !isIgnored(datasetField.getFieldType()))
                .collect(Collectors.toList());
        List<FieldStatistics> notIgnoredResultFieldStatistics = getFieldStats(datasetFields);
        baseFieldStatistics.addAll(notIgnoredResultFieldStatistics);
        return baseFieldStatistics;
    }


    private String concatCountSql(List<DatasetField> datasetFields, ConnectionType connectionType) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT ");
        StringJoiner columnJoiner = new StringJoiner(",");
        StringBuilder tableSqlBuilder = new StringBuilder(" FROM ");
        switch (connectionType) {
            case ATHENA:
                datasetFields.forEach(datasetField -> {
                    String fieldName = datasetField.getName();
                    columnJoiner.add(String.format("COUNT(%s) AS %s", getColumnName(fieldName), getCountColumnName(fieldName)));
                    columnJoiner.add(String.format("APPROX_DISTINCT(cast(%s as varchar)) AS %s", getColumnName(fieldName), getDisColumnName(fieldName)));
                });
                tableSqlBuilder.append(dbNameAfterEscape).append(".").append(tableNameAfterEscape);
                return sqlBuilder.append(columnJoiner).append(tableSqlBuilder).toString();
            case HIVE_THRIFT:
                datasetFields.forEach(datasetField -> {
                    String fieldName = datasetField.getName();
                    columnJoiner.add(String.format("COUNT(%s) AS %s", getColumnName(fieldName), getCountColumnName(fieldName)));
                    columnJoiner.add(String.format("APPROX_COUNT_DISTINCT(%s) AS %s", getColumnName(fieldName), getDisColumnName(fieldName)));
                });
                tableSqlBuilder.append(dbNameAfterEscape).append(".").append(tableNameAfterEscape);
                return sqlBuilder.append(columnJoiner).append(tableSqlBuilder).toString();
            case POSTGRESQL:
                datasetFields.forEach(datasetField -> {
                    String fieldName = datasetField.getName();
                    columnJoiner.add(String.format("COUNT(%s) AS %s", getColumnName(fieldName), getCountColumnName(fieldName)));
                    if (isSpecialType(datasetField.getFieldType().getType())) {
                        columnJoiner.add(String.format("COUNT(DISTINCT(CAST(%s AS VARCHAR))) AS %s", getColumnName(fieldName), getDisColumnName(fieldName)));
                    } else {
                        columnJoiner.add(String.format("COUNT(DISTINCT(%s)) AS %s", getColumnName(fieldName), getDisColumnName(fieldName)));
                    }
                });
                tableSqlBuilder.append(tableNameAfterEscape);
                return sqlBuilder.append(columnJoiner).append(tableSqlBuilder).toString();
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
            Query query = new Query(dbName, schemaName, rowCountSQL);
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

    private boolean isIgnored(DatasetFieldType type) {
        return ignoreTypeSet.contains(type.getType()) || ignoreRowTypeSet.contains(type.getRawType());

    }

    private boolean isSpecialType(DatasetFieldType.Type type) {
        return type == DatasetFieldType.Type.JSON;
    }

    private String getDisColumnName(String fieldName) {
        return DatabaseIdentifierProcessor.escape(PREFIX_DIS_COUNT_STRING + fieldName, connectionType);
    }

    private String getCountColumnName(String fieldName) {
        return DatabaseIdentifierProcessor.escape(PREFIX_COUNT_STRING + fieldName, connectionType);
    }

    private String getColumnName(String fieldName) {
        return DatabaseIdentifierProcessor.escape(fieldName, connectionType);
    }
}