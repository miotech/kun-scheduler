package com.miotech.kun.metadata.extract.impl.hive;

import com.amazonaws.services.glue.model.Column;
import com.amazonaws.services.glue.model.Table;
import com.beust.jcommander.internal.Lists;
import com.google.common.annotations.VisibleForTesting;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.template.ExtractorTemplate;
import com.miotech.kun.metadata.extract.tool.DatasetNameGenerator;
import com.miotech.kun.metadata.model.*;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.utils.JSONUtils;
import io.prestosql.jdbc.$internal.guava.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HiveTableExtractor extends ExtractorTemplate {
    private static Logger logger = LoggerFactory.getLogger(HiveTableExtractor.class);

    private final String database;
    private final String table;
    private final HiveCluster hiveCluster;
    private final Table glueTable;
    private final String tableName;
    private static final List<String> reservedKeywordsOfAthena;
    private static final List<String> reservedKeywordsOfHive;
    static {
        reservedKeywordsOfAthena = Lists.newArrayList(Arrays.asList("ALTER","AND","AS","BETWEEN","BY","CASE","CAST","CONSTRAINT",
                "CREATE","CROSS","CUBE","CURRENT_DATE","CURRENT_PATH","CURRENT_TIME","CURRENT_TIMESTAMP","CURRENT_USER",
                "DEALLOCATE","DELETE","DESCRIBE","DISTINCT","DROP","ELSE","END","ESCAPE","EXCEPT","EXECUTE","EXISTS",
                "EXTRACT","FALSE","FIRST","FOR","FROM","FULL","GROUP","GROUPING","HAVING","IN","INNER","INSERT",
                "INTERSECT","INTO","IS","JOIN","LAST","LEFT","LIKE","LOCALTIME","LOCALTIMESTAMP","NATURAL","NORMALIZE",
                "NOT","NULL","ON","OR","ORDER","OUTER","PREPARE","RECURSIVE","RIGHT","ROLLUP","SELECT","TABLE","THEN",
                "TRUE","UNESCAPE","UNION","UNNEST","USING","VALUES","WHEN","WHERE","WITH"));

        reservedKeywordsOfHive = Lists.newArrayList(Arrays.asList("ALL","ALTER","AND","ARRAY","AS","AUTHORIZATION","BETWEEN",
                "BIGINT","BINARY","BOOLEAN","BOTH","BY","CASE","CAST","CHAR","COLUMN","CONF","CREATE","CROSS","CUBE",
                "CURRENT","CURRENT_DATE","CURRENT_TIMESTAMP","CURSOR","DATABASE","DATE","DECIMAL","DELETE","DESCRIBE",
                "DISTINCT","DOUBLE","DROP","ELSE","END","EXCHANGE","EXISTS","EXTENDED","EXTERNAL","FALSE","FETCH",
                "FLOAT","FOLLOWING","FOR","FROM","FULL","FUNCTION","GRANT","GROUP","GROUPING","HAVING","IF","IMPORT",
                "IN","INNER","INSERT","INT","INTERSECT","INTERVAL","INTO","IS","JOIN","LATERAL","LEFT","LESS","LIKE",
                "LOCAL","MACRO","MAP","MORE","NONE","NOT","NULL","OF","ON","OR","ORDER","OUT","OUTER","OVER","PARTIALSCAN",
                "PARTITION","PERCENT","PRECEDING","PRESERVE","PROCEDURE","RANGE","READS","REDUCE","REVOKE","RIGHT","ROLLUP",
                "ROW","ROWS","SELECT","SET","SMALLINT","TABLE","TABLESAMPLE","THEN","TIMESTAMP","TO","TRANSFORM","TRIGGER",
                "TRUE","TRUNCATE","UNBOUNDED","UNION","UNIQUEJOIN","UPDATE","USER","USING","UTC_TMESTAMP","VALUES",
                "VARCHAR","WHEN","WHERE","WINDOW","WITH"));

    }

    public HiveTableExtractor(HiveCluster cluster, String database, String table, Table glueTable) {
        super(cluster);
        this.database = database;
        this.table = table;
        this.hiveCluster = cluster;
        this.glueTable = glueTable;

        // reference: https://docs.aws.amazon.com/zh_cn/athena/latest/ug/tables-databases-columns-names.html
        Preconditions.checkArgument(StringUtils.isNotBlank(table), "Invalid table name: " + table);
        if (table.startsWith("_")) {
            this.tableName = "`" + table + "`";
        } else if (Character.isDigit(table.charAt(0))) {
            this.tableName = "\"" + table + "\"";
        } else {
            this.tableName = table;
        }
    }

    @Override
    @VisibleForTesting
    public List<DatasetField> getSchema() {
        logger.debug("HiveTableExtractor getSchema start. cluster: {}, database: {}, table: {}",
                JSONUtils.toJsonString(hiveCluster), database, table);
        // Get schema information of table
        List<DatasetField> fields = Lists.newArrayList();

        switch (hiveCluster.getMetaStoreType()) {
            case MYSQL:
                Connection connection = null;
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    connection = JDBCClient.getConnection(DatabaseType.MYSQL, hiveCluster.getMetaStoreUrl(),
                            hiveCluster.getMetaStoreUsername(), hiveCluster.getMetaStorePassword());
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
                    statement = connection.prepareStatement(sql);

                    statement.setString(1, table);
                    statement.setString(2, table);
                    resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                        String name = resultSet.getString(6);
                        String type = resultSet.getString(8);
                        String description = resultSet.getString(9);

                        DatasetField field = new DatasetField(name, new DatasetFieldType(DatasetFieldType.convertRawType(type), type), description);
                        fields.add(field);
                    }
                } catch (ClassNotFoundException classNotFoundException) {
                    logger.error("driver class not found, DatabaseType: {}", DatabaseType.MYSQL.getName(), classNotFoundException);
                    throw ExceptionUtils.wrapIfChecked(classNotFoundException);
                } catch (SQLException sqlException) {
                    throw ExceptionUtils.wrapIfChecked(sqlException);
                } finally {
                    JDBCClient.close(connection, statement, resultSet);
                }
                break;
            case GLUE:
                if (glueTable.getStorageDescriptor() != null) {
                    if (glueTable.getStorageDescriptor().getColumns() != null) {
                        for (Column column : glueTable.getStorageDescriptor().getColumns()) {
                            DatasetField field = new DatasetField(column.getName(), new DatasetFieldType(DatasetFieldType.convertRawType(column.getType()), column.getType()), column.getComment());
                            fields.add(field);
                        }
                    }
                }
                break;
            default:
                throw new RuntimeException("invalid metaStoreType: " + hiveCluster.getMetaStoreType());
        }

        logger.debug("HiveTableExtractor getSchema end. fields: {}", JSONUtils.toJsonString(fields));
        return fields;
    }

    @Override
    @VisibleForTesting
    public DatasetFieldStat getFieldStats(DatasetField datasetField) {
        logger.debug("HiveTableExtractor getFieldStats start. cluster: {}, database: {}, table: {}, datasetField: {}",
                JSONUtils.toJsonString(hiveCluster), database, table, JSONUtils.toJsonString(datasetField));

        DatabaseType hiveAnalysisEngineType = HiveCluster.convertFromAnalysisEngine(hiveCluster.getHiveAnalysisEngine());

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            long distinctCount = 0;
            long nonnullCount = 0;

            if (datasetField.getFieldType().getRawType().contains("array") || datasetField.getFieldType().getRawType().contains("struct")) {
                DatasetFieldStat fieldStat = new DatasetFieldStat(datasetField.getName(), distinctCount, nonnullCount, null, new Date());
                logger.debug("HiveTableExtractor getFieldStats end. fieldStat: {}", JSONUtils.toJsonString(fieldStat));
                return fieldStat;
            }

            String fieldName = datasetField.getName();
            if (hiveAnalysisEngineType.equals(DatabaseType.ATHENA) && reservedKeywordsOfAthena.contains(fieldName.toUpperCase())) {
                fieldName = "\"" + fieldName + "\"";
            } else if (hiveAnalysisEngineType.equals(DatabaseType.HIVE) && reservedKeywordsOfHive.contains(fieldName.toUpperCase())) {
                fieldName = "`" + fieldName + "`";
            }

            connection = JDBCClient.getConnection(hiveAnalysisEngineType, hiveCluster.getDataStoreUrl(), hiveCluster.getDataStoreUsername(), hiveCluster.getDataStorePassword());
            String sql = "SELECT COUNT(*) FROM (SELECT " + fieldName + " FROM " + database + "." + tableName + " GROUP BY " + fieldName + ") t1";
            logger.debug("HiveTableExtractor getFieldStats distinctCount sql: " + sql);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                distinctCount = resultSet.getLong(1);
            }

            sql = "SELECT COUNT(*) FROM " + database + "." + tableName + " WHERE " + fieldName + " IS NOT NULL";
            logger.debug("HiveTableExtractor getFieldStats nonnullCount sql: " + sql);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                nonnullCount = resultSet.getLong(1);
            }

            DatasetFieldStat fieldStat = new DatasetFieldStat(datasetField.getName(), distinctCount, nonnullCount, null, new Date());

            logger.debug("HiveTableExtractor getFieldStats end. fieldStat: {}", JSONUtils.toJsonString(fieldStat));
            return fieldStat;
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.MYSQL.getName(), classNotFoundException);
            throw ExceptionUtils.wrapIfChecked(classNotFoundException);
        } catch (SQLException sqlException) {
            throw ExceptionUtils.wrapIfChecked(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

    }

    @Override
    @VisibleForTesting
    public DatasetStat getTableStats() {
        logger.debug("HiveTableExtractor getFieldStats start. cluster: {}, database: {}, table: {}",
                JSONUtils.toJsonString(hiveCluster), database, table);
        DatasetStat.Builder datasetStatBuilder = DatasetStat.newBuilder();
        DatabaseType hiveAnalysisEngineType = HiveCluster.convertFromAnalysisEngine(hiveCluster.getHiveAnalysisEngine());

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCClient.getConnection(hiveAnalysisEngineType, hiveCluster.getDataStoreUrl(), hiveCluster.getDataStoreUsername(), hiveCluster.getDataStorePassword());
            String sql = "SELECT COUNT(*) FROM " + database + "." + tableName;
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Long rowCount = resultSet.getLong(1);
                datasetStatBuilder.withRowCount(rowCount);
                datasetStatBuilder.withStatDate(new Date());
            }
        } catch (ClassNotFoundException classNotFoundException) {
            logger.error("driver class not found, DatabaseType: {}", DatabaseType.HIVE.getName(), classNotFoundException);
            throw ExceptionUtils.wrapIfChecked(classNotFoundException);
        } catch (SQLException sqlException) {
            throw ExceptionUtils.wrapIfChecked(sqlException);
        } finally {
            JDBCClient.close(connection, statement, resultSet);
        }

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

}
