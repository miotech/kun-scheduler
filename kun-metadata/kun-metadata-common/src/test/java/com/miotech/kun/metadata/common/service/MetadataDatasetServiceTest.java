package com.miotech.kun.metadata.common.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.dao.DataSourceDao;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.common.factory.MockDatasetFactory;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.dataset.DatasetFieldType;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.vo.DatasetColumnSuggestRequest;
import com.miotech.kun.metadata.core.model.vo.DatasetColumnSuggestResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

@RunWith(Theories.class)
public class MetadataDatasetServiceTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator dbOperator;

    @Inject
    private DataSourceDao dataSourceDao;

    @Inject
    private MetadataDatasetService metadataDatasetService;

    @Before
    public void clearThenInit() {
        // Clear kun_mt_datasource_type, because flyway initializes some data
        dbOperator.update("TRUNCATE TABLE kun_mt_datasource_type");
        // Init test data
        dbOperator.update("INSERT INTO kun_mt_datasource_type (id, name)\n" +
                "VALUES (1, 'Hive'),\n" +
                "       (2, 'MongoDB'),\n" +
                "       (3, 'PostgreSQL'),\n" +
                "       (4, 'Elasticsearch'),\n" +
                "       (5, 'Arango'),\n" +
                "       (6, 'AWS')\n" +
                ";");
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
        // Prepare
        Dataset datasetOfFoo = MockDatasetFactory.createDataset("foo", 1L, "dm", Lists.newArrayList(), "Hive");
        insertDataset(dbOperator, datasetOfFoo);

        Dataset datasetOfBar = MockDatasetFactory.createDataset("bar", 1L, "dw", null, "Hive");
        insertDataset(dbOperator, datasetOfBar);

        Dataset datasetOfBook = MockDatasetFactory.createDataset("book", 1L, "dw", null, "Hive");
        insertDataset(dbOperator, datasetOfBook);

        DataSource dataSource = MockDataSourceFactory.createDataSource(1, "dataSource-1", Maps.newHashMap(), 6L, null);
        dataSourceDao.create(dataSource);

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
        assertTrue(suggestDatabases.isEmpty());
    }

    @Test
    public void testSuggestTable() {
        // Prepare
        Dataset datasetOfFoo = MockDatasetFactory.createDataset("foo", 1L, "dm", Lists.newArrayList(), "Hive");
        insertDataset(dbOperator, datasetOfFoo);

        Dataset datasetOfBar = MockDatasetFactory.createDataset("bar", 1L, "dw", null, "Hive");
        insertDataset(dbOperator, datasetOfBar);

        Dataset datasetOfBook = MockDatasetFactory.createDataset("book", 1L, "dw", null, "Hive");
        insertDataset(dbOperator, datasetOfBook);

        DataSource dataSource = MockDataSourceFactory.createDataSource(1, "dataSource-1", Maps.newHashMap(), 6L, null);
        dataSourceDao.create(dataSource);

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
        assertTrue(suggestTables.isEmpty());
    }

    @Test
    public void testSuggestColumn() {
        // Prepare
        List<DatasetField> datasetFields = Lists.newArrayList(DatasetField.newBuilder().withName("id").withFieldType(new DatasetFieldType(DatasetFieldType.Type.NUMBER, "bigint")).build(),
                DatasetField.newBuilder().withName("name").withFieldType(new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "varchar")).build(),
                DatasetField.newBuilder().withName("note").withFieldType(new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "varchar")).build());
        Dataset datasetOfFoo = MockDatasetFactory.createDataset("foo", 1L, "dm", datasetFields, "Hive");
        insertDataset(dbOperator, datasetOfFoo);

        Dataset datasetOfBar = MockDatasetFactory.createDataset("bar", 1L, "dw", null, "Hive");
        insertDataset(dbOperator, datasetOfBar);

        Dataset datasetOfBook = MockDatasetFactory.createDataset("book", 1L, "dw", null, "Hive");
        insertDataset(dbOperator, datasetOfBook);

        DataSource dataSource = MockDataSourceFactory.createDataSource(1, "dataSource-1", Maps.newHashMap(), 6L, null);
        dataSourceDao.create(dataSource);

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
        assertTrue(datasetColumnSuggestResponse.getColumns().isEmpty());
    }

    @Test
    public void createDatasetDSIExist_should_return_old_one(){
        //prepare
        DataSource dataSource = MockDataSourceFactory.createDataSource(1, "hive", Maps.newHashMap(), 1L, null);
        dataSourceDao.create(dataSource);
        DataStore oldStore = MockDatasetFactory.createDataStore("Hive","test","old");
        Dataset oldSet = metadataDatasetService.createDataSetIfNotExist(oldStore);

        DataStore newStore = MockDatasetFactory.createDataStore("Hive","test","old");
        Dataset newSet = metadataDatasetService.createDataSetIfNotExist(newStore);

        //verify
        assertThat(newSet.getGid(),is(oldSet.getGid()));
        assertThat(newSet.getDatasourceId(),is(oldSet.getDatasourceId()));
        assertThat(newSet.getDSI(),is(oldSet.getDSI()));
    }

    @Test
    public void createDatasetNotExist_should_create_new_one(){
        //prepare
        DataSource dataSource = MockDataSourceFactory.createDataSource(1, "hive", Maps.newHashMap(), 1L, null);
        dataSourceDao.create(dataSource);
        DataStore oldStore = MockDatasetFactory.createDataStore("Hive","test","old");
        Dataset oldSet = metadataDatasetService.createDataSetIfNotExist(oldStore);
        DataStore newStore = MockDatasetFactory.createDataStore("Hive","test","new");

        Dataset newSet = metadataDatasetService.createDataSetIfNotExist(newStore);

        //verify
        assertThat(newSet.getDSI(),not(oldSet.getDSI()));
        assertThat(newSet.getGid(),not(oldSet.getGid()));
        assertThat(newSet.getDatasourceId(),is(dataSource.getId()));
        assertThat(newSet.getDatabaseName(),is(newStore.getDatabaseName()));
        assertThat(newSet.getName(),is(newStore.getName()));


    }

    @DataPoints("datasources")
    public static DataSource[] testDataSources(){
        DataSource hive =  MockDataSourceFactory.createDataSource(1, "hive", Maps.newHashMap(), 1L, null);
        Map<String,Object> mongoConnection = ImmutableMap.of("host","127.0.0.1","port",27017);
        DataSource mongo =  MockDataSourceFactory.createDataSource(2, "mongo", mongoConnection, 2L, null);
        Map<String,Object> pgConnection = ImmutableMap.of("host","127.0.0.1","port",5432);
        DataSource pg =  MockDataSourceFactory.createDataSource(3, "postgres", pgConnection, 3L, null);
        Map<String,Object> esConnection = ImmutableMap.of("host","127.0.0.1","port",9200);
        DataSource es =  MockDataSourceFactory.createDataSource(4, "elasticSearch", esConnection, 4L, null);
        Map<String,Object> arangoConnection = ImmutableMap.of("host","127.0.0.1","port",8529);
        DataSource arango =  MockDataSourceFactory.createDataSource(5, "arango", arangoConnection, 5L, null);
        DataSource[] dataSources = {hive,mongo,pg,es,arango};
        return dataSources;
    }


    private String covertSourceIdToStoreType(int sourceId){
        String sourceType;
        switch (sourceId){
            case 1:
                sourceType = "Hive";
                break;
            case 2:
                sourceType = "MongoDB";
                break;
            case 3:
                sourceType = "PostgreSQL";
                break;
            case 4:
                sourceType = "Elasticsearch";
                break;
            case 5:
                sourceType = "Arango";
                break;
            default:
                throw new IllegalStateException("not support sourceType :"+ sourceId);
        }
        return sourceType;
    }

    @Theory
    public void fetchDatasetByDSI(@FromDataPoints("datasources") DataSource dataSource){
        //prepare
        dataSourceDao.create(dataSource);
        String dataStoreType = covertSourceIdToStoreType(dataSource.getTypeId().intValue());
        DataStore dataStore = MockDatasetFactory.createDataStore(dataStoreType,"test","table");
        Dataset dataset = metadataDatasetService.createDataSetIfNotExist(dataStore);

        Dataset fetched = metadataDatasetService.fetchDataSetByDSI(dataset.getDSI());

        //verify
        assertThat(fetched.getGid(),is(dataset.getGid()));
        assertThat(fetched.getName(),is(dataset.getName()));
        assertThat(fetched.getDatabaseName(),is(dataset.getDatabaseName()));
        assertThat(fetched.getDSI(),is(dataset.getDSI()));
        assertThat(fetched.getDatasourceId(),is(dataset.getDatasourceId()));
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

}
