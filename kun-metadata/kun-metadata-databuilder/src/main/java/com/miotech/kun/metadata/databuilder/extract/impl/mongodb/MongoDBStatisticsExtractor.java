package com.miotech.kun.metadata.databuilder.extract.impl.mongodb;

import com.google.common.collect.Lists;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.FieldStatistics;
import com.miotech.kun.metadata.databuilder.extract.statistics.DatasetStatisticsExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.MongoDataSource;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.util.List;

public class MongoDBStatisticsExtractor extends MongoDBExistenceExtractor implements DatasetStatisticsExtractor {

    @Override
    public Long getRowCount(Dataset dataset, DataSource dataSource) {
        MongoDataSource mongoDataSource = (MongoDataSource) dataSource;
        try (MongoClient client = new MongoClient(new MongoClientURI(mongoDataSource.getUrl()))) {
            return client.getDatabase(dataset.getDatabaseName()).getCollection(dataset.getName()).count();
        }
    }

    @Override
    public List<FieldStatistics> extractFieldStatistics(Dataset dataset, DataSource dataSource) {
        return Lists.newArrayList();
    }
}
