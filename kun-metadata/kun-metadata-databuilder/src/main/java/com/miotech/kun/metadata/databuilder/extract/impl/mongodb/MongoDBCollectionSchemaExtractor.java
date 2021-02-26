package com.miotech.kun.metadata.databuilder.extract.impl.mongodb;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.databuilder.extract.schema.SchemaExtractorTemplate;
import com.miotech.kun.metadata.databuilder.extract.tool.FieldTypeParser;
import com.miotech.kun.metadata.databuilder.model.MongoDataSource;
import com.miotech.kun.workflow.core.model.lineage.MongoDataStore;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.List;

public class MongoDBCollectionSchemaExtractor extends SchemaExtractorTemplate {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final MongoDataSource mongoDataSource;

    private final String dbName;

    private final String collectionName;

    public MongoDBCollectionSchemaExtractor(MongoDataSource mongoDataSource, String dbName, String collectionName) {
        super(mongoDataSource.getId());
        Preconditions.checkNotNull(mongoDataSource, "dataSource should not be null.");
        this.mongoDataSource = mongoDataSource;
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

    @Override
    @VisibleForTesting
    public List<DatasetField> getSchema() {
        try (MongoClient client = new MongoClient(new MongoClientURI(mongoDataSource.getUrl()))) {
            List<DatasetField> fields = Lists.newArrayList();

            MongoCollection<Document> documents = client.getDatabase(dbName).getCollection(collectionName);
            Document document = documents.find().first();
            if (document != null) {
                JsonNode jsonNode = objectMapper.convertValue(document, JsonNode.class);
                fields = FieldTypeParser.parse(jsonNode, null);
            }

            return fields;
        }
    }

    @Override
    protected DataStore getDataStore() {
        return new MongoDataStore(mongoDataSource.getHost(), mongoDataSource.getPort(), dbName, collectionName);
    }

    @Override
    public String getName() {
        return collectionName;
    }

    @Override
    protected void close() {
        // Do nothing
    }

}