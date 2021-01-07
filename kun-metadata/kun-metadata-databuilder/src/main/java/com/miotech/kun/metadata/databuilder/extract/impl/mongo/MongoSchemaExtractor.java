package com.miotech.kun.metadata.databuilder.extract.impl.mongo;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetSchemaExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.MongoDataSource;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoIterable;

import java.util.Iterator;
import java.util.List;

public class MongoSchemaExtractor extends MongoExistenceExtractor implements DatasetSchemaExtractor {

    @Override
    public List<DatasetField> extract(Dataset dataset, DataSource dataSource) {
        MongoCollectionSchemaExtractor mongoCollectionSchemaExtractor =
                new MongoCollectionSchemaExtractor((MongoDataSource) dataSource, dataset.getDatabaseName(), dataset.getName());
        return mongoCollectionSchemaExtractor.getSchema();

    }

    @Override
    public Iterator<Dataset> extract(DataSource dataSource) {
        MongoDataSource mongoDataSource = (MongoDataSource) dataSource;
        try (MongoClient client = new MongoClient(new MongoClientURI((mongoDataSource).getUrl()))) {
            List<String> databases = Lists.newArrayList();
            MongoIterable<String> databaseIterable = client.listDatabaseNames();
            for (String database : databaseIterable) {
                databases.add(database);
            }

            return Iterators.concat(databases.stream().map(database -> new MongoDatabaseSchemaExtractor(mongoDataSource, database).extract()).iterator());
        }
    }
}
