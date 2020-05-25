package com.miotech.kun.metadata.extract.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.miotech.kun.metadata.extract.factory.ExtractorTemplate;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldStat;
import com.miotech.kun.metadata.model.DatasetStat;
import com.miotech.kun.workflow.core.model.entity.CommonCluster;
import com.miotech.kun.workflow.core.model.entity.DataStore;
import com.miotech.kun.workflow.core.model.entity.DataStoreType;
import com.miotech.kun.workflow.utils.JSONUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ArangoCollectionExtractor extends ExtractorTemplate {

    private CommonCluster cluster;
    private String dbName;
    private String collection;
    private MioArangoClient client;

    public ArangoCollectionExtractor(CommonCluster cluster, String dbNAme, String collection){
        this.cluster = cluster;
        this.dbName = dbNAme;
        this.collection = collection;
        this.client = new MioArangoClient(cluster);
    }

    @Override
    public List<DatasetField> getSchema(){
        String query = String.format("FOR c IN %s LIMIT 10 RETURN c", collection);
        String doc = client.getDoc(dbName, query);
        JsonNode json = null;
        try {
            json = JSONUtils.stringToJson(doc);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return docToFields(json, null);
    }

    @Override
    public DatasetFieldStat getFieldStats(DatasetField datasetField){
        DatasetFieldStat stat = new DatasetFieldStat();
        String query = String.format("FOR c IN %s FILTER c.%s != NULL COLLECT WITH COUNT INTO length RETURN length", collection, datasetField.getName());
        Integer count = client.count(dbName, query);
        stat.setNonnullCount(count);
        stat.setStatDate(new Date());
        return stat;
    }

    @Override
    public DatasetStat getTableStats(){
        DatasetStat stat = new DatasetStat();
        String query = String.format("RETURN LENGTH(%s)", collection);
        Integer count = client.count(dbName, query);
        stat.setRowCount(count);
        return stat;
    }

    @Override
    public DataStore getDataStore(){
        DataStore dataStore = new DataStore(DataStoreType.COLLECTION);
        dataStore.setCluster(this.cluster);
        return dataStore;
    }

    public List<DatasetField> docToFields(JsonNode root, String parent){
        List<DatasetField> fieldList = new ArrayList<>();
        Iterator<String> fieldNames = root.fieldNames();
        while(fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            String keyName = parent == null ? fieldName : String.format("%s.%s", parent, fieldName);
            JsonNode node = root.get(fieldName);
            if (node.isObject()) {
                fieldList.addAll(docToFields(node, keyName));
            } else if (node.isArray()) {
                for(JsonNode n : node){
                    fieldList.addAll(docToFields(n, keyName));
                }
            } else {
                String fieldType = null;
                if (node.isNull()){
                    fieldType = "UNKNOW";
                }
                else {
                    if (node.isNumber())
                        fieldType = "NUMBER";
                    else if (node.isTextual() || node.isBinary())
                        fieldType = "STRING";
                    else if (node.isBoolean())
                        fieldType = "BOOL";
                }
                DatasetField field = new DatasetField(keyName, fieldType, "");
                fieldList.add(field);
            }
        }
        return fieldList;
    }

}
