package com.miotech.kun.metadata.load.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.miotech.kun.workflow.core.model.entity.*;

public class DataStoreJsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerSubtypes(new NamedType(HiveTableStore.class, "HiveTable"));
        MAPPER.registerSubtypes(new NamedType(MongoDataStore.class, "MongoCollection"));
        MAPPER.registerSubtypes(new NamedType(PostgresDataStore.class, "PostgresTale"));
    }

    public static String toJson(DataStore dataStore) throws JsonProcessingException {

        return MAPPER.writeValueAsString(dataStore);
    }

    public static DataStore toDataStore(String json) throws JsonProcessingException {
        return MAPPER.readValue(json, DataStore.class);
    }

    public static void main(String[] args) throws JsonProcessingException {
        DataStore dataStore = new HiveTableStore("db", "tb", new HiveCluster(123L, "abc", "123", "sdf", "dsf", "sdf", "wer"));
        System.out.println(MAPPER.writeValueAsString(dataStore));
    }

}
