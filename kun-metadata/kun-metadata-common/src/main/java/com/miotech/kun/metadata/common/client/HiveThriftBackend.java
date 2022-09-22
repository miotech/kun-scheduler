package com.miotech.kun.metadata.common.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.common.service.FieldMappingService;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.connection.HiveMetaStoreConnectionConfigInfo;
import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.event.MetadataChangeEvent;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class HiveThriftBackend extends BaseMetadataBackend implements StorageBackend {

    private final Logger logger = LoggerFactory.getLogger(HiveThriftBackend.class);

    private final HiveMetaStoreConnectionConfigInfo connectionConfig;

    private final FieldMappingService fieldMappingService;

    private final HiveMetaStoreClient client;

    private final ClientFactory clientFactory;

    public HiveThriftBackend(HiveMetaStoreConnectionConfigInfo connectionConfig, FieldMappingService fieldMappingService,
                             ClientFactory clientFactory) {
        this.connectionConfig = connectionConfig;
        this.fieldMappingService = fieldMappingService;
        this.clientFactory = clientFactory;
        client = clientFactory.getHiveClient(connectionConfig.getMetaStoreUris());
    }

    @Override
    public OffsetDateTime getLastUpdatedTime(Dataset dataset) {
        String location = storageLocation(dataset);
        int idx = location.indexOf('/', location.lastIndexOf(':'));
        String url = location.substring(0, idx);
        String path = location.substring(idx);
        FileSystem fileSystem = create(url + "/" + dataset.getName(), "hdfs");
        try {
            FileStatus fileStatus = fileSystem.getFileStatus(new Path(path));
            return DateTimeUtils.fromTimestamp(fileStatus.getModificationTime());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    public boolean judgeExistence(Dataset dataset, DatasetExistenceJudgeMode judgeMode) {
        try {
            Table table = client.getTable(dataset.getDatabaseName(), dataset.getName());
            return table != null;
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    public List<DatasetField> extract(Dataset dataset) {
        try {
            Table table = client.getTable(dataset.getDatabaseName(), dataset.getName());
            return extractTable(table);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    public Dataset extract(MetadataChangeEvent mce) {
        return extract(mce.getDataSourceId(), mce.getDatabaseName(), mce.getTableName());
    }

    @Override
    public String storageLocation(Dataset dataset) {
        try {
            Table hiveTable = client.getTable(dataset.getDatabaseName(), dataset.getName());
            if (hiveTable == null) {
                throw new NoSuchElementException("Table not found");
            }

            return hiveTable.getSd().getLocation();
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }


    @Override
    public Long getTotalByteSize(Dataset dataset, String location) {
        return null;
    }

    @Override
    public void close() {
        client.close();
    }

    private FileSystem create(String url, Configuration configuration) {
        try {
            return FileSystem.get(URI.create(url), configuration);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private FileSystem create(String url, String user) {
        try {
            Configuration configuration = new Configuration();
            configuration.set("user", user);
            configuration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
            return create(url, configuration);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private List<String> extractTables(String dbName) {
        try {
            return client.getAllTables(dbName);
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private List<DatasetField> extractTable(Table table) {
        try {
            return table.getSd().getCols().stream()
                    .map(schema -> new DatasetField(schema.getName(),
                            new DatasetFieldType(fieldMappingService.parse(DatasourceType.HIVE.name(), schema.getType()), schema.getType()),
                            schema.getComment()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
        }
    }

    private Dataset extract(Long datasourceId, String database, String tableName) {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();

        try {
            Table table = client.getTable(database, tableName);
            DataStore dataStore = new HiveTableStore(table.getSd().getLocation(), database, tableName);
            List<DatasetField> fields = extractTable(table);
            if (logger.isDebugEnabled()) {
                logger.debug("SchemaExtractorTemplate extract getDataStore: {}", DataStoreJsonUtil.toJson(dataStore));
            }

            datasetBuilder.withName(tableName)
                    .withDatasourceId(datasourceId)
                    .withFields(fields)
                    .withDataStore(dataStore);
        } catch (Exception e) {
            logger.error("SchemaExtractorTemplate extract error database: {},table : {}", database, tableName, e);
        }
        return datasetBuilder.build();
    }


    @Override
    protected List<String> searchDatabase(DataSource dataSource) {
        try {
            return client.getAllDatabases();
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    protected List<Dataset> searchDataset(Long datasourceId, String databaseName) {
        List<String> tablesOnMySQL = extractTables(databaseName);
        return tablesOnMySQL.stream().map(tableName -> extract(datasourceId, databaseName, tableName)).collect(Collectors.toList());
    }
}
