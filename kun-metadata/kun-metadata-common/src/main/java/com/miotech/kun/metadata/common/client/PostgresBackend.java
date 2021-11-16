package com.miotech.kun.metadata.common.client;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.common.connector.Connector;
import com.miotech.kun.metadata.common.connector.ConnectorFactory;
import com.miotech.kun.metadata.common.connector.Query;
import com.miotech.kun.metadata.common.service.FieldMappingService;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.connection.PostgresConnectionInfo;
import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.event.MetadataChangeEvent;
import com.miotech.kun.workflow.core.model.lineage.PostgresDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;

public class PostgresBackend implements MetadataBackend, StorageBackend {

    private final static Logger logger = LoggerFactory.getLogger(PostgresBackend.class);

    private final PostgresConnectionInfo connectionInfo;
    private final FieldMappingService fieldMappingService;
    private final Connector connector;

    public PostgresBackend(PostgresConnectionInfo connectionInfo, FieldMappingService fieldMappingService) {
        this.connectionInfo = connectionInfo;
        this.connector = ConnectorFactory.generateConnector(connectionInfo);
        this.fieldMappingService = fieldMappingService;
    }

    @Override
    public OffsetDateTime getLastUpdatedTime(Dataset dataset) {
        return null;
    }

    @Override
    public boolean judgeExistence(Dataset dataset, DatasetExistenceJudgeMode judgeMode) {
        String dbName = dataset.getDatabaseName().split("\\.")[0];
        String schemaName = dataset.getDatabaseName().split("\\.")[1];

        ResultSet resultSet = null;

        try {
            String sql = "SELECT COUNT(1) FROM pg_tables WHERE schemaname = '%s' and tablename = '%s'";
            Query query = new Query(dbName,schemaName,String.format(sql,schemaName,dataset.getName()));
            resultSet = connector.query(query);
            long count = 0;
            while (resultSet.next()) {
                count = resultSet.getLong(1);
            }

            return count == 1;
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            connector.close();
        }
    }

    @Override
    public List<DatasetField> extract(Dataset dataset) {
        String dbName = dataset.getDatabaseName().split("\\.")[0];
        String schemaName = dataset.getDatabaseName().split("\\.")[1];
        return extract(dbName, schemaName, dataset.getName());
    }

    @Override
    public Iterator<Dataset> extract(DataSource dataSource) {
        String showDatabases = "SELECT datname FROM pg_database WHERE datistemplate = FALSE";
        Query query = new Query("postgres", null, showDatabases);
        ResultSet resultSet = connector.query(query);
        List<String> databases = Lists.newArrayList();
        try {
            while (resultSet.next()) {
                String databaseName = resultSet.getString("datname");
                databases.add(databaseName);
            }
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }


        return Iterators.concat(databases.stream().map(databasesName ->
                extract(dataSource.getId(), databasesName)).iterator());
    }

    @Override
    public Dataset extract(MetadataChangeEvent mce) {
        throw new IllegalStateException("postgres not support extract by push event yet");
    }

    @Override
    public String storageLocation(Dataset dataset) {
        return dataset.getDataStore().getLocationInfo();
    }

    @Override
    public void close() {
        connector.close();
    }

    @Override
    public Long getTotalByteSize(Dataset dataset, String location) {
        String dbName = dataset.getDatabaseName().split("\\.")[0];
        String schemaName = dataset.getDatabaseName().split("\\.")[1];
        ResultSet statisticsResult = null;

        try {
            String statisticsSQL = "SELECT pg_total_relation_size(pc.oid) FROM pg_class pc JOIN pg_namespace pn on pc.relnamespace = pn.oid WHERE pc.relkind = 'r' AND pn.nspname = '%s' AND pc.relname = '%s'";
            Query query = new Query(dbName, schemaName, String.format(statisticsSQL, schemaName, dataset.getName()));
            statisticsResult = connector.query(query);
            long totalByteSize = 0L;
            while (statisticsResult.next()) {
                totalByteSize = statisticsResult.getLong(1);
            }

            return totalByteSize;
        } catch (SQLException sqlException) {
            throw ExceptionUtils.wrapIfChecked(sqlException);
        }
    }

    private Iterator<Dataset> extract(Long datasourceId, String database) {
        ResultSet resultSet = null;
        try {
            String showSchemas = "SELECT schema_name FROM information_schema.schemata WHERE schema_name NOT LIKE 'pg_%' AND schema_name != 'information_schema'";
            Query query = new Query(database, null, showSchemas);
            resultSet = connector.query(query);
            List<String> schemas = Lists.newArrayList();
            while (resultSet.next()) {
                String schema = resultSet.getString(1);
                schemas.add(schema);
            }

            return Iterators.concat(schemas.stream().map(schema ->
                    extract(datasourceId, database, schema)).iterator());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private Iterator<Dataset> extract(Long datasourceId, String database, String schema) {
        ResultSet resultSet = null;
        try {
            String showTables = "SELECT tablename FROM pg_tables WHERE schemaname = '%s'";
            Query query = new Query(database, null, String.format(showTables, schema));
            resultSet = connector.query(query);
            List<String> tables = Lists.newArrayList();
            while (resultSet.next()) {
                String table = resultSet.getString(1);
                tables.add(table);
            }

            return tables.stream().map(table -> buildDataset(datasourceId, database, schema, table)).iterator();
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private List<DatasetField> extract(String database, String schema, String table) {
        ResultSet primaryResultSet = null, schemaResultSet = null;

        try {
            List<DatasetField> fields = Lists.newArrayList();
            List<String> primaryKeys = Lists.newArrayList();
            String primaryKeySQL = "SELECT a.attname " +
                    "FROM pg_index i " +
                    "JOIN pg_attribute a ON a.attrelid = i.indrelid " +
                    "AND a.attnum = ANY(i.indkey) " +
                    "WHERE i.indrelid = '%s.%s'::regclass " +
                    "AND i.indisprimary";

            Query primaryKeyQuery = new Query(database, schema, String.format(primaryKeySQL, schema, table));
            logger.debug("query primary key database = {},schema={},table={}", database, schema, table);
            primaryResultSet = connector.query(primaryKeyQuery);
            while (primaryResultSet.next()) {
                primaryKeys.add(primaryResultSet.getString(1));
            }

            logger.debug("query schema database = {},schema={},table={}", database, schema, table);
            String schemaSQL = "SELECT column_name, udt_name, '', is_nullable FROM information_schema.columns WHERE table_name = '%s' AND table_schema = '%s'";
            Query schemaQuery = new Query(database, schema, String.format(schemaSQL, table, schema));
            schemaResultSet = connector.query(schemaQuery);

            while (schemaResultSet.next()) {
                String name = schemaResultSet.getString(1);
                String rawType = schemaResultSet.getString(2);
                String description = schemaResultSet.getString(3);
                String isNullable = schemaResultSet.getString(4);

                fields.add(DatasetField.newBuilder()
                        .withName(name)
                        .withFieldType(new DatasetFieldType(fieldMappingService.parse(DatasourceType.POSTGRESQL.name(), rawType), rawType))
                        .withComment(description)
                        .withIsPrimaryKey(primaryKeys.contains(name))
                        .withIsNullable("YES".equals(isNullable))
                        .build()
                );
            }

            return fields;
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private Dataset buildDataset(Long datasourceId, String database, String schema, String table) {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();
        DataStore dataStore = new PostgresDataStore(connectionInfo.getHost(), connectionInfo.getPort(), database, schema, table);
        try {
            List<DatasetField> fields = extract(database, schema, table);
            if (logger.isDebugEnabled()) {
                logger.debug("SchemaExtractorTemplate extract getDataStore: {}", DataStoreJsonUtil.toJson(dataStore));
            }

            datasetBuilder.withName(table)
                    .withDatasourceId(datasourceId)
                    .withFields(fields)
                    .withDataStore(dataStore);
        } catch (Exception e) {
            logger.error("SchemaExtractorTemplate extract error dataStore: {}", dataStore, e);
        }
        return datasetBuilder.build();
    }
}
