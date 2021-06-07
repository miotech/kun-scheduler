package com.miotech.kun.metadata.common.service;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.common.factory.MockDatasetFactory;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.*;
import com.miotech.kun.workflow.core.model.lineage.PostgresDataStore;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class MetadataDatasetServiceTest extends DatabaseTestBase {

    @Inject
    private DatabaseOperator dbOperator;

    @Inject
    private MetadataDatasetService metadataDatasetService;

    @Test
    public void fetchDatasetByGid_shouldInvokeMetadataDatasetDao() {
        // Prepare
        DataStore dataStore = new PostgresDataStore("127.0.0.1", 5432, "postgres", "public", "foo");
        dbOperator.update("INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name, dsi, deleted) VALUES(?, ?, ?, CAST(? AS JSONB), ?, ?, ?)",
                1L, "foo",
                1L,
                DataStoreJsonUtil.toJson(dataStore),
                "postgres",
                dataStore.getDSI().toFullString(),
                false
        );

        // Process
        Long queryGid = 1L;
        Optional<Dataset> datasetOpt = metadataDatasetService.fetchDatasetByGid(queryGid);

        // Validate
        assertTrue(datasetOpt.isPresent());
        Dataset dataset = datasetOpt.get();
        assertThat(dataset.getGid(), is(queryGid));
    }

    @Test
    public void testHindDatabase() {
        // Prepare
        prepareTestData();

        // Execute
        String prefix = "d";
        List<String> hintDatabases = metadataDatasetService.hintDatabase(prefix);

        // Validate
        assertThat(hintDatabases.size(), is(2));
        assertThat(hintDatabases, containsInAnyOrder("dm", "dw"));

        // Execute
        prefix = "dm";
        hintDatabases = metadataDatasetService.hintDatabase(prefix);

        // Validate
        assertThat(hintDatabases, containsInAnyOrder("dm"));

        prefix = "t";
        hintDatabases = metadataDatasetService.hintDatabase(prefix);

        // Validate
        assertTrue(hintDatabases.isEmpty());
    }

    @Test
    public void testHintTable() {
        // Prepare
        prepareTestData();

        // Execute
        List<String> hintTables = metadataDatasetService.hintTable("dw", "b");

        // Validate
        assertThat(hintTables.size(), is(2));
        assertThat(hintTables, containsInAnyOrder("book", "bar"));

        // Execute
        hintTables = metadataDatasetService.hintTable("dw", "bar");

        // Validate
        assertThat(hintTables.size(), is(1));
        assertThat(hintTables, containsInAnyOrder("bar"));

        // Execute
        hintTables = metadataDatasetService.hintTable("dw", "t");

        // Validate
        assertTrue(hintTables.isEmpty());
    }

    @Test
    public void testHintColumn() {
        // Prepare
        prepareTestData();

        // Execute
        List<DatasetColumnHintRequest> columnHintRequests = Lists.newArrayList(new DatasetColumnHintRequest("dm", "foo", "n"));
        List<DatasetColumnHintResponse> hintColumns = metadataDatasetService.hintColumn(columnHintRequests);

        // Validate
        assertThat(hintColumns.size(), is(1));
        assertThat(hintColumns.get(0).getColumns(), containsInAnyOrder("name", "note"));

        // Execute
        columnHintRequests = Lists.newArrayList(new DatasetColumnHintRequest("dm", "foo", "name"));
        hintColumns = metadataDatasetService.hintColumn(columnHintRequests);

        // Validate
        assertThat(hintColumns.size(), is(1));
        assertThat(hintColumns.get(0).getColumns(), containsInAnyOrder("name"));

        // Execute
        columnHintRequests = Lists.newArrayList(new DatasetColumnHintRequest("dm", "foo", "t"));
        hintColumns = metadataDatasetService.hintColumn(columnHintRequests);

        // Validate
        assertThat(hintColumns.size(), is(1));
        DatasetColumnHintResponse datasetColumnHintResponse = hintColumns.get(0);
        assertThat(datasetColumnHintResponse.getDatabaseName(), is("dm"));
        assertThat(datasetColumnHintResponse.getTableName(), is("foo"));
        assertTrue(datasetColumnHintResponse.getColumns().isEmpty());
    }

    private void prepareTestData() {
        List<DatasetField> datasetFields = Lists.newArrayList(DatasetField.newBuilder().withName("id").withFieldType(new DatasetFieldType(DatasetFieldType.Type.NUMBER, "bigint")).build(),
                DatasetField.newBuilder().withName("name").withFieldType(new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "varchar")).build(),
                DatasetField.newBuilder().withName("note").withFieldType(new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "varchar")).build());
        Dataset datasetOfFoo = MockDatasetFactory.createDataset(1L, "foo", 1L, "dm", datasetFields, "Hive");
        MockDatasetFactory.insertDataset(dbOperator, datasetOfFoo);


        Dataset datasetOfBar = MockDatasetFactory.createDataset(2L, "bar", 1L, "dw", null, "Hive");
        MockDatasetFactory.insertDataset(dbOperator, datasetOfBar);


        Dataset datasetOfBook = MockDatasetFactory.createDataset(3L, "book", 1L, "dw", null, "Hive");
        MockDatasetFactory.insertDataset(dbOperator, datasetOfBook);

        DataSource dataSource = MockDataSourceFactory.createDataSource(1, "dataSource-1", null, 1L, null, null, null);
        MockDataSourceFactory.insertDataSource(dbOperator, dataSource, "AWS");
    }

}
