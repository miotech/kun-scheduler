package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.metadata.core.model.DatasetFieldMapping;
import com.miotech.kun.metadata.testing.MetadataDataBaseTestBase;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class DatasetFieldMappingDaoTest extends MetadataDataBaseTestBase {
    @Inject
    private DatasetFieldMappingDao fieldMappingDao;

    @Inject
    private DatabaseOperator databaseOperator;

    @Test
    public void fetchDatasetFieldMappingByDatasourceId_withExistDatasourceId() {
        // 1. Prepare
        // Force insert a dataset record into database
        databaseOperator.update(
                "INSERT INTO kun_mt_dataset_field_mapping(datasource_type, pattern, type) VALUES (?, ?, ?)",
                "HIVE",
                "^varchar.*$",
                "CHARACTER"
        );

        // 2. Execute
        List<DatasetFieldMapping> datasetFieldMappings = fieldMappingDao.fetchByDatasourceType("HIVE");

        // 3. Validate
        assertFalse(datasetFieldMappings.isEmpty());
        DatasetFieldMapping datasetFieldMapping = datasetFieldMappings.get(0);
        assertThat(datasetFieldMapping.getDatasourceType(), is("HIVE"));
        assertThat(datasetFieldMapping.getPattern(), is("^varchar.*$"));
        assertThat(datasetFieldMapping.getType(), is("CHARACTER"));
    }

    @Test
    public void fetchDatasetFieldMappingByDatasourceId_withNonExistDatasourceId() {
        // Execute
        List<DatasetFieldMapping> datasetFieldMappings = fieldMappingDao.fetchByDatasourceType("HIVE");

        // Validate
        assertTrue(datasetFieldMappings.isEmpty());
    }
}