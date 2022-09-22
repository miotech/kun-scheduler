package com.miotech.kun.metadata.common.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.factory.MockConnectionFactory;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.common.factory.MockDatasetFactory;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.constant.SearchContent;
import com.miotech.kun.metadata.core.model.dataset.*;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.search.SearchFilterOption;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import com.miotech.kun.metadata.core.model.vo.*;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

public class MetadataDatasetServiceTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator dbOperator;

    @Inject
    private DataSourceService dataSourceService;

    @Inject
    private MetadataDatasetService metadataDatasetService;

    @Inject
    private SearchService searchService;

    @Inject
    private javax.sql.DataSource dataSource;

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
        ((HikariDataSource) dataSource).close();
    }

    @BeforeEach
    public void clearThenInit() {
        // Clear kun_mt_datasource_type, because flyway initializes some data
//        dbOperator.update("TRUNCATE TABLE kun_mt_datasource_type");
    }

    @Test
    public void fetchDatasetByGid_shouldInvokeMetadataDatasetDao() {
        // Prepare
        Dataset dataset = MockDatasetFactory.createDataset(1L);
        insertDataset(dbOperator, dataset);

        // Process
        Long queryGid = 1L;
        Optional<Dataset> datasetOpt = metadataDatasetService.fetchDatasetByGid(queryGid);

        // Validate
        assertTrue(datasetOpt.isPresent());
        Dataset datasetOfFetch = datasetOpt.get();
        assertThat(datasetOfFetch.getGid(), is(queryGid));
    }

    @Test
    public void testSuggestDatabase() {
        DataSource dataSource = mockHiveServerDatasource();
        // Prepare
        Dataset datasetOfFoo = MockDatasetFactory.createDataset("foo", dataSource.getId(), "dm", Lists.newArrayList(), DataStoreType.HIVE_TABLE);
        insertDataset(dbOperator, datasetOfFoo);

        Dataset datasetOfBar = MockDatasetFactory.createDataset("bar", dataSource.getId(), "dw", null, DataStoreType.HIVE_TABLE);
        insertDataset(dbOperator, datasetOfBar);

        Dataset datasetOfBook = MockDatasetFactory.createDataset("book", dataSource.getId(), "dw", null, DataStoreType.HIVE_TABLE);
        insertDataset(dbOperator, datasetOfBook);

        // Execute
        String prefix = "d";
        List<String> suggestDatabases = metadataDatasetService.suggestDatabase(prefix);

        // Validate
        assertThat(suggestDatabases.size(), is(2));
        assertThat(suggestDatabases, containsInAnyOrder("dm", "dw"));

        // Execute
        prefix = "dm";
        suggestDatabases = metadataDatasetService.suggestDatabase(prefix);

        // Validate
        assertThat(suggestDatabases, containsInAnyOrder("dm"));

        // Execute
        prefix = "t";
        suggestDatabases = metadataDatasetService.suggestDatabase(prefix);

        // Validate
        Assertions.assertTrue(suggestDatabases.isEmpty());
    }

    private DataSource mockHiveServerDatasource() {
        HiveMetaStoreConnectionConfigInfo connectionInfo = new HiveMetaStoreConnectionConfigInfo(ConnectionType.HIVE_THRIFT, "uri");
        ConnectionConfigInfo storageConnectionConfigInfo = new HDFSConnectionConfigInfo(ConnectionType.HDFS);
        ConnectionConfigInfo hiveServerConnectionConfigInfo = new HiveServerConnectionConfigInfo(ConnectionType.HIVE_SERVER, "127.0.0.1", 5000);
        DatasourceConnection datasourceConnection = MockConnectionFactory.createDatasourceConnection(hiveServerConnectionConfigInfo, hiveServerConnectionConfigInfo, connectionInfo, storageConnectionConfigInfo);
        Map<String, Object> hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 5000);
        DataSourceRequest dataSourceRequest = MockDataSourceFactory.createRequest("hive", hostPortDatasourceConfig, datasourceConnection, DatasourceType.HIVE, new ArrayList<>(), "admin");
        return dataSourceService.create(dataSourceRequest);
    }

    @Test
    public void testSuggestTable() {
        DataSource dataSource = mockHiveServerDatasource();

        // Prepare
        Dataset datasetOfFoo = MockDatasetFactory.createDataset("foo", dataSource.getId(), "dm", Lists.newArrayList(), DataStoreType.HIVE_TABLE);
        insertDataset(dbOperator, datasetOfFoo);

        Dataset datasetOfBar = MockDatasetFactory.createDataset("bar", dataSource.getId(), "dw", null, DataStoreType.HIVE_TABLE);
        insertDataset(dbOperator, datasetOfBar);

        Dataset datasetOfBook = MockDatasetFactory.createDataset("book", dataSource.getId(), "dw", null, DataStoreType.HIVE_TABLE);
        insertDataset(dbOperator, datasetOfBook);


        // Execute
        List<String> suggestTables = metadataDatasetService.suggestTable("dw", "b");

        // Validate
        assertThat(suggestTables.size(), is(2));
        assertThat(suggestTables, containsInAnyOrder("book", "bar"));

        // Execute
        suggestTables = metadataDatasetService.suggestTable("dw", "bar");

        // Validate
        assertThat(suggestTables.size(), is(1));
        assertThat(suggestTables, containsInAnyOrder("bar"));

        // Execute
        suggestTables = metadataDatasetService.suggestTable("dw", "t");

        // Validate
        Assertions.assertTrue(suggestTables.isEmpty());
    }

    @Test
    public void testSuggestColumn() {
        DataSource dataSource = mockHiveServerDatasource();
        // Prepare
        List<DatasetField> datasetFields = Lists.newArrayList(DatasetField.newBuilder().withName("id").withFieldType(new DatasetFieldType(DatasetFieldType.Type.NUMBER, "bigint")).build(),
                DatasetField.newBuilder().withName("name").withFieldType(new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "varchar")).build(),
                DatasetField.newBuilder().withName("note").withFieldType(new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "varchar")).build());
        Dataset datasetOfFoo = MockDatasetFactory.createDataset("foo", dataSource.getId(), "dm", datasetFields, DataStoreType.HIVE_TABLE);
        insertDataset(dbOperator, datasetOfFoo);

        Dataset datasetOfBar = MockDatasetFactory.createDataset("bar", dataSource.getId(), "dw", null, DataStoreType.HIVE_TABLE);
        insertDataset(dbOperator, datasetOfBar);

        Dataset datasetOfBook = MockDatasetFactory.createDataset("book", dataSource.getId(), "dw", null, DataStoreType.HIVE_TABLE);
        insertDataset(dbOperator, datasetOfBook);


        // Execute
        List<DatasetColumnSuggestRequest> columnSuggestRequests = Lists.newArrayList(new DatasetColumnSuggestRequest("dm", "foo", "n"));
        List<DatasetColumnSuggestResponse> suggestColumns = metadataDatasetService.suggestColumn(columnSuggestRequests);

        // Validate
        assertThat(suggestColumns.size(), is(1));
        assertThat(suggestColumns.get(0).getColumns(), containsInAnyOrder("name", "note"));

        // Execute
        columnSuggestRequests = Lists.newArrayList(new DatasetColumnSuggestRequest("dm", "foo", "name"));
        suggestColumns = metadataDatasetService.suggestColumn(columnSuggestRequests);

        // Validate
        assertThat(suggestColumns.size(), is(1));
        assertThat(suggestColumns.get(0).getColumns(), containsInAnyOrder("name"));

        // Execute
        columnSuggestRequests = Lists.newArrayList(new DatasetColumnSuggestRequest("dm", "foo", "t"));
        suggestColumns = metadataDatasetService.suggestColumn(columnSuggestRequests);

        // Validate
        assertThat(suggestColumns.size(), is(1));
        DatasetColumnSuggestResponse datasetColumnSuggestResponse = suggestColumns.get(0);
        assertThat(datasetColumnSuggestResponse.getDatabaseName(), is("dm"));
        assertThat(datasetColumnSuggestResponse.getTableName(), is("foo"));
        Assertions.assertTrue(datasetColumnSuggestResponse.getColumns().isEmpty());
    }

    @Test
    public void createDatasetDSIExist_should_return_old_one() {
        // Prepare
        mockHiveServerDatasource();
        DataStore oldStore = MockDatasetFactory.createDataStore(DataStoreType.HIVE_TABLE, "test", "old");
        Dataset oldSet = metadataDatasetService.createDataSetIfNotExist(oldStore);

        DataStore newStore = MockDatasetFactory.createDataStore(DataStoreType.HIVE_TABLE, "test", "old");
        Dataset newSet = metadataDatasetService.createDataSetIfNotExist(newStore);

        //verify
        assertThat(newSet.getGid(), is(oldSet.getGid()));
        assertThat(newSet.getDatasourceId(), is(oldSet.getDatasourceId()));
        assertThat(newSet.getDSI(), is(oldSet.getDSI()));
    }

    @Test
    public void createDatasetNotExist_should_create_new_one() {
        // Prepare
        DataSource dataSource = mockHiveServerDatasource();
        DataStore oldStore = MockDatasetFactory.createDataStore(DataStoreType.HIVE_TABLE, "test", "old");
        Dataset oldSet = metadataDatasetService.createDataSetIfNotExist(oldStore);
        DataStore newStore = MockDatasetFactory.createDataStore(DataStoreType.HIVE_TABLE, "test", "new");

        Dataset newSet = metadataDatasetService.createDataSetIfNotExist(newStore);

        //verify
        assertThat(newSet.getDSI(), not(oldSet.getDSI()));
        assertThat(newSet.getGid(), not(oldSet.getGid()));
        assertThat(newSet.getDatasourceId(), is(dataSource.getId()));
        assertThat(newSet.getDatabaseName(), is(newStore.getDatabaseName()));
        assertThat(newSet.getName(), is(newStore.getName()));


    }

    public static Stream<DataSourceRequest> testDataSources() {
        // Prepare
        ConnectionConfigInfo hiveServerConnectionConfigInfo = new HiveServerConnectionConfigInfo(ConnectionType.HIVE_SERVER, "127.0.0.1", 10000);
        Map<String, Object> hive_hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 10000);
        DatasourceConnection hive_datasourceConnection = MockConnectionFactory.createDatasourceConnection(hiveServerConnectionConfigInfo);
        DataSourceRequest hive = MockDataSourceFactory.createRequest("Hive", hive_hostPortDatasourceConfig, hive_datasourceConnection, DatasourceType.HIVE, Lists.newArrayList("test"), "admin");

        ConnectionConfigInfo mongoConnection = new MongoConnectionConfigInfo(ConnectionType.MONGODB, "127.0.0.1", 27017);
        Map<String, Object> mongo_hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 27017);
        DatasourceConnection mongo_datasourceConnection = MockConnectionFactory.createDatasourceConnection(mongoConnection);
        DataSourceRequest mongo = MockDataSourceFactory.createRequest("mongo", mongo_hostPortDatasourceConfig, mongo_datasourceConnection, DatasourceType.MONGODB, null, "admin");

        ConnectionConfigInfo pgConnection = new PostgresConnectionConfigInfo(ConnectionType.POSTGRESQL, "127.0.0.1", 5432);
        Map<String, Object> pg_hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 5432);
        DatasourceConnection pg_datasourceConnection = MockConnectionFactory.createDatasourceConnection(pgConnection);
        DataSourceRequest pg = MockDataSourceFactory.createRequest("postgres", pg_hostPortDatasourceConfig, pg_datasourceConnection, DatasourceType.POSTGRESQL, null, "admin");

        ConnectionConfigInfo arangoConnection = new ArangoConnectionConfigInfo(ConnectionType.ARANGO, "127.0.0.1", 8529);
        Map<String, Object> arango_hostPortDatasourceConfig = MockDataSourceFactory.createHostPortDatasourceConfig("127.0.0.1", 8529);
        DatasourceConnection arango_datasourceConnection = MockConnectionFactory.createDatasourceConnection(arangoConnection);
        DataSourceRequest arango = MockDataSourceFactory.createRequest("arango", arango_hostPortDatasourceConfig, arango_datasourceConnection, DatasourceType.ARANGO, null, "admin");

        return Stream.of(hive, mongo, pg, arango);
    }


    private DataStoreType covertSourceTypeToStoreType(DatasourceType sourceType) {
        DataStoreType storeType;
        switch (sourceType) {
            case HIVE:
                storeType = DataStoreType.HIVE_TABLE;
                break;
            case MONGODB:
                storeType = DataStoreType.MONGO_COLLECTION;
                break;
            case POSTGRESQL:
                storeType = DataStoreType.POSTGRES_TABLE;
                break;
            case ELASTICSEARCH:
                storeType = DataStoreType.ELASTICSEARCH_INDEX;
                break;
            case ARANGO:
                storeType = DataStoreType.ARANGO_COLLECTION;
                break;
            default:
                throw new IllegalStateException("not support sourceType :" + sourceType);
        }
        return storeType;
    }

    @ParameterizedTest
    @MethodSource("testDataSources")
    public void fetchDatasetByDSI(DataSourceRequest dataSourceRequest) {
        //prepare
        DataSource dataSource = dataSourceService.create(dataSourceRequest);
        DataStoreType dataStoreType = covertSourceTypeToStoreType(dataSource.getDatasourceType());
        DataStore dataStore = MockDatasetFactory.createDataStore(dataStoreType, "test", "table");
        Dataset dataset = metadataDatasetService.createDataSetIfNotExist(dataStore);

        Dataset fetched = metadataDatasetService.fetchDataSetByDSI(dataset.getDSI());

        //verify
        assertThat(fetched.getGid(), is(dataset.getGid()));
        assertThat(fetched.getName(), is(dataset.getName()));
        assertThat(fetched.getDatabaseName(), is(dataset.getDatabaseName()));
        assertThat(fetched.getDSI(), is(dataset.getDSI()));
        assertThat(fetched.getDatasourceId(), is(dataset.getDatasourceId()));
    }


    @Test
    public void createHiveDataSet_dataset_name_should_be_lowerCase() {
        //prepare
        String tableName = "UpperCaseTable";
        mockHiveServerDatasource();
        DataStore dataStore = MockDatasetFactory.createDataStore(DataStoreType.HIVE_TABLE, "test", tableName);
        Dataset dataset = metadataDatasetService.createDataSetIfNotExist(dataStore);

        //verify
        assertThat(dataset.getName(), is(tableName.toLowerCase()));
        assertThat(dataset.getDatabaseName(), is("test"));
        assertThat(dataset.getDatasourceId(), is(notNullValue()));
    }

    private void insertDataset(DatabaseOperator dbOperator, Dataset dataset) {
        dbOperator.update("INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name, dsi, deleted) VALUES(?, ?, ?, CAST(? AS JSONB), ?, ?, ?)",
                dataset.getGid(), dataset.getName(), dataset.getDatasourceId(), DataStoreJsonUtil.toJson(dataset.getDataStore()),
                dataset.getDatabaseName(),
                dataset.getDSI(),
                dataset.isDeleted()
        );

        if (CollectionUtils.isNotEmpty(dataset.getFields())) {
            for (DatasetField field : dataset.getFields()) {
                dbOperator.update("INSERT INTO kun_mt_dataset_field(dataset_gid, name, type) VALUES(?, ?, ?)", dataset.getGid(), field.getName(), field.getFieldType().getType().toValue());
            }
        }
    }

    @Test
    public void test_updateSearchInfo() {
        DataSource dataSource = mockHiveServerDatasource();
        // Prepare
        Dataset dataset1 = MockDatasetFactory.createDataset("test-1", dataSource.getId(), "default", DataStoreType.HIVE_TABLE);
        Dataset dataset2 = MockDatasetFactory.createDataset("test-2", dataSource.getId(), "default", DataStoreType.HIVE_TABLE);
        Dataset dataSetR1 = metadataDatasetService.createDataSet(dataset1);
        Dataset dataSetR2 = metadataDatasetService.createDataSet(dataset2);
        ArrayList<String> owners = Lists.newArrayList("test-user", "dev-user");
        ArrayList<String> tags = Lists.newArrayList("hive", "test");
        metadataDatasetService.updateDataset(dataSetR1.getGid(), MockDatasetFactory.createDatasetUpdateRequest(dataSetR1.getGid(), owners, tags));
        metadataDatasetService.updateDataset(dataSetR2.getGid(), MockDatasetFactory.createDatasetUpdateRequest(dataSetR2.getGid(), owners, tags));


        List<SearchFilterOption> searchFilterOptionList = Arrays.stream(new String[]{"test"})
                .map(s -> SearchFilterOption.Builder.newBuilder()
                        .withSearchContents(Sets.newHashSet(SearchContent.values()))
                        .withKeyword(s).build())
                .collect(Collectors.toList());
        UniversalSearchRequest request = new UniversalSearchRequest();
        request.setSearchFilterOptions(searchFilterOptionList);
        UniversalSearchInfo search = searchService.search(request);
        assertThat(search, is(notNullValue()));
        List<SearchedInfo> searchedInfoList = search.getSearchedInfoList();
        assertThat(searchedInfoList, is(notNullValue()));
        assertThat(searchedInfoList.size(), is(2));


    }

}
