package com.miotech.kun.metadata.databuilder.extract.impl.mongo;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetStat;
import com.miotech.kun.metadata.databuilder.extract.stat.DatasetStatExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.MongoDataSource;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.time.LocalDateTime;

public class MongoStatExtractor extends MongoExistenceExtractor implements DatasetStatExtractor {

    @Override
    public Dataset extract(Dataset dataset, DataSource dataSource) {
        Dataset.Builder resultBuilder = Dataset.newBuilder().withGid(dataset.getGid());
        MongoDataSource mongoDataSource = (MongoDataSource) dataSource;

        try (MongoClient client = new MongoClient(new MongoClientURI(mongoDataSource.getUrl()))) {
            DatasetStat.Builder datasetStatBuilder = DatasetStat.newBuilder();

            long count = client.getDatabase(dataset.getDatabaseName()).getCollection(dataset.getName()).count();
            resultBuilder.withDatasetStat(datasetStatBuilder.withRowCount(count)
                    .withLastUpdatedTime(getLastUpdateTime())
                    .withStatDate(LocalDateTime.now()).build());

            return resultBuilder.build();
        }
    }

}
