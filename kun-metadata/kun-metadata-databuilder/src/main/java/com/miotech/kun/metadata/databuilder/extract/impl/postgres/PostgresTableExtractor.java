package com.miotech.kun.metadata.databuilder.extract.impl.postgres;

import com.google.common.base.Preconditions;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.metadata.core.model.*;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.template.ExtractorTemplate;
import com.miotech.kun.metadata.databuilder.extract.tool.TableOrFieldNameEscapeUtil;
import com.miotech.kun.metadata.databuilder.extract.tool.UseDatabaseUtil;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;
import com.miotech.kun.workflow.core.model.lineage.PostgresDataStore;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

public class PostgresTableExtractor extends ExtractorTemplate {
    private static Logger logger = LoggerFactory.getLogger(PostgresTableExtractor.class);

    private final PostgresDataSource dataSource;
    private final String database;
    private final String schema;
    private final String table;
    private final DatabaseOperator dbOperator;
    private final DataSource pgDataSource;

    public PostgresTableExtractor(PostgresDataSource dataSource, String database, String schema, String table) {
        super(dataSource.getId());
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
        this.database = database;
        this.schema = schema;
        this.table = table;
        this.pgDataSource = JDBCClient.getDataSource(UseDatabaseUtil.useSchema(dataSource.getUrl(), database, schema),
                this.dataSource.getUsername(), this.dataSource.getPassword(), DatabaseType.POSTGRES);
        this.dbOperator = new DatabaseOperator(pgDataSource);
    }

    @Override
    public List<DatasetField> getSchema() {
        // Get schema information of table
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresTableExtractor getSchema start. dataSource: {}, database: {}, schema: {}, table: {}",
                    JSONUtils.toJsonString(dataSource), database, schema, table);
        }

        List<DatasetField> fields;

        String sql = "SELECT column_name, udt_name, '' FROM information_schema.columns WHERE table_name = ? AND table_schema = ?";
        fields = dbOperator.fetchAll(sql, rs -> {
            String name = rs.getString(1);
            String type = rs.getString(2);
            String description = rs.getString(3);

            return new DatasetField(name, new DatasetFieldType(convertRawType(type), type), description);
        }, table, schema);

        if (logger.isDebugEnabled()) {
            logger.debug("PostgresTableExtractor getSchema end. fields: {}", JSONUtils.toJsonString(fields));
        }
        return fields;
    }

    @Override
    public DatasetFieldStat getFieldStats(DatasetField datasetField) {
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresTableExtractor getFieldStats start. dataSource: {}, database: {}, schema: {}, table: {}, field: {}",
                    JSONUtils.toJsonString(dataSource), database, schema, table, datasetField.getName());
        }

        DatasetFieldStat.Builder datasetFieldBuilder = DatasetFieldStat.newBuilder();
        datasetFieldBuilder.withName(datasetField.getName()).withStatDate(LocalDateTime.now());


        String distinctCountSql = String.format("SELECT COUNT(DISTINCT(%s)) FROM " + TableOrFieldNameEscapeUtil.escape(table),
                TableOrFieldNameEscapeUtil.escape(datasetField.getName()));
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresTableExtractor.getFieldStats distinctCountSql: {}", distinctCountSql);
        }

        if (isIgnoredType(datasetField.getFieldType().getType())) {
            return datasetFieldBuilder.withDistinctCount(0L).withNonnullCount(0L).build();
        } else if (isSpecialType(datasetField.getFieldType().getType())) {
            distinctCountSql = String.format("SELECT COUNT(DISTINCT(CAST(%s AS VARCHAR))) FROM " + TableOrFieldNameEscapeUtil.escape(table),
                    TableOrFieldNameEscapeUtil.escape(datasetField.getName()));
        }
        // Temporarily close distinct count
        datasetFieldBuilder.withDistinctCount(0L);

        String nonNullCountSql = String.format("SELECT COUNT(*) FROM %s WHERE %s IS NOT NULL", TableOrFieldNameEscapeUtil.escape(table),
                TableOrFieldNameEscapeUtil.escape(datasetField.getName()));
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresTableExtractor.getFieldStats nonnullCountSql: {}", nonNullCountSql);
        }

        datasetFieldBuilder.withNonnullCount(dbOperator.fetchOne(nonNullCountSql,
                rs -> rs.getLong(1)));

        DatasetFieldStat result = datasetFieldBuilder.build();
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresTableExtractor getFieldStats end. datasetField: {}", JSONUtils.toJsonString(result));
        }

        return result;
    }

    private boolean isSpecialType(DatasetFieldType.Type type) {
        return type == DatasetFieldType.Type.JSON;
    }

    private boolean isIgnoredType(DatasetFieldType.Type type) {
        return type == DatasetFieldType.Type.STRUCT;
    }

    @Override
    public DatasetStat getTableStats() {
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresTableExtractor getTableStats start. dataSource: {}, database: {}, schema: {}, table: {}",
                    JSONUtils.toJsonString(dataSource), database, schema, table);
        }

        DatasetStat.Builder datasetStatBuilder = DatasetStat.newBuilder();
        datasetStatBuilder.withStatDate(LocalDateTime.now());

        String sql = "SELECT COUNT(*) FROM " + TableOrFieldNameEscapeUtil.escape(table);
        datasetStatBuilder.withRowCount(dbOperator.fetchOne(sql, rs -> rs.getLong(1)));
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresTableExtractor.getTableStats rowCountSql: {}", sql);
        }

        DatasetStat result = datasetStatBuilder.build();

        if (logger.isDebugEnabled()) {
            logger.debug("PostgresTableExtractor getTableStats end. datasetStat: {}", JSONUtils.toJsonString(result));
        }
        return result;
    }

    @Override
    protected DataStore getDataStore() {
        return new PostgresDataStore(dataSource.getUrl(), database, schema, table);
    }

    @Override
    public String getName() {
        return table;
    }

    @Override
    protected void close() {
        if (pgDataSource instanceof HikariDataSource) {
            ((HikariDataSource) pgDataSource).close();
        }
    }

    private DatasetFieldType.Type convertRawType(String rawType) {
        if ("string".equals(rawType) ||
                rawType.startsWith("varchar") ||
                rawType.startsWith("char") ||
                "text".equals(rawType)) {
            return DatasetFieldType.Type.CHARACTER;
        } else if ("timestamp".equals(rawType) ||
                "date".equals(rawType)) {
            return DatasetFieldType.Type.DATETIME;
        } else if (rawType.startsWith("array")) {
            return DatasetFieldType.Type.ARRAY;
        } else if (rawType.startsWith("decimal") ||
                "double".equals(rawType) ||
                "number".equals(rawType) ||
                rawType.startsWith("int") ||
                "bigint".equals(rawType)) {
            return DatasetFieldType.Type.NUMBER;
        } else if (rawType.startsWith("struct")) {
            return DatasetFieldType.Type.STRUCT;
        } else if ("boolean".equals(rawType) || rawType.equalsIgnoreCase("bool")) {
            return DatasetFieldType.Type.BOOLEAN;
        } else if ("json".equals(rawType) || "jsonb".equals(rawType)) {
            return DatasetFieldType.Type.JSON;
        } else {
            logger.warn("unknown type: {}", rawType);
            return DatasetFieldType.Type.UNKNOW;
        }
    }

}
