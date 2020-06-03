package com.miotech.kun.metadata.extract.impl.mongo;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.miotech.kun.commons.utils.ExceptionUtils;
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
import com.miotech.kun.workflow.utils.JSONUtils;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class MongoCollectionExtractor extends ExtractorTemplate {
    private static Logger logger = LoggerFactory.getLogger(MongoCollectionExtractor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final MongoCluster mongoCluster;

    private final String database;

    private final String collection;

    private final MongoClient client;

    public MongoCollectionExtractor(MongoCluster mongoCluster, String database, String collection) {
        super(mongoCluster);
        Preconditions.checkNotNull(mongoCluster, "mongoCluster should not be null.");
        this.mongoCluster = mongoCluster;
        this.database = database;
        this.collection = collection;
        this.client = new MongoClient(new MongoClientURI(mongoCluster.getUrl()));
    }

    @Override
    @VisibleForTesting
    public List<DatasetField> getSchema() {
        logger.debug("MongoCollectionExtractor getSchema start. cluster: {}, database: {}, collection: {}",
                JSONUtils.toJsonString(cluster), database, collection);
        List<DatasetField> fields = Lists.newArrayList();

        MongoCollection<Document> documents = client.getDatabase(database).getCollection(collection);
        Document document = documents.find().first();
        if (document != null) {
            JsonNode jsonNode = objectMapper.convertValue(document, JsonNode.class);
            fields = FieldFlatUtil.flatFields(jsonNode, null);
        }

        logger.debug("MongoCollectionExtractor getSchema end. fields: {}", JSONUtils.toJsonString(fields));
        return fields;
    }

    @Override
    public DatasetFieldStat getFieldStats(DatasetField datasetField) {
        return null;
    }

    @Override
    @VisibleForTesting
    public DatasetStat getTableStats() {
        try {
            logger.debug("MongoCollectionExtractor getTableStats start. cluster: {}, database: {}, collection: {}",
                    JSONUtils.toJsonString(cluster), database, collection);
            long count = client.getDatabase(database).getCollection(collection).count();

            DatasetStat datasetStat = new DatasetStat(count, LocalDate.now());
            logger.debug("MongoCollectionExtractor getTableStats end. datasetStat: {}", JSONUtils.toJsonString(datasetStat));
            return datasetStat;
        } catch (Exception e) {
            logger.error("MongoCollectionExtractor getTableStats error: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
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
