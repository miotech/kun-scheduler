package com.miotech.kun.metadata.extract.impl.mongo;

import com.beust.jcommander.internal.Lists;
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
import java.util.List;

public class MongoExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(MongoExtractor.class);

    private final MongoCluster cluster;
    private MongoClient client;

    public MongoExtractor(MongoCluster cluster) {
        this.cluster = cluster;
        if (cluster != null) {
            this.client = new MongoClient(new MongoClientURI(cluster.getUrl()));
        }
    }

    @Override
    public Iterator<Dataset> extract() {
        try {
            List<String> databases = Lists.newArrayList();
            MongoIterable<String> databaseIterable = client.listDatabaseNames();
            for (String database : databaseIterable) {
                databases.add(database);
            }

            return Iterators.concat(databases.stream().map((database) -> new MongoDatabaseExtractor(cluster, database).extract()).iterator());
        } catch (Exception e) {
            logger.error("mongo operate error: ", e);
            throw new RuntimeException(e);
        } finally {
            client.close();
        }
    }

}
