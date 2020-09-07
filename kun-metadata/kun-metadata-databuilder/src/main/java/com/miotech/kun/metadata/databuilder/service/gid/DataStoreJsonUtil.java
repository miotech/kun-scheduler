package com.miotech.kun.metadata.databuilder.service.gid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
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

    public static String toJson(DataStore dataStore) throws JsonProcessingException {
        return MAPPER.writeValueAsString(dataStore);
    }

    public static DataStore toDataStore(String json) throws JsonProcessingException {
        return MAPPER.readValue(json, DataStore.class);
    }

}
