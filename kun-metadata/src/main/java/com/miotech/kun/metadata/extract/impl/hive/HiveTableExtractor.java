package com.miotech.kun.metadata.extract.impl.hive;

import com.google.common.annotations.VisibleForTesting;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.impl.QueryEngineStatService;
import com.miotech.kun.metadata.extract.template.ExtractorTemplate;
import com.miotech.kun.metadata.extract.tool.DatasetNameGenerator;
import com.miotech.kun.metadata.model.*;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class HiveTableExtractor extends ExtractorTemplate {
    private static Logger logger = LoggerFactory.getLogger(HiveTableExtractor.class);

    private final String database;
    private final String table;
    private final ConfigurableDataSource dataSource;

    public HiveTableExtractor(ConfigurableDataSource dataSource, String database, String table) {
        super(dataSource.getId());
        this.database = database;
        this.table = table;
        this.dataSource = dataSource;
    }

    @Override
    @VisibleForTesting
    public List<DatasetField> getSchema() {
        logger.debug("HiveTableExtractor getSchema start. dataSource: {}, database: {}, table: {}",
                JSONUtils.toJsonString(dataSource), database, table);
        // Get schema information of table
        MetaStoreCatalog catalog = (MetaStoreCatalog) dataSource.getCatalog();
        DataSource dataSourceOfMySQL = JDBCClient.getDataSource(catalog.getUrl(), catalog.getUsername(),
                catalog.getPassword(), DatabaseType.MYSQL);
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
    }

    @Override
    @VisibleForTesting
    public DatasetFieldStat getFieldStats(DatasetField datasetField) {
        logger.debug("HiveTableExtractor getFieldStats start. dataSource: {}, database: {}, table: {}, datasetField: {}",
                JSONUtils.toJsonString(dataSource), database, table, JSONUtils.toJsonString(datasetField));
        QueryEngineStatService statService = new QueryEngineStatService(database, table,
                QueryEngine.parseDatabaseTypeFromDataSource(dataSource.getQueryEngine()));
        return statService.getFieldStats(datasetField, dataSource.getQueryEngine());
    }

    @Override
    @VisibleForTesting
    public DatasetStat getTableStats() {
        logger.debug("HiveTableExtractor getFieldStats start. dataSource: {}, database: {}, table: {}",
                JSONUtils.toJsonString(dataSource), database, table);
        QueryEngineStatService statService = new QueryEngineStatService(database, table,
                QueryEngine.parseDatabaseTypeFromDataSource(dataSource.getQueryEngine()));
        return statService.getTableStats(dataSource.getQueryEngine());
    }

    @Override
    protected DataStore getDataStore() {
        return new HiveTableStore(QueryEngine.parseConnInfos(dataSource.getQueryEngine())[0], database, table);
    }

    @Override
    protected String getName() {
        return DatasetNameGenerator.generateDatasetName(DatabaseType.HIVE, table);
    }

}
