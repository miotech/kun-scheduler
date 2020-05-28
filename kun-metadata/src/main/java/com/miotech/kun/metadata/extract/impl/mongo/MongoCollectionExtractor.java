package com.miotech.kun.metadata.extract.impl.mongo;

import com.beust.jcommander.internal.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    private final MongoCluster mongoCluster;

    private final String database;

    private final String collection;

    public MongoCollectionExtractor(MongoCluster mongoCluster, String database, String collection) {
        super(mongoCluster);
        this.mongoCluster = mongoCluster;
        this.database = database;
        this.collection = collection;
    }

    @Override
    protected List<DatasetField> getSchema() {
        List<DatasetField> fields = Lists.newArrayList();
        MongoClient client = null;
        try {
            client = new MongoClient(new MongoClientURI(mongoCluster.getUrl()));
            MongoCollection<Document> documents = client.getDatabase(database).getCollection(collection);
            Document document = documents.find().first();
            if (document != null) {
                JsonObject jsonObject = new JsonParser().parse(new Gson().toJson(document)).getAsJsonObject();
                for (Map.Entry<String, Object> entry : document.entrySet()) {
                    String key = entry.getKey();
                    fields.add(new DatasetField(key, "", ""));
                }
            }
        } catch (Exception e) {
            logger.error("mongo operate error: ", e);
            throw new RuntimeException(e);
        } finally {
            client.close();
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
            client = new MongoClient(new MongoClientURI(mongoCluster.getUrl()));
            long count = client.getDatabase(database).getCollection(collection).count();
            return new DatasetStat(count, new Date());
        } catch (Exception e) {
            logger.error("mongo operate error: ", e);
            throw new RuntimeException(e);
        } finally {
            client.close();
        }
    }

    @Override
    protected DataStore getDataStore() {
        return new MongoDataStore(mongoCluster.getUrl(), database, collection);
    }

    @Override
    protected String getName() {
        return DatasetNameGenerator.generateDatasetName(DatabaseType.MONGO, collection);
    }

    @Override
    protected long getClusterId() {
        return cluster.getClusterId();
    }
}
