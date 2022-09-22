package com.miotech.kun.metadata.common.backend;

import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.client.ClientFactory;
import com.miotech.kun.metadata.common.client.HiveThriftBackend;
import com.miotech.kun.metadata.common.factory.MockConnectionFactory;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.common.factory.MockDatasetFactory;
import com.miotech.kun.metadata.common.service.DataSourceService;
import com.miotech.kun.metadata.common.service.FieldMappingService;
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceBasicInfoRequest;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

public class HiveThriftBackendTest extends DatabaseTestBase {

    @Inject
    private FieldMappingService fieldMappingService;
    @Inject
    private DataSourceService dataSourceService;

    private ClientFactory clientFactory;
    private HiveMetaStoreClient hiveMetaStoreClient;

    @Override
    protected void configuration() {
        super.configuration();
        clientFactory = mock(ClientFactory.class);
        bind(ClientFactory.class, clientFactory);
        hiveMetaStoreClient = mock(HiveMetaStoreClient.class);

    }

    @BeforeEach
    public void init() {
        doReturn(hiveMetaStoreClient).when(clientFactory).getHiveClient(anyString());
    }

    private DataSource mockHiveServerDatasource() {
        HiveMetaStoreConnectionConfigInfo hiveMetaStoreConnectionConfigInfo = new HiveMetaStoreConnectionConfigInfo(ConnectionType.HIVE_THRIFT, "uri");
        ConnectionConfigInfo storageConnectionConfigInfo = new HDFSConnectionConfigInfo(ConnectionType.HDFS);
        ConnectionConfigInfo hiveServerConnectionConfigInfo = new HiveServerConnectionConfigInfo(ConnectionType.HIVE_SERVER, "127.0.0.1", 5000);
        DatasourceConnection datasourceConnection = MockConnectionFactory.createDatasourceConnection(hiveServerConnectionConfigInfo, hiveServerConnectionConfigInfo, hiveMetaStoreConnectionConfigInfo, storageConnectionConfigInfo);
        Map<String, Object> hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 5000);
        DataSourceRequest dataSourceRequest = MockDataSourceFactory.createRequest("hive", hostPortDatasourceConfig, datasourceConnection, DatasourceType.HIVE, new ArrayList<>(), "admin");
        return dataSourceService.create(dataSourceRequest);
    }

    @Test
    public void HiveThriftBackendExtractDataset() throws TException {
        //prepare
        DataSource hive = mockHiveServerDatasource();
        ConnectionConfigInfo connectionConfigInfo = hive.getDatasourceConnection().getMetadataConnection().getConnectionConfigInfo();
        HiveThriftBackend hiveThriftBackend = new HiveThriftBackend((HiveMetaStoreConnectionConfigInfo) connectionConfigInfo, fieldMappingService, clientFactory);
        List<DatasetField> datasetFields = new ArrayList<>();
        DatasetFieldType datasetFieldType = new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "string");
        DatasetField datasetField = DatasetField.newBuilder()
                .withName("column1")
                .withFieldType(datasetFieldType)
                .build();
        datasetFields.add(datasetField);
        Dataset dataset = MockDatasetFactory.createDataset("HiveThriftTable", hive.getId(), "database", datasetFields, DataStoreType.HIVE_TABLE);
        Table table = datasetToHiveTable(dataset);
        doReturn(table).when(hiveMetaStoreClient).getTable(anyString(), anyString());

        List<DatasetField> extractedFileds = hiveThriftBackend.extract(dataset);

        //verify
        MatcherAssert.assertThat(extractedFileds.size(), is(1));
        DatasetField extractedFiled = extractedFileds.get(0);
        MatcherAssert.assertThat(extractedFiled.getName(), is("column1"));
        MatcherAssert.assertThat(datasetField.getFieldType(), is(datasetFieldType));

    }

    @Test
    public void hiveBackendExtractDatasource() throws TException {
        //prepare
        DataSource hive = mockHiveServerDatasource();
        ConnectionConfigInfo connectionConfigInfo = hive.getDatasourceConnection().getMetadataConnection().getConnectionConfigInfo();
        HiveThriftBackend hiveThriftBackend = new HiveThriftBackend((HiveMetaStoreConnectionConfigInfo) connectionConfigInfo, fieldMappingService, clientFactory);
        List<DatasetField> datasetFields = new ArrayList<>();
        DatasetFieldType datasetFieldType = new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "string");
        DatasetField datasetField = DatasetField.newBuilder()
                .withName("column1")
                .withFieldType(datasetFieldType)
                .build();
        datasetFields.add(datasetField);
        Dataset dataset = MockDatasetFactory.createDataset("HiveThriftTable", hive.getId(), "database", datasetFields, DataStoreType.HIVE_TABLE);
        Table table = datasetToHiveTable(dataset);
        doReturn(table).when(hiveMetaStoreClient).getTable(anyString(), anyString());
        doReturn(Collections.singletonList("database")).when(hiveMetaStoreClient).getAllDatabases();
        doReturn(Collections.singletonList("HiveThriftTable")).when(hiveMetaStoreClient).getAllTables("database");

        Iterator<Dataset> datasets = hiveThriftBackend.extract(hive);

        //verify
        MatcherAssert.assertThat(datasets.hasNext(), is(true));
        Dataset extractedDateset = datasets.next();
        MatcherAssert.assertThat(extractedDateset.getName(), is(dataset.getName()));
        MatcherAssert.assertThat(extractedDateset.getDatabaseName(), is(dataset.getDatabaseName()));
        MatcherAssert.assertThat(extractedDateset.getDatasourceId(), is(dataset.getDatasourceId()));
    }


    private Table datasetToHiveTable(Dataset dataset) {
        Table table = new Table();
        table.setTableName(dataset.getName());
        table.setDbName(dataset.getDatabaseName());
        StorageDescriptor storageDescriptor = new StorageDescriptor();
        List<FieldSchema> fieldSchemas = new ArrayList<>();
        List<DatasetField> datasetFields = dataset.getFields();
        for (DatasetField datasetField : datasetFields) {
            FieldSchema fieldSchema = new FieldSchema();
            fieldSchema.setName(datasetField.getName());
            fieldSchema.setType(datasetField.getFieldType().getRawType());
            fieldSchemas.add(fieldSchema);
        }
        storageDescriptor.setCols(fieldSchemas);
        table.setSd(storageDescriptor);
        return table;
    }
}
