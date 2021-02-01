package com.miotech.kun.metadata.databuilder.extract.impl.arangodb;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.JsonNode;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldType;
import com.miotech.kun.metadata.databuilder.client.ArangoClient;
import com.miotech.kun.metadata.databuilder.extract.schema.SchemaExtractorTemplate;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import com.miotech.kun.metadata.databuilder.utils.JSONUtils;
import com.miotech.kun.workflow.core.model.lineage.ArangoCollectionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArangoDBCollectionSchemaExtractor extends SchemaExtractorTemplate {
    private static final Logger logger = LoggerFactory.getLogger(ArangoDBCollectionSchemaExtractor.class);

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
        return docToFields(json, null);
    }

    @Override
    public DataStore getDataStore() {
        return new ArangoCollectionStore(arangoDataSource.getUrl(), dbName, collectionName);
    }

    @Override
    public String getName() {
        return collectionName;
    }

    @Override
    protected void close() {
        client.close();
    }

    public List<DatasetField> docToFields(JsonNode root, String parent) {
        List<DatasetField> fieldList = new ArrayList<>();
        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            String keyName = parent == null ? fieldName : String.format("%s.%s", parent, fieldName);
            JsonNode node = root.get(fieldName);
            if (node.isObject()) {
                fieldList.addAll(docToFields(node, keyName));
            } else if (node.isArray()) {
                DatasetField field = new DatasetField(keyName, new DatasetFieldType(DatasetFieldType.Type.ARRAY, "ARRAY"), "");
                fieldList.add(field);
            } else {
                DatasetFieldType.Type fieldType = null;
                String rawType = null;
                if (node.isNull()) {
                    fieldType = DatasetFieldType.Type.UNKNOW;
                    rawType = "UNKNOW";
                } else {
                    if (node.isNumber()) {
                        fieldType = DatasetFieldType.Type.NUMBER;
                        rawType = "NUMBER";
                    } else if (node.isTextual() || node.isBinary()) {
                        fieldType = DatasetFieldType.Type.CHARACTER;
                        rawType = "STRING";
                    } else if (node.isBoolean()) {
                        fieldType = DatasetFieldType.Type.BOOLEAN;
                        rawType = "BOOLEAN";
                    }
                }
                DatasetField field = new DatasetField(keyName, new DatasetFieldType(fieldType, rawType), "");
                fieldList.add(field);
            }
        }
        return fieldList;
    }

}