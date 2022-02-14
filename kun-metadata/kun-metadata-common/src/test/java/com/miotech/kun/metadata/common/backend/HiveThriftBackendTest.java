package com.miotech.kun.metadata.common.backend;

import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.cataloger.CatalogerConfig;
import com.miotech.kun.metadata.common.client.ClientFactory;
import com.miotech.kun.metadata.common.client.HiveThriftBackend;
import com.miotech.kun.metadata.common.dao.DataSourceDao;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.common.factory.MockDatasetFactory;
import com.miotech.kun.metadata.common.service.FieldMappingService;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import com.miotech.kun.metadata.core.model.connection.HiveMetaStoreConnectionInfo;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

public class HiveThriftBackendTest extends DatabaseTestBase {

    @Inject
    private FieldMappingService fieldMappingService;
    @Inject
    private DataSourceDao dataSourceDao;

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

    @Test
    public void HiveThriftBackendExtractDataset() throws TException {
        //prepare
        HiveMetaStoreConnectionInfo connectionInfo = new HiveMetaStoreConnectionInfo(ConnectionType.HIVE_THRIFT, "uri");
        HiveThriftBackend hiveThriftBackend = new HiveThriftBackend(connectionInfo, fieldMappingService, clientFactory, CatalogerConfig.newBuilder().build());
        List<DatasetField> datasetFields = new ArrayList<>();
        DatasetFieldType datasetFieldType = new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "string");
        DatasetField datasetField = DatasetField.newBuilder()
                .withName("column1")
                .withFieldType(datasetFieldType)
                .build();
        datasetFields.add(datasetField);
        Dataset dataset = MockDatasetFactory.createDataset("HiveThriftTable", 1l, "database", datasetFields, "Hive");
        Table table = datasetToHiveTable(dataset);
        doReturn(table).when(hiveMetaStoreClient).getTable(anyString(),anyString());

        List<DatasetField> extractedFileds = hiveThriftBackend.extract(dataset);

        //verify
        assertThat(extractedFileds.size(), is(1));
        DatasetField extractedFiled = extractedFileds.get(0);
        assertThat(extractedFiled.getName(), is("column1"));
        assertThat(datasetField.getFieldType(), is(datasetFieldType));

    }

    @Test
    public void hiveBackendExtractDatasource() throws TException {
        //prepare
        HiveMetaStoreConnectionInfo connectionInfo = new HiveMetaStoreConnectionInfo(ConnectionType.HIVE_THRIFT, "uri");
        HiveThriftBackend hiveThriftBackend = new HiveThriftBackend(connectionInfo, fieldMappingService, clientFactory, CatalogerConfig.newBuilder().build());
        DataSource hive = MockDataSourceFactory.createDataSource(1, "hive", connectionInfo, DatasourceType.HIVE, new ArrayList<>());
        dataSourceDao.create(hive);
        List<DatasetField> datasetFields = new ArrayList<>();
        DatasetFieldType datasetFieldType = new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "string");
        DatasetField datasetField = DatasetField.newBuilder()
                .withName("column1")
                .withFieldType(datasetFieldType)
                .build();
        datasetFields.add(datasetField);
        Dataset dataset = MockDatasetFactory.createDataset("HiveThriftTable", 1l, "database", datasetFields, "Hive");
        Table table = datasetToHiveTable(dataset);
        doReturn(table).when(hiveMetaStoreClient).getTable(anyString(),anyString());
        doReturn(Arrays.asList("database")).when(hiveMetaStoreClient).getAllDatabases();
        doReturn(Arrays.asList("HiveThriftTable")).when(hiveMetaStoreClient).getAllTables("database");

        Iterator<Dataset> datasets = hiveThriftBackend.extract(hive);



        //verify
        assertThat(datasets.hasNext(), is(true));
        Dataset extractedDateset = datasets.next();
        assertThat(extractedDateset.getName(), is(dataset.getName()));
        assertThat(extractedDateset.getDatabaseName(), is(dataset.getDatabaseName()));
        assertThat(extractedDateset.getDatasourceId(), is(dataset.getDatasourceId()));
    }


    private Table datasetToHiveTable(Dataset dataset){
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
