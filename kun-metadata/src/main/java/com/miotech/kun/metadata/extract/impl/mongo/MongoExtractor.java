package com.miotech.kun.metadata.extract.impl.mongo;

import com.google.common.collect.Iterators;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.MongoCluster;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class MongoExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(MongoExtractor.class);

    private final MongoCluster cluster;

    public MongoExtractor(MongoCluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public Iterator<Dataset> extract() {
        MongoClient client = null;
        try {
            client = new MongoClient(new MongoClientURI(cluster.getUrl()));
            MongoIterable<String> databases = client.listDatabaseNames();
            return Iterators.concat(databases.map((database) -> new MongoDatabaseExtractor(cluster, database).extract()).iterator());
        } catch (Exception e) {
            logger.error("mongo operate error: ", e);
            throw new RuntimeException(e);
        }
    }
}
