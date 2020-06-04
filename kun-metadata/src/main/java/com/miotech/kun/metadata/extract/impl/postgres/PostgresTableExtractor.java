package com.miotech.kun.metadata.extract.impl.postgres;

import com.google.common.base.Preconditions;
import com.miotech.kun.metadata.client.JDBCClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.template.ExtractorTemplate;
import com.miotech.kun.metadata.extract.tool.DatasetNameGenerator;
import com.miotech.kun.metadata.extract.tool.StringUtil;
import com.miotech.kun.metadata.extract.tool.UseDatabaseUtil;
import com.miotech.kun.metadata.model.*;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.PostgresDataStore;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;

public class PostgresTableExtractor extends ExtractorTemplate {
    private static Logger logger = LoggerFactory.getLogger(PostgresTableExtractor.class);

    private final PostgresDataSource dataSource;
    private final String database;
    private final String schema;
    private final String table;

    public PostgresTableExtractor(PostgresDataSource dataSource, String database, String schema, String table) {
        super(dataSource.getId());
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
        this.database = database;
        this.schema = schema;
        this.table = table;
    }

    @Override
    public List<DatasetField> getSchema() {
        // Get schema information of table
        logger.debug("PostgresTableExtractor getSchema start. dataSource: {}, database: {}, schema: {}, table: {}",
                JSONUtils.toJsonString(dataSource), database, schema, table);
        List<DatasetField> fields;

        DataSource pgDataSource = JDBCClient.getDataSource(UseDatabaseUtil.useDatabase(dataSource.getUrl(), database),
                dataSource.getUsername(), dataSource.getPassword(), DatabaseType.POSTGRES);
        DatabaseOperator dbOperator = new DatabaseOperator(pgDataSource);
        String sql = "SELECT column_name, udt_name, '' FROM information_schema.columns WHERE table_name = ? AND table_schema = ?";
        fields = dbOperator.fetchAll(sql, rs -> {
            String name = rs.getString(1);
            String type = rs.getString(2);
            String description = rs.getString(3);

            return new DatasetField(name, new DatasetFieldType(convertRawType(type), type), description);
        }, table, schema);

        logger.debug("PostgresTableExtractor getSchema end. fields: {}", JSONUtils.toJsonString(fields));
        return fields;
    }

    @Override
    public DatasetFieldStat getFieldStats(DatasetField datasetField) {
        logger.debug("PostgresTableExtractor getFieldStats start. dataSource: {}, database: {}, schema: {}, table: {}, field: {}",
                JSONUtils.toJsonString(dataSource), database, schema, table, datasetField.getName());
        DatasetFieldStat.Builder datasetFieldBuilder = DatasetFieldStat.newBuilder();
        datasetFieldBuilder.withName(datasetField.getName()).withStatDate(LocalDate.now());

        DataSource pgDataSource = JDBCClient.getDataSource(UseDatabaseUtil.useDatabase(dataSource.getUrl(), database),
                dataSource.getUsername(), dataSource.getPassword(), DatabaseType.POSTGRES);
        DatabaseOperator dbOperator = new DatabaseOperator(pgDataSource);
        String sql = "SELECT COUNT(DISTINCT(" + StringUtil.convertUpperCase(datasetField.getName()) + ")) FROM " + StringUtil.convertUpperCase(table);

        if (isIgnoredType(datasetField.getFieldType().getType())) {
            return datasetFieldBuilder.withDistinctCount(0L).withNonnullCount(0L).build();
        } else if (isSpecialType(datasetField.getFieldType().getType())) {
            sql = "SELECT COUNT(DISTINCT(CAST(" + StringUtil.convertUpperCase(datasetField.getName()) + " AS VARCHAR))) FROM " + StringUtil.convertUpperCase(table);
        }
        datasetFieldBuilder.withDistinctCount(dbOperator.fetchOne(sql, rs -> rs.getLong(1)));

        sql = "SELECT COUNT(*) FROM " + StringUtil.convertUpperCase(table) + " WHERE " + StringUtil.convertUpperCase(datasetField.getName()) + " IS NOT NULL";
        datasetFieldBuilder.withNonnullCount(dbOperator.fetchOne(sql, rs -> rs.getLong(1)));

        DatasetFieldStat result = datasetFieldBuilder.build();
        logger.debug("PostgresTableExtractor getFieldStats end. datasetField: {}", JSONUtils.toJsonString(result));
        return result;
    }

    private boolean isSpecialType(DatasetFieldType.Type type) {
        if (type == DatasetFieldType.Type.JSON) {
            return true;
        }
        return false;
    }

    private boolean isIgnoredType(DatasetFieldType.Type type) {
        if (type == DatasetFieldType.Type.STRUCT) {
            return true;
        }
        return false;
    }

    @Override
    public DatasetStat getTableStats() {
        logger.debug("PostgresTableExtractor getTableStats start. dataSource: {}, database: {}, schema: {}, table: {}",
                JSONUtils.toJsonString(dataSource), database, schema, table);
        DatasetStat.Builder datasetStatBuilder = DatasetStat.newBuilder();
        datasetStatBuilder.withStatDate(LocalDate.now());

        DataSource pgDataSource = JDBCClient.getDataSource(UseDatabaseUtil.useDatabase(dataSource.getUrl(), database),
                dataSource.getUsername(), dataSource.getPassword(), DatabaseType.POSTGRES);
        DatabaseOperator dbOperator = new DatabaseOperator(pgDataSource);
        String sql = "SELECT COUNT(*) FROM " + StringUtil.convertUpperCase(table);
        datasetStatBuilder.withRowCount(dbOperator.fetchOne(sql, rs -> rs.getLong(1)));

        DatasetStat result = datasetStatBuilder.build();
        logger.debug("PostgresTableExtractor getTableStats end. datasetStat: {}", JSONUtils.toJsonString(result));
        return result;
    }

    @Override
    protected DataStore getDataStore() {
        return new PostgresDataStore(dataSource.getUrl(), database, schema, table);
    }

    @Override
    protected String getName() {
        return DatasetNameGenerator.generateDatasetName(DatabaseType.POSTGRES, table);
    }

    private DatasetFieldType.Type convertRawType(String rawType) {
        if ("string".equals(rawType) ||
                rawType.startsWith("varchar") ||
                rawType.startsWith("char")) {
            return DatasetFieldType.Type.CHARACTER;
        } else if ("timestamp".equals(rawType) ||
                "date".equals(rawType)) {
            return DatasetFieldType.Type.DATETIME;
        } else if (rawType.startsWith("array")) {
            return DatasetFieldType.Type.ARRAY;
        } else if (rawType.startsWith("decimal") ||
                "double".equals(rawType) ||
                "number".equals(rawType) ||
                "int".equals(rawType) ||
                "bigint".equals(rawType)) {
            return DatasetFieldType.Type.NUMBER;
        } else if (rawType.startsWith("struct")) {
            return DatasetFieldType.Type.STRUCT;
        } else if ("boolean".equals(rawType) || "BOOL".equals(rawType)) {
            return DatasetFieldType.Type.BOOLEAN;
        } else if ("json".equals(rawType) || "jsonb".equals(rawType)) {
            return DatasetFieldType.Type.JSON;
        } else {
            logger.warn("unknown type: " + rawType);
            return DatasetFieldType.Type.UNKNOW;
        }
    }

}
