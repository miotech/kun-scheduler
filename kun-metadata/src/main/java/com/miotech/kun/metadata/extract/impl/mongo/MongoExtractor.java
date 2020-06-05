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
import com.mongodb.client.MongoIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class MongoExtractor implements Extractor {
    private static Logger logger = LoggerFactory.getLogger(MongoExtractor.class);

    private final MongoDataSource dataSource;
    private final MongoClient client;

    public MongoExtractor(MongoDataSource dataSource) {
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
        this.client = new MongoClient(new MongoClientURI(dataSource.getUrl()));
    }

    @Override
    public Iterator<Dataset> extract() {
        try {
            logger.debug("MongoExtractor extract start. dataSource: {}", JSONUtils.toJsonString(dataSource));
            List<String> databases = Lists.newArrayList();
            MongoIterable<String> databaseIterable = client.listDatabaseNames();
            for (String database : databaseIterable) {
                databases.add(database);
            }

            logger.debug("MongoExtractor extract end. databases: {}", JSONUtils.toJsonString(databases));
            return Iterators.concat(databases.stream().map((database) -> new MongoDatabaseExtractor(dataSource, database).extract()).iterator());
        } catch (Exception e) {
            logger.error("MongoExtractor extract error: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        } finally {
            client.close();
        }
    }

}
