package com.miotech.kun.metadata.databuilder.extract.impl.postgresql;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldType;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.schema.SchemaExtractorTemplate;
import com.miotech.kun.metadata.databuilder.extract.tool.UseDatabaseUtil;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;
import com.miotech.kun.workflow.core.model.lineage.PostgresDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class PostgreSQLTableSchemaExtractor extends SchemaExtractorTemplate {
    private static Logger logger = LoggerFactory.getLogger(PostgreSQLTableSchemaExtractor.class);

    private final PostgresDataSource postgresDataSource;
    private final String dbName;
    private final String schemaName;
    private final String tableName;

    public PostgreSQLTableSchemaExtractor(PostgresDataSource postgresDataSource, String dbName, String schemaName, String tableName) {
        super(postgresDataSource.getId());
        Preconditions.checkNotNull(postgresDataSource, "dataSource should not be null.");
        this.postgresDataSource = postgresDataSource;
        this.dbName = dbName;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    @Override
    public List<DatasetField> getSchema() {
        Connection connection = null;
        Statement primaryKeyStatement = null, schemaStatement = null;
        ResultSet primaryResultSet = null, schemaResultSet = null;

        try {
            List<DatasetField> fields = Lists.newArrayList();
            connection = JDBCClient.getConnection(UseDatabaseUtil.useSchema(postgresDataSource.getUrl(), dbName, schemaName),
                    this.postgresDataSource.getUsername(), this.postgresDataSource.getPassword(), DatabaseType.POSTGRES);

            List<String> primaryKeys = Lists.newArrayList();
            String primaryKeySQL = "SELECT a.attname " +
                    "FROM pg_index i " +
                    "JOIN pg_attribute a ON a.attrelid = i.indrelid " +
                    "AND a.attnum = ANY(i.indkey) " +
                    "WHERE i.indrelid = '%s.%s'::regclass " +
                    "AND i.indisprimary";

            primaryKeyStatement = connection.createStatement();
            primaryResultSet = primaryKeyStatement.executeQuery(String.format(primaryKeySQL, schemaName, tableName));
            while (primaryResultSet.next()) {
                primaryKeys.add(primaryResultSet.getString(1));
            }

            String schemaSQL = "SELECT column_name, udt_name, '', is_nullable FROM information_schema.columns WHERE table_name = '%s' AND table_schema = '%s'";
            schemaStatement = connection.createStatement();
            schemaResultSet = schemaStatement.executeQuery(String.format(schemaSQL, tableName, schemaName));
            while (schemaResultSet.next()) {
                String name = schemaResultSet.getString(1);
                String type = schemaResultSet.getString(2);
                String description = schemaResultSet.getString(3);
                String isNullable = schemaResultSet.getString(4);

                fields.add(DatasetField.newBuilder()
                        .withName(name)
                        .withFieldType(new DatasetFieldType(convertRawType(type), type))
                        .withComment(description)
                        .withIsPrimaryKey(primaryKeys.contains(name))
                        .withIsNullable("YES".equals(isNullable))
                        .build()
                );
            }

            return fields;
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            JDBCClient.close(connection, primaryKeyStatement, schemaStatement, primaryResultSet, schemaResultSet);
        }
    }

    @Override
    protected DataStore getDataStore() {
        return new PostgresDataStore(postgresDataSource.getUrl(), dbName, schemaName, tableName);
    }

    @Override
    public String getName() {
        return tableName;
    }

    @Override
    protected void close() {
        // do nothing
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