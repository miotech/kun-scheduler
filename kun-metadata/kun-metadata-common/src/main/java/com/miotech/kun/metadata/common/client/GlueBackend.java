package com.miotech.kun.metadata.common.client;

import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.model.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miotech.kun.metadata.common.cataloger.CatalogerConfig;
import com.miotech.kun.metadata.common.service.FieldMappingService;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.connection.GlueConnectionInfo;
import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.event.MetadataChangeEvent;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class GlueBackend extends BaseMetadataBackend {

    private final Logger logger = LoggerFactory.getLogger(GlueBackend.class);

    private final AWSGlue awsGlue;

    private final GlueConnectionInfo glueConnectionInfo;

    private final FieldMappingService fieldMappingService;

    private final ClientFactory clientFactory;

    private static final List<String> CONCERNED_TABLE_TYPES = Lists.newArrayList("MANAGED_TABLE");
    private Map<String, Table> tables = Maps.newHashMap();
    private AtomicBoolean initedCache = new AtomicBoolean(false);

    public GlueBackend(GlueConnectionInfo glueConnectionInfo, FieldMappingService fieldMappingService, ClientFactory clientFactory, CatalogerConfig config) {
        super(config);
        this.glueConnectionInfo = glueConnectionInfo;
        this.fieldMappingService = fieldMappingService;
        this.clientFactory = clientFactory;
        awsGlue = clientFactory.getAWSGlue(glueConnectionInfo.getGlueAccessKey(), glueConnectionInfo.getGlueSecretKey(), glueConnectionInfo.getGlueRegion());

    }

    @Override
    public OffsetDateTime getLastUpdatedTime(Dataset dataset) {
        Table table = searchTable(dataset.getDatabaseName(), dataset.getName());
        return table == null ? null : table.getUpdateTime().toInstant().atZone(ZoneId.of("UTC")).toOffsetDateTime();
    }

    @Override
    public boolean judgeExistence(Dataset dataset, DatasetExistenceJudgeMode judgeMode) {
        Table table = null;
        if (judgeMode.equals(DatasetExistenceJudgeMode.SNAPSHOT)) {
            table = searchTableWithSnapshot(dataset.getDatabaseName(), dataset.getName());
        } else if (judgeMode.equals(DatasetExistenceJudgeMode.DATASET)) {
            table = searchTable(dataset.getDatabaseName(), dataset.getName());
        }
        return table != null;
    }


    @Override
    public List<DatasetField> extract(Dataset dataset) {
        Table table = searchTable(dataset.getDatabaseName(), dataset.getName());
        if (table == null) {
            throw new IllegalStateException("Table not found, gid: " + dataset.getGid());
        }

        return getSchema(table);
    }

    @Override
    public Dataset extract(MetadataChangeEvent mce) {
        Table table = searchTable(mce.getDatabaseName(), mce.getTableName());
        if (table == null) {
            return null;
        }
        return extract(table, mce.getDataSourceId());
    }

    @Override
    public String storageLocation(Dataset dataset) {
        Table table = searchTable(dataset.getDatabaseName(), dataset.getName());
        if (table == null) {
            throw new NoSuchElementException("Table not found");
        }
        return table.getStorageDescriptor().getLocation();
    }

    @Override
    public void close() {
        awsGlue.shutdown();
    }

    @Override
    protected List<String> searchDatabase(DataSource dataSource) {
        GetDatabasesResult databases = awsGlue.getDatabases(new GetDatabasesRequest());
        return databases.getDatabaseList().stream().map(database -> database.getName()).collect(Collectors.toList());
    }

    @Override
    protected List<Dataset> searchDataset(Long datasourceId, String databaseName) {
        List<Table> tables = searchTables(databaseName);
        return tables.stream().map(table -> extract(table, datasourceId)).collect(Collectors.toList());
    }

    private Dataset extract(Table table, Long datasourceId) {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();
        DataStore dataStore = new HiveTableStore(table.getStorageDescriptor().getLocation(), table.getDatabaseName(), table.getName());
        try {
            List<DatasetField> fields = getSchema(table);
            if (logger.isDebugEnabled()) {
                logger.debug("SchemaExtractorTemplate extract getDataStore: {}", DataStoreJsonUtil.toJson(dataStore));
            }

            datasetBuilder.withName(table.getName())
                    .withDatasourceId(datasourceId)
                    .withFields(fields)
                    .withDataStore(dataStore);
        } catch (Exception e) {
            logger.error("SchemaExtractorTemplate extract error dataStore: {}", dataStore, e);
        }
        return datasetBuilder.build();
    }

    private List<DatasetField> getSchema(Table table) {
        List<Column> partitionKeys = table.getPartitionKeys();
        List<Column> columns = table.getStorageDescriptor().getColumns();
        if (CollectionUtils.isNotEmpty(partitionKeys)) {
            columns.addAll(partitionKeys);
        }
        return columns.stream().map(column -> DatasetField.newBuilder()
                .withName(column.getName())
                .withComment(column.getComment())
                .withFieldType(new DatasetFieldType(fieldMappingService.parse(DatasourceType.HIVE.name(), column.getType()), column.getType()))
                .withIsPrimaryKey(false)
                .withIsNullable(true)
                .build()
        ).collect(Collectors.toList());
    }

    private List<Table> searchTables(String databaseName) {

        String nextToken = null;
        List<Table> tables = Lists.newArrayList();
        GetTablesResult getTablesResult;

        do {
            GetTablesRequest getTablesRequest = new GetTablesRequest();
            getTablesRequest.setDatabaseName(databaseName);
            getTablesRequest.setNextToken(nextToken);

            getTablesResult = awsGlue.getTables(getTablesRequest);
            for (Table table : getTablesResult.getTableList()) {
                tables.add(table);
            }
            nextToken = getTablesResult.getNextToken();
        } while (getTablesResult.getNextToken() != null);

        return tables;
    }

    private Table searchTable(String targetDatabase, String targetTable) {
        String nextToken = null;

        do {
            SearchTablesRequest searchTablesRequest = new SearchTablesRequest();
            searchTablesRequest.withNextToken(nextToken);

            List<PropertyPredicate> filters = Lists.newArrayList();

            PropertyPredicate databaseNameFilter = new PropertyPredicate();
            databaseNameFilter.withKey("databaseName");
            databaseNameFilter.withValue(targetDatabase);
            filters.add(databaseNameFilter);

            PropertyPredicate tableNameFilter = new PropertyPredicate();
            tableNameFilter.withKey("name");
            tableNameFilter.withValue(targetTable);
            filters.add(tableNameFilter);

            searchTablesRequest.setFilters(filters);
            SearchTablesResult searchTablesResult = awsGlue.searchTables(searchTablesRequest);
            for (Table table : searchTablesResult.getTableList()) {
                if (targetDatabase.equals(table.getDatabaseName()) && targetTable.equals(table.getName())) {
                    return table;
                }
            }

            nextToken = searchTablesResult.getNextToken();
        } while (StringUtils.isNotBlank(nextToken));

        return null;
    }

    public Table searchTableWithSnapshot(String targetDatabase, String targetTable) {
        if (initedCache.compareAndSet(false, true)) {
            initCache();
        }

        return tables.get(targetDatabase + "." + targetTable);
    }

    private void initCache() {
        String nextToken = null;

        do {
            SearchTablesResult searchTablesResult = awsGlue.searchTables(new SearchTablesRequest().withNextToken(nextToken));

            List<Table> tableList = searchTablesResult.getTableList();
            for (Table table : tableList) {
                tables.put(table.getDatabaseName() + "." + table.getName(), table);
            }

            nextToken = searchTablesResult.getNextToken();
        } while (StringUtils.isNotBlank(nextToken));
    }
}
