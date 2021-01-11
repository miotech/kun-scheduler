package com.miotech.kun.metadata.databuilder.extract.impl.mongo;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.metadata.databuilder.model.MongoDataSource;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import java.util.Iterator;
import java.util.List;

public class MongoDatabaseSchemaExtractor implements Extractor {
    private final MongoDataSource mongoDataSource;

    private final String dbName;

    public MongoDatabaseSchemaExtractor(MongoDataSource mongoDataSource, String dbName) {
        Preconditions.checkNotNull(mongoDataSource, "dataSource should not be null.");
        this.mongoDataSource = mongoDataSource;
        this.dbName = dbName;
    }

    @Override
    public Iterator<Dataset> extract() {
        try (MongoClient client = new MongoClient(new MongoClientURI(mongoDataSource.getUrl()))) {

            List<String> collections = Lists.newArrayList();
            MongoDatabase usedDatabase = client.getDatabase(dbName);
            MongoIterable<String> collectionIterable = usedDatabase.listCollectionNames();
            for (String collection : collectionIterable) {
                collections.add(collection);
            }

            return Iterators.concat(collections.stream().map(collection ->
                    new MongoCollectionSchemaExtractor(mongoDataSource, dbName, collection).extract()).iterator());
        }
    }
}
