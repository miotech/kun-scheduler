package com.miotech.kun.metadata.databuilder.extract.impl.arangodb;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.JsonNode;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.metadata.core.model.dataset.DatasetField;
import com.miotech.kun.metadata.databuilder.client.ArangoClient;
import com.miotech.kun.metadata.databuilder.extract.schema.SchemaExtractorTemplate;
import com.miotech.kun.metadata.databuilder.extract.tool.FieldTypeParser;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import com.miotech.kun.metadata.databuilder.utils.JSONUtils;
import com.miotech.kun.workflow.core.model.lineage.ArangoCollectionStore;

import java.util.List;

public class ArangoDBCollectionSchemaExtractor extends SchemaExtractorTemplate {

    private final ArangoDataSource arangoDataSource;
    private final String dbName;
    private final String collectionName;
    private final ArangoClient client;

    public ArangoDBCollectionSchemaExtractor(ArangoDataSource arangoDataSource, String dbName, String collectionName) {
        super(arangoDataSource.getId());
        this.arangoDataSource = arangoDataSource;
        this.dbName = dbName;
        this.collectionName = collectionName;
        this.client = new ArangoClient(arangoDataSource);
    }

    @Override
    public List<DatasetField> getSchema() {
        String query = String.format("FOR c IN %s LIMIT 10 RETURN c", collectionName);
        String doc = client.getDoc(dbName, query);
        if (doc == null)
            return Lists.newArrayList();
        JsonNode json= JSONUtils.stringToJson(doc);
        return FieldTypeParser.parse(json, null);
    }

    @Override
    public DataStore getDataStore() {
        return new ArangoCollectionStore(arangoDataSource.getHost(), arangoDataSource.getPort(), dbName, collectionName);
    }

    @Override
    public String getName() {
        return collectionName;
    }

    @Override
    protected void close() {
        client.close();
    }

}