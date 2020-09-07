package com.miotech.kun.metadata.databuilder.extract.impl.glue;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldStat;
import com.miotech.kun.metadata.core.model.DatasetFieldType;
import com.miotech.kun.metadata.core.model.DatasetStat;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.impl.hive.HiveTableExtractor;
import com.miotech.kun.metadata.databuilder.extract.tool.DatabaseIdentifierProcessor;
import com.miotech.kun.metadata.databuilder.model.QueryEngine;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.LocalDateTime;

public class JDBCStatService {
    private static Logger logger = LoggerFactory.getLogger(HiveTableExtractor.class);

    private final String database;
    private final String table;
    private final String tableNameWithIdentifier;
    private final DatabaseType databaseType;

    public JDBCStatService(String database, String table, DatabaseType databaseType) {
        this.database = DatabaseIdentifierProcessor.escape(database, databaseType);
        this.table = table;
        this.databaseType = databaseType;
        this.tableNameWithIdentifier = DatabaseIdentifierProcessor.escape(table, databaseType);
    }

    public DatasetFieldStat getFieldStats(DatasetField datasetField, QueryEngine queryEngine) {
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

        DataSource dataSource = buildDataSource(databaseType, queryEngine);
        DatabaseOperator dbOperator = new DatabaseOperator(dataSource);
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

    public DatasetStat getTableStats(QueryEngine queryEngine) {
        if (logger.isDebugEnabled()) {
            logger.debug("HiveTableExtractor getFieldStats start. database: {}, table: {}", database, table);
        }

        DatasetStat.Builder datasetStatBuilder = DatasetStat.newBuilder();
        datasetStatBuilder.withStatDate(LocalDateTime.now());

        DataSource dataSource = buildDataSource(databaseType, queryEngine);
        DatabaseOperator dbOperator = new DatabaseOperator(dataSource);
        String rowCountSql = String.format("SELECT COUNT(*) FROM %s.%s", database, tableNameWithIdentifier);
        datasetStatBuilder.withRowCount(dbOperator.fetchOne(rowCountSql, rs -> rs.getLong(1)));

        if (logger.isDebugEnabled()) {
            logger.debug("HiveTableExtractor getFieldStats end. datasetStat: {}",
                    JSONUtils.toJsonString(datasetStatBuilder.build()));
        }
        return datasetStatBuilder.build();
    }

    private DataSource buildDataSource(DatabaseType databaseType, QueryEngine queryEngine) {
        String[] connInfos = QueryEngine.parseConnInfos(queryEngine);
        return JDBCClient.getDataSource(connInfos[0], connInfos[1], connInfos[2], databaseType);
    }

    private boolean isIgnored(DatasetFieldType.Type type) {
        return type.equals(DatasetFieldType.Type.ARRAY) || type.equals(DatasetFieldType.Type.STRUCT);
    }

}
