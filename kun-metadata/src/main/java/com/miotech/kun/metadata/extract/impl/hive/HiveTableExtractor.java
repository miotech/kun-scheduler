package com.miotech.kun.metadata.extract.impl.hive;

import com.amazonaws.services.glue.model.Column;
import com.amazonaws.services.glue.model.Table;
import com.beust.jcommander.internal.Lists;
import com.google.common.annotations.VisibleForTesting;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.template.ExtractorTemplate;
import com.miotech.kun.metadata.extract.tool.DatabaseIdentifierProcessor;
import com.miotech.kun.metadata.extract.tool.DatasetNameGenerator;
import com.miotech.kun.metadata.model.*;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.utils.JSONUtils;
import io.prestosql.jdbc.$internal.guava.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;

public class HiveTableExtractor extends ExtractorTemplate {
    private static Logger logger = LoggerFactory.getLogger(HiveTableExtractor.class);

    private final String database;
    private final String table;
    private final HiveCluster hiveCluster;
    private final Table glueTable;
    private final DatabaseType hiveAnalysisEngineType;
    private final String tableName;

    public HiveTableExtractor(HiveCluster cluster, String database, String table, Table glueTable) {
        super(cluster);
        this.database = database;
        this.table = table;
        this.hiveCluster = cluster;
        this.glueTable = glueTable;
        this.hiveAnalysisEngineType = HiveCluster.convertFromAnalysisEngine(hiveCluster.getHiveAnalysisEngine());
        this.tableName = DatabaseIdentifierProcessor.processTableNameIdentifier(table, hiveAnalysisEngineType);
        Preconditions.checkArgument(StringUtils.isNotBlank(table), "Invalid table name: " + table);
    }

    @Override
    @VisibleForTesting
    public List<DatasetField> getSchema() {
        logger.debug("HiveTableExtractor getSchema start. cluster: {}, database: {}, table: {}",
                JSONUtils.toJsonString(hiveCluster), database, table);
        // Get schema information of table
        switch (hiveCluster.getMetaStoreType()) {
            case MYSQL:
                DataSource dataSourceOfMySQL = JDBCClient.getDataSource(hiveCluster.getMetaStoreUrl(),
                        hiveCluster.getMetaStoreUsername(), hiveCluster.getMetaStorePassword(), DatabaseType.MYSQL);
                DatabaseOperator dbOperator = new DatabaseOperator(dataSourceOfMySQL);
                String sql = "SELECT source.* FROM  " +
                        "    (SELECT t.TBL_ID, d.NAME as `schema`, t.TBL_NAME name, t.TBL_TYPE, tp.PARAM_VALUE as description,  " +
                        "           p.PKEY_NAME as col_name, p.INTEGER_IDX as col_sort_order,  " +
                        "           p.PKEY_TYPE as col_type, p.PKEY_COMMENT as col_description, 1 as is_partition_col,  " +
                        "           IF(t.TBL_TYPE = 'VIRTUAL_VIEW', 1, 0) is_view " +
                        "    FROM TBLS t" +
                        "    JOIN DBS d ON t.DB_ID = d.DB_ID" +
                        "    JOIN PARTITION_KEYS p ON t.TBL_ID = p.TBL_ID " +
                        "    LEFT JOIN TABLE_PARAMS tp ON (t.TBL_ID = tp.TBL_ID AND tp.PARAM_KEY='comment') " +
                        "    WHERE t.TBL_NAME = ? " +
                        "    UNION " +
                        "    SELECT t.TBL_ID, d.NAME as `schema`, t.TBL_NAME name, t.TBL_TYPE, tp.PARAM_VALUE as description, " +
                        "           c.COLUMN_NAME as col_name, c.INTEGER_IDX as col_sort_order, " +
                        "           c.TYPE_NAME as col_type, c.COMMENT as col_description, 0 as is_partition_col, " +
                        "           IF(t.TBL_TYPE = 'VIRTUAL_VIEW', 1, 0) is_view " +
                        "    FROM TBLS t " +
                        "    JOIN DBS d ON t.DB_ID = d.DB_ID " +
                        "    JOIN SDS s ON t.SD_ID = s.SD_ID " +
                        "    JOIN COLUMNS_V2 c ON s.CD_ID = c.CD_ID " +
                        "    LEFT JOIN TABLE_PARAMS tp ON (t.TBL_ID = tp.TBL_ID AND tp.PARAM_KEY='comment') " +
                        "    WHERE t.TBL_NAME = ? " +
                        "    ) source " +
                        "    ORDER by tbl_id, is_partition_col desc;";
                return dbOperator.fetchAll(sql, rs -> {
                    String name = rs.getString(6);
                    String type = rs.getString(8);
                    String description = rs.getString(9);
                    return new DatasetField(name, new DatasetFieldType(DatasetFieldType.convertRawType(type), type), description);
                }, table, table);
            case GLUE:
                List<DatasetField> fields = Lists.newArrayList();
                if (glueTable.getStorageDescriptor() != null) {
                    if (glueTable.getStorageDescriptor().getColumns() != null) {
                        for (Column column : glueTable.getStorageDescriptor().getColumns()) {
                            DatasetField field = new DatasetField(column.getName(), new DatasetFieldType(DatasetFieldType.convertRawType(column.getType()), column.getType()), column.getComment());
                            fields.add(field);
                        }
                    }
                }
                return fields;
            default:
                throw new RuntimeException("invalid metaStoreType: " + hiveCluster.getMetaStoreType());
        }
    }

    @Override
    @VisibleForTesting
    public DatasetFieldStat getFieldStats(DatasetField datasetField) {
        logger.debug("HiveTableExtractor getFieldStats start. cluster: {}, database: {}, table: {}, datasetField: {}",
                JSONUtils.toJsonString(hiveCluster), database, table, JSONUtils.toJsonString(datasetField));
        long distinctCount = 0;
        long nonnullCount = 0;
        DatasetFieldStat.Builder fieldStatBuilder = DatasetFieldStat.newBuilder();
        fieldStatBuilder.withName(datasetField.getName()).withStatDate(LocalDate.now());

        if (isIgnored(datasetField.getFieldType().getType())) {
            fieldStatBuilder.withDistinctCount(distinctCount).withNonnullCount(nonnullCount);
            return fieldStatBuilder.build();
        }

        /* reference: https://docs.aws.amazon.com/zh_cn/athena/latest/ug/tables-databases-columns-names.html */
        String fieldName = DatabaseIdentifierProcessor.processFieldNameIdentifier(datasetField.getName(), hiveAnalysisEngineType);

        DataSource dataSource = JDBCClient.getDataSource(hiveCluster.getDataStoreUrl(), hiveCluster.getDataStoreUsername(),
                hiveCluster.getDataStorePassword(), hiveAnalysisEngineType);
        DatabaseOperator dbOperator = new DatabaseOperator(dataSource);
        String sql = "SELECT COUNT(*) FROM (SELECT " + fieldName + " FROM " + database + "." + tableName + " GROUP BY " + fieldName + ") t1";
        logger.debug("HiveTableExtractor getFieldStats distinctCount sql:" + sql);
        fieldStatBuilder.withDistinctCount(dbOperator.fetchOne(sql, rs -> rs.getLong(1)));

        sql = "SELECT COUNT(*) FROM " + database + "." + tableName + " WHERE " + fieldName + " IS NOT NULL";
        logger.debug("HiveTableExtractor getFieldStats nonnullCount sql:" + sql);
        fieldStatBuilder.withNonnullCount(dbOperator.fetchOne(sql, rs -> rs.getLong(1)));

        return fieldStatBuilder.build();
    }

    @Override
    @VisibleForTesting
    public DatasetStat getTableStats() {
        logger.debug("HiveTableExtractor getFieldStats start. cluster: {}, database: {}, table: {}",
                JSONUtils.toJsonString(hiveCluster), database, table);
        DatasetStat.Builder datasetStatBuilder = DatasetStat.newBuilder();
        datasetStatBuilder.withStatDate(LocalDate.now());

        DataSource dataSource = JDBCClient.getDataSource(hiveCluster.getDataStoreUrl(), hiveCluster.getDataStoreUsername(),
                hiveCluster.getDataStorePassword(), hiveAnalysisEngineType);
        DatabaseOperator dbOperator = new DatabaseOperator(dataSource);
        String sql = "SELECT COUNT(*) FROM " + database + "." + tableName;
        datasetStatBuilder.withRowCount(dbOperator.fetchOne(sql, rs -> rs.getLong(1)));

        logger.debug("HiveTableExtractor getFieldStats end. datasetStat: {}",
                JSONUtils.toJsonString(datasetStatBuilder.build()));
        return datasetStatBuilder.build();
    }

    @Override
    protected DataStore getDataStore() {
        return new HiveTableStore(hiveCluster.getDataStoreUrl(), database, table);
    }

    @Override
    protected String getName() {
        return DatasetNameGenerator.generateDatasetName(DatabaseType.HIVE, table);
    }

    @Override
    protected long getClusterId() {
        return cluster.getClusterId();
    }

    private boolean isIgnored(DatasetFieldType.Type type) {
        return type.equals(DatasetFieldType.Type.ARRAY) || type.equals(DatasetFieldType.Type.STRUCT);
    }

}
