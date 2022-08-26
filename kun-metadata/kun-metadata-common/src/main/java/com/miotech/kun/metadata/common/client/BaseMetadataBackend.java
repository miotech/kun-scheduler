package com.miotech.kun.metadata.common.client;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.datasource.DataSource;

import java.util.Iterator;
import java.util.List;

public abstract class BaseMetadataBackend implements MetadataBackend {


    public BaseMetadataBackend() {

    }

    @Override
    public Iterator<Dataset> extract(DataSource dataSource) {
        List<String> databaseList = extractDatabase(dataSource);
        return Iterators.concat(databaseList.stream().map(database -> extractDataset(dataSource.getId(), database)).iterator());

    }

    protected abstract List<String> searchDatabase(DataSource dataSource);

    protected abstract List<Dataset> searchDataset(Long datasourceId, String databaseName);


    private List<String> extractDatabase(DataSource dataSource) {
        return searchDatabase(dataSource);

    }

    private Iterator<Dataset> extractDataset(Long datasourceId, String databaseName) {
        List<Dataset> datasets = searchDataset(datasourceId, databaseName);
        return datasets.iterator();
    }
}
