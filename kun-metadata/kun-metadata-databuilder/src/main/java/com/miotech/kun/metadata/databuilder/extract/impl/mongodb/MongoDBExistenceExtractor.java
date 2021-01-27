package com.miotech.kun.metadata.databuilder.extract.impl.mongodb;

import com.beust.jcommander.internal.Lists;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetExistenceExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.MongoDataSource;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MongoDBExistenceExtractor implements DatasetExistenceExtractor {

    @Override
    public boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode) {
        try (MongoClient client = new MongoClient(new MongoClientURI(((MongoDataSource) dataSource).getUrl()))) {
            return client.getDatabase(dataset.getDatabaseName()).listCollectionNames().into(Lists.newArrayList()).contains(dataset.getName());
        }
    }

}
