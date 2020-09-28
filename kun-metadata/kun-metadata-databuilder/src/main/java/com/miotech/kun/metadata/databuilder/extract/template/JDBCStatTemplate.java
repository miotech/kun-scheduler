package com.miotech.kun.metadata.databuilder.extract.template;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.impl.hive.HiveTableExtractor;
import com.miotech.kun.metadata.databuilder.extract.tool.DatabaseIdentifierProcessor;
import com.miotech.kun.metadata.databuilder.model.DatasetField;
import com.miotech.kun.metadata.databuilder.model.DatasetFieldStat;
import com.miotech.kun.metadata.databuilder.model.DatasetFieldType;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.LocalDateTime;

public class JDBCStatTemplate {
    private static Logger logger = LoggerFactory.getLogger(HiveTableExtractor.class);

    private final String database;
    private final String table;
    private final String tableNameWithIdentifier;
    private final DatabaseType databaseType;
    private final DatabaseOperator dbOperator;

    public JDBCStatTemplate(String database, String table, DatabaseType databaseType, DataSource dataSource) {
        this.database = DatabaseIdentifierProcessor.escape(database, databaseType);
        this.table = table;
        this.databaseType = databaseType;
        this.tableNameWithIdentifier = DatabaseIdentifierProcessor.escape(table, databaseType);
        this.dbOperator = new DatabaseOperator(dataSource);
    }

    public DatasetFieldStat getFieldStats(DatasetField datasetField) {
        if (logger.isDebugEnabled()) {
            logger.debug("HiveTableExtractor getFieldStats start. database: {}, table: {}, datasetField: {}", database,
                    table, JSONUtils.toJsonString(datasetField));
        }

        DatasetFieldStat.Builder fieldStatBuilder = DatasetFieldStat.newBuilder();
        fieldStatBuilder.withName(datasetField.getName()).withStatDate(LocalDateTime.now());

        if (isIgnored(datasetField.getFieldType().getType())) {
            fieldStatBuilder.withDistinctCount(0L).withNonnullCount(0L);
            return fieldStatBuilder.build();
        }

        /* reference: https://docs.aws.amazon.com/zh_cn/athena/latest/ug/tables-databases-columns-names.html */
        String fieldName = DatabaseIdentifierProcessor.escape(datasetField.getName(), databaseType);
        String distinctCountSql = String.format("SELECT COUNT(*) FROM (SELECT %s FROM %s.%s GROUP BY %s) t1",
                fieldName, database, tableNameWithIdentifier, fieldName);

        if (logger.isDebugEnabled()) {
            logger.debug("HiveTableExtractor getFieldStats distinctCount sql: {}", distinctCountSql);
        }
        // Temporarily close distinct count
        fieldStatBuilder.withDistinctCount(0L);

        String nonNullCountSql = String.format("SELECT COUNT(*) FROM %s.%s WHERE %s IS NOT NULL",
                database, tableNameWithIdentifier, fieldName);

        if (logger.isDebugEnabled()) {
            logger.debug("HiveTableExtractor getFieldStats nonnullCount sql: {}", nonNullCountSql);
        }
        fieldStatBuilder.withNonnullCount(dbOperator.fetchOne(nonNullCountSql, rs -> rs.getLong(1)));

        return fieldStatBuilder.build();
    }

    public Long getRowCount() {
        if (logger.isDebugEnabled()) {
            logger.debug("JDBCStatTemplate getRowCount start. database: {}, table: {}", database, table);
        }

        String rowCountSql = String.format("SELECT COUNT(*) FROM %s.%s", database, tableNameWithIdentifier);
        return dbOperator.fetchOne(rowCountSql, rs -> rs.getLong(1));
    }

    private boolean isIgnored(DatasetFieldType.Type type) {
        return type.equals(DatasetFieldType.Type.ARRAY) || type.equals(DatasetFieldType.Type.STRUCT);
    }

}