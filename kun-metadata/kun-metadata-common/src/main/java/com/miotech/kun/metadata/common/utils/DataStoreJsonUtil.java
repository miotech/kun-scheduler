package com.miotech.kun.metadata.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.model.lineage.*;

public class DataStoreJsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerSubtypes(new NamedType(HiveTableStore.class, "HiveTable"));
        MAPPER.registerSubtypes(new NamedType(MongoDataStore.class, "MongoCollection"));
        MAPPER.registerSubtypes(new NamedType(PostgresDataStore.class, "PostgresTale"));
        MAPPER.registerSubtypes(new NamedType(ElasticSearchIndexStore.class, "ElasticSearchIndex"));
        MAPPER.registerSubtypes(new NamedType(ArangoCollectionStore.class, "ArangoCollection"));
    }

    private DataStoreJsonUtil() {
    }

    public static String toJson(DataStore dataStore) {
        try {
            return MAPPER.writeValueAsString(dataStore);
        } catch (JsonProcessingException jsonProcessingException) {
            throw ExceptionUtils.wrapIfChecked(jsonProcessingException);
        }
    }

    public static DataStore toDataStore(String json) {
        try {
            return MAPPER.readValue(json, DataStore.class);
        } catch (JsonProcessingException jsonProcessingException) {
            throw ExceptionUtils.wrapIfChecked(jsonProcessingException);
        }
    }

}
