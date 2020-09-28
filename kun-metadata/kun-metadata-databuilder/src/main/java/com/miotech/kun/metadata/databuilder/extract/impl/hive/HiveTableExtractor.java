package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.google.common.annotations.VisibleForTesting;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.core.model.*;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.template.ExtractorTemplate;
import com.miotech.kun.metadata.databuilder.extract.template.JDBCStatTemplate;
import com.miotech.kun.metadata.databuilder.model.HiveDataSource;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

public class HiveTableExtractor extends ExtractorTemplate {
    private static Logger logger = LoggerFactory.getLogger(HiveTableExtractor.class);

    private final String database;
    private final String table;
    private final HiveDataSource dataSource;
    private final DataSource metastoreDataSource;
    private final DataSource datastoreDataSource;

    public HiveTableExtractor(Props props, HiveDataSource dataSource, String database, String table) {
        super(props, dataSource.getId());
        this.database = database;
        this.table = table;
        this.dataSource = dataSource;
        this.metastoreDataSource = JDBCClient.getDataSource(dataSource.getMetastoreUrl(), dataSource.getMetastoreUsername(),
                dataSource.getMetastorePassword(), DatabaseType.MYSQL);
        this.datastoreDataSource = JDBCClient.getDataSource(dataSource.getDatastoreUrl(), dataSource.getDatastoreUsername(),
                dataSource.getDatastorePassword(), DatabaseType.HIVE);
    }

    @Override
    @VisibleForTesting
    public List<DatasetField> getSchema() {
        if (logger.isDebugEnabled()) {
            logger.debug("HiveTableExtractor getSchema start. dataSource: {}, database: {}, table: {}",
                    JSONUtils.toJsonString(dataSource), database, table);
        }

        // Get schema information of table
        DatabaseOperator dbOperator = new DatabaseOperator(metastoreDataSource);
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
    }

    @Override
    @VisibleForTesting
    public DatasetFieldStat getFieldStats(DatasetField datasetField) {
        if (logger.isDebugEnabled()) {
            logger.debug("HiveTableExtractor getFieldStats start. dataSource: {}, database: {}, table: {}, datasetField: {}",
                    JSONUtils.toJsonString(dataSource), database, table, JSONUtils.toJsonString(datasetField));
        }

        JDBCStatTemplate statService = new JDBCStatTemplate(database, table, DatabaseType.HIVE, datastoreDataSource);
        return statService.getFieldStats(datasetField);
    }

    @Override
    @VisibleForTesting
    public DatasetStat getTableStats() {
        DatasetStat.Builder datasetStatBuilder = DatasetStat.newBuilder();
        if (logger.isDebugEnabled()) {
            logger.debug("HiveTableExtractor getFieldStats start. dataSource: {}, database: {}, table: {}",
                    JSONUtils.toJsonString(dataSource), database, table);
        }

        JDBCStatTemplate statService = new JDBCStatTemplate(database, table, DatabaseType.HIVE, datastoreDataSource);
        datasetStatBuilder.withStatDate(LocalDateTime.now())
                .withRowCount(statService.getRowCount())
                .withLastUpdatedTime(getLastUpdateTime());
        return datasetStatBuilder.build();
    }

    @Override
    protected DataStore getDataStore() {
        return new HiveTableStore(dataSource.getDatastoreUrl(), database, table);
    }

    @Override
    protected String getName() {
        return table;
    }

    @Override
    protected LocalDateTime getLastUpdateTime() {
        String sql = "SELECT s.LOCATION FROM TBLS t JOIN DBS d ON t.DB_ID = d.DB_ID JOIN SDS s ON t.SD_ID = s.SD_ID WHERE d.NAME = ? AND t.TBL_NAME = ?";
        DatabaseOperator dbOperator = new DatabaseOperator(metastoreDataSource);
        String[] locations = dbOperator.fetchOne(sql, rs -> {
            String location = rs.getString(1);
            int idx = location.indexOf("/", location.lastIndexOf(":"));

            String[] locationArr = new String[2];
            locationArr[0] = location.substring(0, idx);
            locationArr[1] = location.substring(idx);
            return locationArr;
        }, database, table);
        FileSystem fileSystem = HDFSOperator.create(locations[0] + "/" + table, "hdfs");
        FileStatus fileStatus;
        try {
            fileStatus = fileSystem.getFileStatus(new Path(locations[1]));
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(fileStatus.getModificationTime()), TimeZone.getDefault().toZoneId());
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            HDFSOperator.close(fileSystem);
        }
    }

    @Override
    protected void close() {
        ((HikariDataSource) metastoreDataSource).close();
        ((HikariDataSource) datastoreDataSource).close();
    }

}