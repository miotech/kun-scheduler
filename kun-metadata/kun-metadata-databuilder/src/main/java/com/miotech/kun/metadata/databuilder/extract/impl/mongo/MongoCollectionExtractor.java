package com.miotech.kun.metadata.databuilder.extract.impl.mongo;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.databuilder.extract.template.ExtractorTemplate;
import com.miotech.kun.metadata.databuilder.extract.tool.FieldFlatUtil;
import com.miotech.kun.metadata.databuilder.model.DatasetField;
import com.miotech.kun.metadata.databuilder.model.DatasetFieldStat;
import com.miotech.kun.metadata.databuilder.model.DatasetStat;
import com.miotech.kun.metadata.databuilder.model.MongoDataSource;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.lineage.MongoDataStore;
import com.miotech.kun.workflow.utils.JSONUtils;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class MongoCollectionExtractor extends ExtractorTemplate {
    private static Logger logger = LoggerFactory.getLogger(MongoCollectionExtractor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final MongoDataSource dataSource;

    private final String database;

    private final String collection;

    public MongoCollectionExtractor(MongoDataSource dataSource, String database, String collection) {
        super(dataSource.getId());
        Preconditions.checkNotNull(dataSource, "dataSource should not be null.");
        this.dataSource = dataSource;
        this.database = database;
        this.collection = collection;
    }

    @Override
    @VisibleForTesting
    public List<DatasetField> getSchema() {
        try (MongoClient client = new MongoClient(new MongoClientURI(dataSource.getUrl()))) {
            if (logger.isDebugEnabled()) {
                logger.debug("MongoCollectionExtractor getSchema start. dataSource: {}, database: {}, collection: {}",
                        JSONUtils.toJsonString(dataSource), database, collection);
            }

            List<DatasetField> fields = Lists.newArrayList();

            MongoCollection<Document> documents = client.getDatabase(database).getCollection(collection);
            Document document = documents.find().first();
            if (document != null) {
                JsonNode jsonNode = objectMapper.convertValue(document, JsonNode.class);
                fields = FieldFlatUtil.flatFields(jsonNode, null);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("MongoCollectionExtractor getSchema end. fields: {}", JSONUtils.toJsonString(fields));
            }
            return fields;
        } catch (Exception e) {
            logger.error("MongoCollectionExtractor getSchema error: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    public DatasetFieldStat getFieldStats(DatasetField datasetField) {
        return null;
    }

    @Override
    @VisibleForTesting
    public DatasetStat getTableStats() {
        try (MongoClient client = new MongoClient(new MongoClientURI(dataSource.getUrl()))) {
            if (logger.isDebugEnabled()) {
                logger.debug("MongoCollectionExtractor getTableStats start. dataSource: {}, database: {}, collection: {}",
                        JSONUtils.toJsonString(dataSource), database, collection);
            }

            long count = client.getDatabase(database).getCollection(collection).count();

            DatasetStat datasetStat = new DatasetStat(count, LocalDateTime.now());
            if (logger.isDebugEnabled()) {
                logger.debug("MongoCollectionExtractor getTableStats end. datasetStat: {}", JSONUtils.toJsonString(datasetStat));
            }
            return datasetStat;
        } catch (Exception e) {
            logger.error("MongoCollectionExtractor getTableStats error: ", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    @Override
    protected DataStore getDataStore() {
        return new MongoDataStore(dataSource.getUrl(), database, collection);
    }

    @Override
    public String getName() {
        return collection;
    }

    @Override
    protected void close() {
        // Do nothing
    }

}
