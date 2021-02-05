package com.miotech.kun.metadata.databuilder.extract.impl.postgresql;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldType;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.context.ApplicationContext;
import com.miotech.kun.metadata.databuilder.extract.schema.SchemaExtractorTemplate;
import com.miotech.kun.metadata.databuilder.extract.tool.UseDatabaseUtil;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;
import com.miotech.kun.metadata.databuilder.service.fieldmapping.FieldMappingService;
import com.miotech.kun.workflow.core.model.lineage.PostgresDataStore;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class PostgreSQLTableSchemaExtractor extends SchemaExtractorTemplate {

    private final PostgresDataSource postgresDataSource;
    private final String dbName;
    private final String schemaName;
    private final String tableName;
    private final FieldMappingService fieldMappingService;

    public PostgreSQLTableSchemaExtractor(PostgresDataSource postgresDataSource, String dbName, String schemaName, String tableName) {
        super(postgresDataSource.getId());
        Preconditions.checkNotNull(postgresDataSource, "dataSource should not be null.");
        this.postgresDataSource = postgresDataSource;
        this.dbName = dbName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.fieldMappingService = ApplicationContext.getContext().getInjector().getInstance(FieldMappingService.class);
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
                String rawType = schemaResultSet.getString(2);
                String description = schemaResultSet.getString(3);
                String isNullable = schemaResultSet.getString(4);

                fields.add(DatasetField.newBuilder()
                        .withName(name)
                        .withFieldType(new DatasetFieldType(fieldMappingService.parse(postgresDataSource.getId(), rawType), rawType))
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

}