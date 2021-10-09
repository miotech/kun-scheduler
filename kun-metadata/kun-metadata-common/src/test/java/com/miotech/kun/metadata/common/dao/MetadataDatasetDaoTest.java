package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.workflow.core.model.lineage.ArangoCollectionStore;
import org.junit.Test;

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
                "INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name, dsi, deleted) VALUES (?, ?, ?, CAST(? AS JSONB), ?, ?, ?)",
                1L,
                "example_dataset",
                3L,
                "{\"type\": \"ARANGO_COLLECTION\", \"@class\": \"com.miotech.kun.workflow.core.model.lineage.ArangoCollectionStore\", \"database\": \"miotech_test_database\", \"collection\": \"demo_collection\", \"host\": \"127.0.0.1\", \"port\": 7890}",
                "miotech_test_database",
                "arango:collection=demo_collection,database=miotech_test_database,host=127.0.0.1,port=7890",
                true
        );

        databaseOperator.update("INSERT INTO kun_mt_dataset_field(dataset_gid, name, type, description, raw_type) VALUES (?, ?, ?, ?, ?)",
                1L,
                "id",
                "NUMBER",
                "",
                "decimal(18,0)");

        DataStore dataStore = new ArangoCollectionStore(
                "127.0.0.1",
                7890,
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
        assertThat(dataset.isDeleted(), is(true));
        assertThat(dataset.getFields().size(), is(1));
    }

    @Test
    public void fetchDatasetByGid_withNonExistGid_shouldReturnEmptyOptionalObject() {
        // Execute
        Optional<Dataset> datasetOptional = metadataDatasetDao.fetchDatasetByGid(1L);

        // Validate
        assertFalse(datasetOptional.isPresent());
    }

    @Test
    public void testFindByName_withNonExistName() {
        // Execute
        Optional<Dataset> datasetOpt = metadataDatasetDao.findByName("NonExistName");

        // Validate
        assertFalse(datasetOpt.isPresent());
    }

    @Test
    public void testFindByName_withExistName() {
        String name = "example_dataset";
        String fieldName = "id";
        // 1. Prepare
        // Force insert a dataset record into database
        databaseOperator.update(
                "INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name, dsi, deleted) VALUES (?, ?, ?, CAST(? AS JSONB), ?, ?, ?)",
                1L,
                name,
                3L,
                "{\"type\": \"ARANGO_COLLECTION\", \"@class\": \"com.miotech.kun.workflow.core.model.lineage.ArangoCollectionStore\", \"database\": \"miotech_test_database\", \"collection\": \"demo_collection\", \"host\": \"127.0.0.1\", \"port\": 7890}",
                "miotech_test_database",
                "arango:collection=demo_collection,database=miotech_test_database,host=127.0.0.1,port=7890",
                true
        );

        databaseOperator.update("INSERT INTO kun_mt_dataset_field(dataset_gid, name, type, description, raw_type) VALUES (?, ?, ?, ?, ?)",
                1L,
                fieldName,
                "NUMBER",
                "",
                "decimal(18,0)");

        // Execute
        Optional<Dataset> datasetOpt = metadataDatasetDao.findByName(name);

        // Validate
        assertTrue(datasetOpt.isPresent());
        Dataset dataset = datasetOpt.get();
        assertThat(dataset.getName(), is(name));
        assertThat(dataset.getFields().size(), is(1));
        DatasetField datasetField = dataset.getFields().get(0);
        assertThat(datasetField.getName(), is(fieldName));
    }

}