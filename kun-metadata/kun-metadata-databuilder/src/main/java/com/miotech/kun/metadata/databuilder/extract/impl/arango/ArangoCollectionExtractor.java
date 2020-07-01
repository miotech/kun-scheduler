package com.miotech.kun.metadata.databuilder.extract.impl.arango;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.miotech.kun.metadata.databuilder.extract.template.ExtractorTemplate;
import com.miotech.kun.metadata.databuilder.model.*;
import com.miotech.kun.workflow.core.model.lineage.ArangoCollectionStore;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.utils.JSONUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArangoCollectionExtractor extends ExtractorTemplate {

    private ArangoDataSource cluster;
    private String dbName;
    private String collection;
    private MioArangoClient client;

    public ArangoCollectionExtractor(ArangoDataSource cluster, String dbNAme, String collection) {
        super(cluster.getId());
        this.cluster = cluster;
        this.dbName = dbNAme;
        this.collection = collection;
        this.client = new MioArangoClient(cluster);
    }

    @Override
    public List<DatasetField> getSchema() {
        String query = String.format("FOR c IN %s LIMIT 10 RETURN c", collection);
        String doc = client.getDoc(dbName, query);
        if (doc == null)
            return new ArrayList<DatasetField>();
        JsonNode json = null;
        try {
            json = JSONUtils.stringToJson(doc);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return docToFields(json, null);
    }

    @Override
    public DatasetFieldStat getFieldStats(DatasetField datasetField) {
        String query = String.format("FOR c IN %s FILTER c.%s != NULL COLLECT WITH COUNT INTO length RETURN length", collection, datasetField.getName());
        Integer count = client.count(dbName, query);

        DatasetFieldStat stat = DatasetFieldStat.newBuilder()
                .withName(datasetField.getName())
                .withNonnullCount(count)
                .withStatDate(LocalDateTime.now())
                .build();

        return stat;
    }

    @Override
    public DatasetStat getTableStats() {
        String query = String.format("RETURN LENGTH(%s)", collection);
        Integer count = client.count(dbName, query);
        DatasetStat stat = DatasetStat.newBuilder()
                .withRowCount(count)
                .withStatDate(LocalDateTime.now())
                .build();

        return stat;
    }

    @Override
    public DataStore getDataStore() {
        DataStore dataStore = new ArangoCollectionStore(cluster.getUrl(), dbName, collection);
        return dataStore;
    }

    @Override
    protected String getName() {
        return dbName + "." + collection;
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
