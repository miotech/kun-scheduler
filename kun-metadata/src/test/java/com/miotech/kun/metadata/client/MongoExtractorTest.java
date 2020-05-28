package com.miotech.kun.metadata.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.metadata.extract.impl.mongo.MongoCollectionExtractor;
import com.miotech.kun.metadata.model.DatasetField;
import com.mongodb.MongoClient;
import org.bson.Document;
import org.joor.Reflect;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MongoExtractorTest {

    @Test
    public void testExtract() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\n" +
                "\t\"_id\": {\n" +
                "\t\t\"timestamp\": 1590558820,\n" +
                "\t\t\"machineIdentifier\": 15723379,\n" +
                "\t\t\"processIdentifier\": -29499,\n" +
                "\t\t\"counter\": 3855686\n" +
                "\t},\n" +
                "\t\"dataStore\": {\n" +
                "\t\t\"url\": \"mongodb://127.0.0.1:27017\",\n" +
                "\t\t\"database\": \"local\",\n" +
                "\t\t\"collection\": \"startup_log\",\n" +
                "\t\t\"type\": \"COLLECTION\"\n" +
                "\t},\n" +
                "\t\"fields\": [],\n" +
                "\t\"fieldStats\": [],\n" +
                "\t\"datasetStat\": {\n" +
                "\t\t\"rowCount\": 2.0,\n" +
                "\t\t\"statDate\": \"May 27, 2020 11:41:30 AM\"\n" +
                "\t}\n" +
                "}";
        Document document = objectMapper.readValue(json, Document.class);

        MongoClient mockClient = mock(MongoClient.class, Mockito.RETURNS_DEEP_STUBS);
        when(mockClient.getDatabase("unden").getCollection("account").find().first()).thenReturn(document);

        MongoCollectionExtractor extractor = new MongoCollectionExtractor(null, "unden", "account");
        Reflect.on(extractor).set("client", mockClient);

        List<DatasetField> fields = extractor.getSchema();
        List<String> flatFields = fields.stream().map(field -> field.getName()).collect(Collectors.toList());
        assertThat(flatFields, containsInAnyOrder("_id.timestamp", "_id.machineIdentifier", "_id.processIdentifier",
                "_id.counter", "dataStore.url", "dataStore.database", "dataStore.collection", "dataStore.type",
                "fields", "fieldStats", "datasetStat.rowCount", "datasetStat.statDate"));
    }

}
