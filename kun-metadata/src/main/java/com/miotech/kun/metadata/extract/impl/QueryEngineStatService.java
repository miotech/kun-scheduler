package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.impl.hive.HiveTableExtractor;
import com.miotech.kun.metadata.extract.tool.DatabaseIdentifierProcessor;
import com.miotech.kun.metadata.model.*;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.LocalDate;

public class QueryEngineStatService {
    private static Logger logger = LoggerFactory.getLogger(HiveTableExtractor.class);

    private final String database;
    private final String table;
    private final String tableNameWithIdentifier;
    private final DatabaseType databaseType;

    public QueryEngineStatService(String database, String table, DatabaseType databaseType) {
        this.database = database;
        this.table = table;
        this.databaseType = databaseType;
        this.tableNameWithIdentifier = DatabaseIdentifierProcessor.processTableNameIdentifier(table, databaseType);
    }

    public DatasetFieldStat getFieldStats(DatasetField datasetField, QueryEngine queryEngine) {
        logger.debug("HiveTableExtractor getFieldStats start. database: {}, table: {}, datasetField: {}", database,
                table, JSONUtils.toJsonString(datasetField));
        DatasetFieldStat.Builder fieldStatBuilder = DatasetFieldStat.newBuilder();
        fieldStatBuilder.withName(datasetField.getName()).withStatDate(LocalDate.now());

        if (isIgnored(datasetField.getFieldType().getType())) {
            fieldStatBuilder.withDistinctCount(0L).withNonnullCount(0L);
            return fieldStatBuilder.build();
        }

        /* reference: https://docs.aws.amazon.com/zh_cn/athena/latest/ug/tables-databases-columns-names.html */
        String fieldName = DatabaseIdentifierProcessor.processFieldNameIdentifier(datasetField.getName(), databaseType);

        DataSource dataSource = buildDataSource(databaseType, queryEngine);
        DatabaseOperator dbOperator = new DatabaseOperator(dataSource);
        String sql = "SELECT COUNT(*) FROM (SELECT " + fieldName + " FROM " + database + "." + tableNameWithIdentifier + " GROUP BY " + fieldName + ") t1";
        logger.debug("HiveTableExtractor getFieldStats distinctCount sql:" + sql);
        fieldStatBuilder.withDistinctCount(dbOperator.fetchOne(sql, rs -> rs.getLong(1)));

        sql = "SELECT COUNT(*) FROM " + database + "." + tableNameWithIdentifier + " WHERE " + fieldName + " IS NOT NULL";
        logger.debug("HiveTableExtractor getFieldStats nonnullCount sql:" + sql);
        fieldStatBuilder.withNonnullCount(dbOperator.fetchOne(sql, rs -> rs.getLong(1)));

        return fieldStatBuilder.build();
    }

    public DatasetStat getTableStats(QueryEngine queryEngine) {
        logger.debug("HiveTableExtractor getFieldStats start. database: {}, table: {}", database, table);
        DatasetStat.Builder datasetStatBuilder = DatasetStat.newBuilder();
        datasetStatBuilder.withStatDate(LocalDate.now());

        DataSource dataSource = buildDataSource(databaseType, queryEngine);
        DatabaseOperator dbOperator = new DatabaseOperator(dataSource);
        String sql = "SELECT COUNT(*) FROM " + database + "." + tableNameWithIdentifier;
        datasetStatBuilder.withRowCount(dbOperator.fetchOne(sql, rs -> rs.getLong(1)));

        logger.debug("HiveTableExtractor getFieldStats end. datasetStat: {}",
                JSONUtils.toJsonString(datasetStatBuilder.build()));
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
