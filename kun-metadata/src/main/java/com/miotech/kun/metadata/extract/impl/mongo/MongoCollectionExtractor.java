package com.miotech.kun.metadata.extract.impl.mongo;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.miotech.kun.metadata.client.ObjectMapperClient;
import com.miotech.kun.metadata.constant.DatabaseType;
import com.miotech.kun.metadata.extract.template.ExtractorTemplate;
import com.miotech.kun.metadata.extract.tool.DatasetNameGenerator;
import com.miotech.kun.metadata.extract.tool.FieldFlatUtil;
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

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MongoCollectionExtractor extends ExtractorTemplate {
    private static Logger logger = LoggerFactory.getLogger(MongoCollectionExtractor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final MongoCluster mongoCluster;

    private final String database;

    private final String collection;

    private MongoClient client;



    public MongoCollectionExtractor(MongoCluster mongoCluster, String database, String collection) {
        super(mongoCluster);
        this.mongoCluster = mongoCluster;
        this.database = database;
        this.collection = collection;
        if (mongoCluster != null) {
            this.client = new MongoClient(new MongoClientURI(mongoCluster.getUrl()));
        }
    }

    @Override
    public List<DatasetField> getSchema() {
        List<DatasetField> fields = Lists.newArrayList();
        try {
            MongoCollection<Document> documents = client.getDatabase(database).getCollection(collection);
            Document document = documents.find().first();
            if (document != null) {
                JsonNode jsonNode = objectMapper.convertValue(document, JsonNode.class);
                fields = FieldFlatUtil.flatFields(jsonNode, null);
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
        try {
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
