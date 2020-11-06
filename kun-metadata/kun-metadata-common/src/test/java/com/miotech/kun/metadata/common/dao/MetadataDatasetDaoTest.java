package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetBaseInfo;
import com.miotech.kun.workflow.core.model.lineage.ArangoCollectionStore;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class MetadataDatasetDaoTest extends DatabaseTestBase {
    @Inject
    private MetadataDatasetDao metadataDatasetDao;

    @Inject
    private DatabaseOperator databaseOperator;

    @Test
    public void fetchDatasetByGid_withExistGid_shouldReturnResultProperly() {
        // 1. Prepare
        // Force insert a dataset record into database
        databaseOperator.update(
                "INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name, dsi) VALUES (?, ?, ?, CAST(? AS JSONB), ?, ?)",
                1L,
                "example_dataset",
                3L,
                "{\"type\": \"ARANGO_COLLECTION\", \"@class\": \"com.miotech.kun.workflow.core.model.lineage.ArangoCollectionStore\", \"database\": \"miotech_test_database\", \"collection\": \"demo_collection\", \"dataStoreUrl\": \"127.0.0.1:7890\"}",
                "miotech_test_database",
                "arango:collection=demo_collection,database=miotech_test_database,url=127.0.0.1%3A7890,"
        );

        DataStore dataStore = new ArangoCollectionStore(
                "127.0.0.1:7890",
                "miotech_test_database",
                "demo_collection"
        );

        // 2. Execute
        Optional<Dataset> datasetOptional = metadataDatasetDao.fetchDatasetByGid(1L);

        // 3. Validate
        assertTrue(datasetOptional.isPresent());
        Dataset dataset = datasetOptional.get();
        assertThat(dataset.getGid(), is(1L));
        assertThat(dataset.getName(), is("example_dataset"));
        assertThat(dataset.getDatabaseName(), is("miotech_test_database"));
        assertThat(dataset.getDataStore(), sameBeanAs(dataStore));
    }

    @Test
    public void fetchDatasetByGid_withNonExistGid_shouldReturnEmptyOptionalObject() {
        // Execute
        Optional<Dataset> datasetOptional = metadataDatasetDao.fetchDatasetByGid(1L);

        // Validate
        assertFalse(datasetOptional.isPresent());
    }

    @Test
    public void testFetchDatasetsByDatasourceAndName_success() {
        // 1. Prepare
        // Force insert a dataset record into database
        databaseOperator.update(
                "INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name) VALUES(?, ?, ?, CAST(? AS JSONB), ?)",
                1L,
                "example_dataset",
                3L,
                "{\"type\": \"ARANGO_COLLECTION\", \"@class\": \"com.miotech.kun.workflow.core.model.lineage.ArangoCollectionStore\", \"database\": \"miotech_test_database\", \"collection\": \"demo_collection\", \"dataStoreUrl\": \"127.0.0.1:7890\"}",
                "miotech_test_database"
        );

        DataStore dataStore = new ArangoCollectionStore(
                "127.0.0.1:7890",
                "miotech_test_database",
                "demo_collection"
        );

        // 2. Query existing data
        List<DatasetBaseInfo> datasetBaseInfos = metadataDatasetDao.fetchDatasetsByDatasourceAndNameLike(3L, "example");

        // 3. Validate
        assertThat(datasetBaseInfos.size(), is(1));
        DatasetBaseInfo datasetBaseInfo = datasetBaseInfos.get(0);
        assertThat(datasetBaseInfo.getName(), is("example_dataset"));

        // 4. Query non-existent data
        datasetBaseInfos = metadataDatasetDao.fetchDatasetsByDatasourceAndNameLike(3L, "non-existent");

        // 5. Validate
        assertThat(datasetBaseInfos.size(), is(0));
    }

}