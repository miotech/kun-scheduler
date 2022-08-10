package com.miotech.kun.metadata.common.mock;

import com.miotech.kun.metadata.common.client.BaseMetadataBackend;
import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.event.MetadataChangeEvent;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockMetadataBackend extends BaseMetadataBackend {

    private Map<Long, List<String>> databaseOfDatasource = new HashMap<>();

    private Map<String, List<Dataset>> datasetOfDatabase = new HashMap<>();

    public MockMetadataBackend() {
    }

    @Override
    protected List<String> searchDatabase(DataSource dataSource) {
        return databaseOfDatasource.get(dataSource.getId());
    }

    @Override
    protected List<Dataset> searchDataset(Long datasourceId, String databaseName) {
        List<String> databases = databaseOfDatasource.get(datasourceId);
        if (databases == null || !databases.contains(databaseName)) {
            return new ArrayList<>();
        }
        return datasetOfDatabase.get(databaseName);
    }

    public void setDatabasesForDatasource(Long datasourceId, List<String> databaseList) {
        databaseOfDatasource.put(datasourceId, databaseList);
    }

    public void setDataSetsForDatabase(String database, List<Dataset> datasetList) {
        datasetOfDatabase.put(database, datasetList);
    }

    @Override
    public OffsetDateTime getLastUpdatedTime(Dataset dataset) {
        return null;
    }

    @Override
    public boolean judgeExistence(Dataset dataset, DatasetExistenceJudgeMode judgeMode) {
        return false;
    }

    @Override
    public List<DatasetField> extract(Dataset dataset) {
        return null;
    }

    @Override
    public Dataset extract(MetadataChangeEvent mce) {
        return null;
    }

    @Override
    public String storageLocation(Dataset dataset) {
        return null;
    }

    @Override
    public void close() {

    }
}
