package com.miotech.kun.metadata.common.client;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.common.cataloger.CatalogerBlackList;
import com.miotech.kun.metadata.common.cataloger.CatalogerConfig;
import com.miotech.kun.metadata.common.cataloger.CatalogerFilter;
import com.miotech.kun.metadata.common.cataloger.CatalogerWhiteList;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.datasource.DataSource;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseMetadataBackend implements MetadataBackend {

    protected final CatalogerFilter filter;

    public BaseMetadataBackend(CatalogerConfig config) {
        this.filter = generateFilter(config);
    }

    @Override
    public Iterator<Dataset> extract(DataSource dataSource) {
        List<String> databaseList = extractDatabase(dataSource);
        return Iterators.concat(databaseList.stream().map(database -> extractDataset(dataSource.getId(), database)).iterator());

    }

    protected abstract List<String> searchDatabase(DataSource dataSource);

    protected abstract List<Dataset> searchDataset(Long datasourceId, String databaseName);

    private CatalogerFilter generateFilter(CatalogerConfig config){
        CatalogerBlackList blackList = config.getBlackList();
        CatalogerWhiteList whiteList = config.getWhiteList();
        return new CatalogerFilter(whiteList,blackList);
    }

    private List<String> extractDatabase(DataSource dataSource) {
        List<String> databases = searchDatabase(dataSource);
        return databases.stream().filter(database -> filter.filterDatabase(database))
                .collect(Collectors.toList());
    }

    private Iterator<Dataset> extractDataset(Long datasourceId, String databaseName) {
        List<Dataset> datasets = searchDataset(datasourceId, databaseName);
        List<Dataset> filteredDataset = datasets.stream().filter(dataset -> {
                    String datasetName = dataset.getName();
                    return filter.filterTable(databaseName,datasetName);
                })
                .collect(Collectors.toList());
        return filteredDataset.iterator();
    }
}
