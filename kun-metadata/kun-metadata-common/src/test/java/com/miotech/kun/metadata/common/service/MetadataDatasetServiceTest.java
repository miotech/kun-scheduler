package com.miotech.kun.metadata.common.service;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.common.factory.MockDataSourceFactory;
import com.miotech.kun.metadata.common.factory.MockDatasetFactory;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.*;
import org.apache.commons.collections4.CollectionUtils;
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
        Dataset datasetOfFoo = MockDatasetFactory.createDataset(1L);
        insertDataset(dbOperator, datasetOfFoo);

        // Process
        Long queryGid = 1L;
        Optional<Dataset> datasetOpt = metadataDatasetService.fetchDatasetByGid(queryGid);

        // Validate
        assertTrue(datasetOpt.isPresent());
        Dataset dataset = datasetOpt.get();
        assertThat(dataset.getGid(), is(queryGid));
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

        DataSource dataSource = MockDataSourceFactory.createDataSource(1, "dataSource-1", null, 1L, null, null, null);
        insertDataSource(dbOperator, dataSource, "AWS");

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

        DataSource dataSource = MockDataSourceFactory.createDataSource(1, "dataSource-1", null, 1L, null, null, null);
        insertDataSource(dbOperator, dataSource, "AWS");

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

        DataSource dataSource = MockDataSourceFactory.createDataSource(1, "dataSource-1", null, 1L, null, null, null);
        insertDataSource(dbOperator, dataSource, "AWS");

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

    private void insertDataset(DatabaseOperator dbOperator, Dataset dataset) {
        dbOperator.update("INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name, dsi, deleted) VALUES(?, ?, ?, CAST(? AS JSONB), ?, ?, ?)",
                dataset.getGid(), dataset.getName(), dataset.getDatasourceId(), DataStoreJsonUtil.toJson(dataset.getDataStore()),
                dataset.getDatabaseName(),
                dataset.getDataStore().getDSI().toFullString(),
                dataset.isDeleted()
        );

        if (CollectionUtils.isNotEmpty(dataset.getFields())) {
            for (DatasetField field : dataset.getFields()) {
                dbOperator.update("INSERT INTO kun_mt_dataset_field(dataset_gid, name, type) VALUES(?, ?, ?)", dataset.getGid(), field.getName(), field.getFieldType().getType().toValue());
            }
        }
    }

    private void insertDataSource(DatabaseOperator dbOperator, DataSource dataSource, String dataSourceTypeName) {
        dbOperator.update("INSERT INTO kun_mt_datasource(id, type_id) VALUES(?, ?)",
                dataSource.getId(), dataSource.getTypeId());
        dbOperator.update("INSERT INTO kun_mt_datasource_type(id, name) VALUES(?, ?)",
                dataSource.getTypeId(), dataSourceTypeName);
    }

}
