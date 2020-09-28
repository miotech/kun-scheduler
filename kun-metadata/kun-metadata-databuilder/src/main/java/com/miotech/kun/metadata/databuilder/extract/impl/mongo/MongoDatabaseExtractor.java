package com.miotech.kun.metadata.databuilder.extract.impl.mongo;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.extract.AbstractExtractor;
import com.miotech.kun.metadata.databuilder.model.Dataset;
import com.miotech.kun.metadata.databuilder.model.MongoDataSource;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class MongoDatabaseExtractor extends AbstractExtractor {
    private static Logger logger = LoggerFactory.getLogger(MongoDatabaseExtractor.class);

    private final MongoDataSource dataSource;

    private final String database;

    public MongoDatabaseExtractor(Props props, MongoDataSource dataSource, String database) {
        super(props);
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
        this.database = database;
    }

    @Override
    public Iterator<Dataset> extract() {
        try (MongoClient client = new MongoClient(new MongoClientURI(dataSource.getUrl()))) {
            if (logger.isDebugEnabled()) {
                logger.debug("MongoDatabaseExtractor extract start. dataSource: {}, database: {}",
                        JSONUtils.toJsonString(dataSource), database);
            }

            List<String> collections = Lists.newArrayList();
            MongoDatabase usedDatabase = client.getDatabase(database);
            MongoIterable<String> collectionIterable = usedDatabase.listCollectionNames();
            for (String collection : collectionIterable) {
                collections.add(collection);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("MongoDatabaseExtractor extract end. collections: {}", collections);
            }
            return Iterators.concat(collections.stream().map(collection ->
                    new MongoCollectionExtractor(getProps(), dataSource, database, collection).extract()).iterator());
        } catch (Exception e) {
            logger.error("MongoDatabaseExtractor extract error: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }
}
