package com.miotech.kun.metadata.common.backend;

import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.common.client.ClientFactory;
import com.miotech.kun.metadata.common.client.GlueBackend;
import com.miotech.kun.metadata.common.factory.MockConnectionFactory;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.common.factory.MockDatasetFactory;
import com.miotech.kun.metadata.common.service.DataSourceService;
import com.miotech.kun.metadata.common.service.FieldMappingService;
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.dataset.*;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.event.MetadataChangeEvent;
import com.miotech.kun.metadata.core.model.vo.DataSourceBasicInfoRequest;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

public class GlueBackendTest extends DatabaseTestBase {


    @Inject
    private FieldMappingService fieldMappingService;
    @Inject
    private DataSourceService dataSourceService;

    private ClientFactory clientFactory;
    private AWSGlue awsGlue;
    private HiveMetaStoreClient hiveMetaStoreClient;
    private AmazonS3 s3Client;

    @Override
    protected void configuration() {
        super.configuration();
        clientFactory = mock(ClientFactory.class);
        bind(ClientFactory.class, clientFactory);
        awsGlue = mock(AWSGlue.class);
        hiveMetaStoreClient = mock(HiveMetaStoreClient.class);
        s3Client = mock(AmazonS3.class);
    }

    @BeforeEach
    public void init() {
        doReturn(awsGlue).when(clientFactory).getAWSGlue(anyString(), anyString(), anyString());
        doReturn(hiveMetaStoreClient).when(clientFactory).getHiveClient(anyString());
        doReturn(s3Client).when(clientFactory).getAmazonS3Client(anyString(), anyString(), anyString());
    }

    @Test
    public void glueBackendExtractDataset() {
        //prepare
        DataSource hive = mockHiveGlueDatasource();
        ConnectionConfigInfo connectionConfigInfo = hive.getDatasourceConnection().getMetadataConnection().getConnectionConfigInfo();
        GlueBackend glueBackend = new GlueBackend((GlueConnectionConfigInfo) connectionConfigInfo, fieldMappingService, clientFactory);
        List<DatasetField> datasetFields = new ArrayList<>();
        DatasetFieldType datasetFieldType = new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "string");
        DatasetField datasetField = DatasetField.newBuilder()
                .withName("column1")
                .withFieldType(datasetFieldType)
                .build();
        datasetFields.add(datasetField);
        Dataset dataset = MockDatasetFactory.createDataset("glueTable", hive.getId(), "database", datasetFields, DataStoreType.HIVE_TABLE);
        SearchTablesResult searchTablesResult = prepareSearchTable(Collections.singletonList(dataset));
        doReturn(searchTablesResult).when(awsGlue).searchTables(any(SearchTablesRequest.class));

        List<DatasetField> extractedFileds = glueBackend.extract(dataset);

        //verify
        MatcherAssert.assertThat(extractedFileds.size(), is(1));
        DatasetField extractedFiled = extractedFileds.get(0);
        MatcherAssert.assertThat(extractedFiled.getName(), is("column1"));
        MatcherAssert.assertThat(datasetField.getFieldType(), is(datasetFieldType));

    }

    @Test
    public void glueBackendLastUpdatedTime() {
        //prepare
        DataSource hive = mockHiveGlueDatasource();
        ConnectionConfigInfo connectionConfigInfo = hive.getDatasourceConnection().getMetadataConnection().getConnectionConfigInfo();
        GlueBackend glueBackend = new GlueBackend((GlueConnectionConfigInfo) connectionConfigInfo, fieldMappingService, clientFactory);
        List<DatasetField> datasetFields = new ArrayList<>();
        OffsetDateTime expectedTime = DateTimeUtils.fromISODateTimeString("2021-11-03T10:15:30+00:00");
        TableStatistics tableStatistics = TableStatistics
                .newBuilder()
                .withLastUpdatedTime(expectedTime)
                .build();
        Dataset dataset = MockDatasetFactory.createDataset("glueTable", hive.getId(), "database", datasetFields, DataStoreType.HIVE_TABLE)
                .cloneBuilder()
                .withTableStatistics(tableStatistics)
                .build();
        SearchTablesResult searchTablesResult = prepareSearchTable(Collections.singletonList(dataset));
        doReturn(searchTablesResult).when(awsGlue).searchTables(any(SearchTablesRequest.class));

        OffsetDateTime updatedTime = glueBackend.getLastUpdatedTime(dataset);

        //verify
        MatcherAssert.assertThat(updatedTime, is(expectedTime));

    }

    @Test
    public void glueBackendJudgeExistence() {
        //prepare
        DataSource hive = mockHiveGlueDatasource();
        ConnectionConfigInfo connectionConfigInfo = hive.getDatasourceConnection().getMetadataConnection().getConnectionConfigInfo();
        GlueBackend glueBackend = new GlueBackend((GlueConnectionConfigInfo) connectionConfigInfo, fieldMappingService, clientFactory);
        List<DatasetField> datasetFields = new ArrayList<>();
        OffsetDateTime expectedTime = DateTimeUtils.fromISODateTimeString("2021-11-03T10:15:30+00:00");
        TableStatistics tableStatistics = TableStatistics
                .newBuilder()
                .withLastUpdatedTime(expectedTime)
                .build();
        Dataset dataset = MockDatasetFactory.createDataset("glueTable", hive.getId(), "database", datasetFields, DataStoreType.HIVE_TABLE)
                .cloneBuilder()
                .withTableStatistics(tableStatistics)
                .build();
        SearchTablesResult searchTablesResult = prepareSearchTable(Collections.singletonList(dataset));
        doReturn(searchTablesResult).when(awsGlue).searchTables(any(SearchTablesRequest.class));

        boolean isExist = glueBackend.judgeExistence(dataset, DatasetExistenceJudgeMode.DATASET);

        //verify
        MatcherAssert.assertThat(isExist, is(true));

    }

    @Test
    public void glueBackendExtractMceEvent() {
        //prepare
        DataSource hive = mockHiveGlueDatasource();
        ConnectionConfigInfo connectionConfigInfo = hive.getDatasourceConnection().getMetadataConnection().getConnectionConfigInfo();
        GlueBackend glueBackend = new GlueBackend((GlueConnectionConfigInfo) connectionConfigInfo, fieldMappingService, clientFactory);
        List<DatasetField> datasetFields = new ArrayList<>();
        OffsetDateTime expectedTime = DateTimeUtils.fromISODateTimeString("2021-11-03T10:15:30+00:00");
        TableStatistics tableStatistics = TableStatistics
                .newBuilder()
                .withLastUpdatedTime(expectedTime)
                .build();
        Dataset dataset = MockDatasetFactory.createDataset("glueTable", hive.getId(), "database", datasetFields, DataStoreType.HIVE_TABLE)
                .cloneBuilder()
                .withTableStatistics(tableStatistics)
                .build();
        MetadataChangeEvent metadataChangeEvent = MetadataChangeEvent
                .newBuilder()
                .withDatabaseName(dataset.getDatabaseName())
                .withEventType(MetadataChangeEvent.EventType.CREATE_TABLE)
                .withDataSourceType(MetadataChangeEvent.DataSourceType.GLUE)
                .withTableName(dataset.getName())
                .withDataSourceId(dataset.getDatasourceId())
                .build();
        SearchTablesResult searchTablesResult = prepareSearchTable(Collections.singletonList(dataset));
        doReturn(searchTablesResult).when(awsGlue).searchTables(any(SearchTablesRequest.class));

        Dataset extractedDateset = glueBackend.extract(metadataChangeEvent);

        //verify
        MatcherAssert.assertThat(extractedDateset.getName(), is(dataset.getName()));
        MatcherAssert.assertThat(extractedDateset.getDatabaseName(), is(dataset.getDatabaseName()));
        MatcherAssert.assertThat(extractedDateset.getDatasourceId(), is(dataset.getDatasourceId()));

    }

    @Test
    public void glueBackendExtractDatasource() {
        //prepare
        DataSource hive = mockHiveGlueDatasource();
        ConnectionConfigInfo connectionConfigInfo = hive.getDatasourceConnection().getMetadataConnection().getConnectionConfigInfo();
        GlueBackend glueBackend = new GlueBackend((GlueConnectionConfigInfo) connectionConfigInfo, fieldMappingService, clientFactory);
        List<DatasetField> datasetFields = new ArrayList<>();
        Dataset dataset = MockDatasetFactory.createDataset("glueTable", hive.getId(), "database", datasetFields, DataStoreType.HIVE_TABLE);
        List<Dataset> datasetList = new ArrayList<>();
        datasetList.add(dataset);
        SearchTablesResult searchTablesResult = prepareSearchTable(datasetList);
        doReturn(searchTablesResult).when(awsGlue).searchTables(any(SearchTablesRequest.class));
        doReturn(prepareGlueDatabase(datasetList)).when(awsGlue).getDatabases(any(GetDatabasesRequest.class));
        doReturn(prepareGlueTable(datasetList)).when(awsGlue).getTables(any(GetTablesRequest.class));

        Iterator<Dataset> datasets = glueBackend.extract(hive);

        //verify
        MatcherAssert.assertThat(datasets.hasNext(), is(true));
        Dataset extractedDateset = datasets.next();
        MatcherAssert.assertThat(extractedDateset.getName(), is(dataset.getName()));
        MatcherAssert.assertThat(extractedDateset.getDatabaseName(), is(dataset.getDatabaseName()));
        MatcherAssert.assertThat(extractedDateset.getDatasourceId(), is(dataset.getDatasourceId()));

    }

    private DataSource mockHiveGlueDatasource() {
        GlueConnectionConfigInfo connectionInfo = new GlueConnectionConfigInfo(ConnectionType.GLUE, "asskey", "secretkey", "region");
        ConnectionConfigInfo storageConnectionConfigInfo = new S3ConnectionConfigInfo(ConnectionType.S3, "glue", "glue", "glue");
        ConnectionConfigInfo athenaConnectionConfigInfo = new AthenaConnectionConfigInfo(ConnectionType.ATHENA, "jdbc:awsathena", "user", "password");
        DatasourceConnection datasourceConnection = MockConnectionFactory.createDatasourceConnection(athenaConnectionConfigInfo, athenaConnectionConfigInfo, connectionInfo, storageConnectionConfigInfo);
        Map<String, Object> athenaDatasourceConfig = MockDataSourceFactory.createAthenaDatasourceConfig("jdbc:awsathena");
        DataSourceRequest dataSourceRequest = MockDataSourceFactory.createRequest("hive", athenaDatasourceConfig, datasourceConnection, DatasourceType.HIVE, new ArrayList<>(), "admin");
        return dataSourceService.create(dataSourceRequest);
    }

    private GetTablesResult prepareGlueTable(List<Dataset> datasets) {
        GetTablesResult tablesResult = new GetTablesResult();
        List<Table> tableList = new ArrayList<>();
        for (Dataset dataset : datasets) {
            Table table = datasetToGlueTable(dataset);
            tableList.add(table);
        }
        tablesResult.setTableList(tableList);
        return tablesResult;
    }

    private GetDatabasesResult prepareGlueDatabase(List<Dataset> datasets) {
        GetDatabasesResult databasesResult = new GetDatabasesResult();
        List<Database> databaseList = new ArrayList<>();
        for (Dataset dataset : datasets) {
            Database database = new Database();
            database.setName(dataset.getDatabaseName());
            databaseList.add(database);
        }
        databasesResult.setDatabaseList(databaseList);
        return databasesResult;
    }

    private SearchTablesResult prepareSearchTable(List<Dataset> datasets) {
        List<Table> tableList = new ArrayList<>();
        for (Dataset dataset : datasets) {
            Table table = datasetToGlueTable(dataset);
            tableList.add(table);
        }
        SearchTablesResult searchTablesResult = new SearchTablesResult();
        searchTablesResult.setTableList(tableList);
        return searchTablesResult;
    }

    private Table datasetToGlueTable(Dataset dataset) {
        Table table = new Table();
        table.setName(dataset.getName());
        table.setDatabaseName(dataset.getDatabaseName());
        StorageDescriptor storageDescriptor = new StorageDescriptor();
        List<Column> columns = new ArrayList<>();
        List<DatasetField> datasetFields = dataset.getFields();
        for (DatasetField datasetField : datasetFields) {
            Column column = new Column();
            column.setName(datasetField.getName());
            column.setType(datasetField.getFieldType().getRawType());
            columns.add(column);
        }
        storageDescriptor.setColumns(columns);
        table.setStorageDescriptor(storageDescriptor);
        if (dataset.getTableStatistics() != null) {
            OffsetDateTime updateTime = dataset.getTableStatistics().getLastUpdatedTime();
            table.setUpdateTime(new Date(updateTime.toInstant().getEpochSecond() * 1000));
        }
        return table;
    }
}
