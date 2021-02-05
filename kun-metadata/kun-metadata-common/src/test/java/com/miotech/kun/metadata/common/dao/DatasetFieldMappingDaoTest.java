package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.core.model.DatasetFieldMapping;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class DatasetFieldMappingDaoTest extends DatabaseTestBase {
    @Inject
    private DatasetFieldMappingDao fieldMappingDao;

    @Inject
    private DatabaseOperator databaseOperator;

    @Test
    public void fetchDatasetFieldMappingByDatasourceId_withExistDatasourceId() {
        // 1. Prepare
        // Force insert a dataset record into database
        databaseOperator.update(
                "INSERT INTO kun_mt_dataset_field_mapping(datasource_id, pattern, type) VALUES (?, ?, ?)",
                1L,
                "^varchar.*$",
                "CHARACTER"
        );

        // 2. Execute
        List<DatasetFieldMapping> datasetFieldMappings = fieldMappingDao.fetchByDatasourceId(1L);

        // 3. Validate
        assertFalse(datasetFieldMappings.isEmpty());
        DatasetFieldMapping datasetFieldMapping = datasetFieldMappings.get(0);
        assertThat(datasetFieldMapping.getDatasourceId(), is(1L));
    }

    @Test
    public void fetchDatasetFieldMappingByDatasourceId_withNonExistDatasourceId() {
        // Execute
        List<DatasetFieldMapping> datasetFieldMappings = fieldMappingDao.fetchByDatasourceId(1L);

        // Validate
        assertTrue(datasetFieldMappings.isEmpty());
    }
}