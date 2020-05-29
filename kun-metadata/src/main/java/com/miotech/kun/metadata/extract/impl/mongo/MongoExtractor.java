package com.miotech.kun.metadata.extract.impl.mongo;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.MongoCluster;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class MongoExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(MongoExtractor.class);

    private final MongoCluster mongoCluster;
    private final MongoClient client;

    public MongoExtractor(MongoCluster mongoCluster) {
        Preconditions.checkNotNull(mongoCluster, "mongoCluster should not be null.");
        this.mongoCluster = mongoCluster;
        this.client = new MongoClient(new MongoClientURI(mongoCluster.getUrl()));
    }

    @Override
    public Iterator<Dataset> extract() {
        try {
            logger.debug("MongoExtractor extract start. cluster: {}", JSONUtils.toJsonString(mongoCluster));
            List<String> databases = Lists.newArrayList();
            MongoIterable<String> databaseIterable = client.listDatabaseNames();
            for (String database : databaseIterable) {
                databases.add(database);
            }

            logger.debug("MongoExtractor extract end. databases: {}", JSONUtils.toJsonString(databases));
            return Iterators.concat(databases.stream().map((database) -> new MongoDatabaseExtractor(mongoCluster, database).extract()).iterator());
        } catch (Exception e) {
            logger.error("MongoExtractor extract error: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            client.close();
        }
    }

}
