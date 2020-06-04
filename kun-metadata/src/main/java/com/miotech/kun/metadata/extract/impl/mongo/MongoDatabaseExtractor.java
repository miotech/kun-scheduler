package com.miotech.kun.metadata.extract.impl.mongo;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.MongoDataSource;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class MongoDatabaseExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(MongoDatabaseExtractor.class);

    private final MongoDataSource dataSource;

    private final String database;

    private final MongoClient client;

    public MongoDatabaseExtractor(MongoDataSource dataSource, String database) {
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
        this.database = database;
        this.client = new MongoClient(new MongoClientURI(dataSource.getUrl()));
    }

    @Override
    public Iterator<Dataset> extract() {
        try {
            logger.debug("MongoDatabaseExtractor extract start. dataSource: {}, database: {}",
                    JSONUtils.toJsonString(dataSource), database);
            List<String> collections = Lists.newArrayList();
            MongoDatabase usedDatabase = client.getDatabase(database);
            MongoIterable<String> collectionIterable = usedDatabase.listCollectionNames();
            for (String collection : collectionIterable) {
                collections.add(collection);
            }

            logger.debug("MongoDatabaseExtractor extract end. collections: {}", collections);
            return Iterators.concat(collections.stream().map((collection) ->
                    new MongoCollectionExtractor(dataSource, database, collection).extract()).iterator());
        } catch (Exception e) {
            logger.error("MongoDatabaseExtractor extract error: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            client.close();
        }
    }
}
