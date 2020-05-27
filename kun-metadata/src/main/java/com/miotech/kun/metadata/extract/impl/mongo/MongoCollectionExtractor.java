package com.miotech.kun.metadata.extract.impl.mongo;

import com.beust.jcommander.internal.Lists;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.template.ExtractorTemplate;
import com.miotech.kun.metadata.extract.tool.DatasetNameGenerator;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldStat;
import com.miotech.kun.metadata.model.DatasetStat;
import com.miotech.kun.metadata.model.MongoCluster;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.MongoDataStore;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MongoCollectionExtractor extends ExtractorTemplate {
    private static Logger logger = LoggerFactory.getLogger(MongoCollectionExtractor.class);

    private final MongoCluster cluster;

    private final String database;

    private final String collection;

    public MongoCollectionExtractor(MongoCluster cluster, String database, String collection) {
        this.cluster = cluster;
        this.database = database;
        this.collection = collection;
    }

    @Override
    protected List<DatasetField> getSchema() {
        List<DatasetField> fields = Lists.newArrayList();
        MongoClient client = null;
        try {
            client = new MongoClient(new MongoClientURI(cluster.getUrl()));
            MongoCollection<Document> documents = client.getDatabase(database).getCollection(this.collection);
            Document document = documents.find().first();
            if (document != null) {
                for (Map.Entry<String, Object> stringObjectEntry : document.entrySet()) {
                    String key = stringObjectEntry.getKey();
                    Object value = stringObjectEntry.getValue();
                    logger.info("fields: {}", key);
                    fields.add(new DatasetField(key, "", ""));
                }
            }
        } catch (Exception e) {
            logger.error("mongo operate error: ", e);
            throw new RuntimeException(e);
        }
        return fields;
    }

    @Override
    protected DatasetFieldStat getFieldStats(DatasetField datasetField) {
        return null;
    }

    @Override
    protected DatasetStat getTableStats() {
        MongoClient client = null;
        try {
            client = new MongoClient(new MongoClientURI(cluster.getUrl()));
            long count = client.getDatabase(database).getCollection(collection).count();
            return new DatasetStat(count, new Date());
        } catch (Exception e) {
            logger.error("mongo operate error: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected DataStore getDataStore() {
        return new MongoDataStore(cluster.getUrl(), database, collection);
    }

    @Override
    protected String getName() {
        return DatasetNameGenerator.generateDatasetName(DatabaseType.MONGO, collection);
    }
}
