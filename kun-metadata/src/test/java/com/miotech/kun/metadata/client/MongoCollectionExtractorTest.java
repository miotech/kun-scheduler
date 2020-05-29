package com.miotech.kun.metadata.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.metadata.extract.impl.mongo.MongoCollectionExtractor;
import com.miotech.kun.metadata.extract.impl.mongo.MongoExtractor;
import com.miotech.kun.metadata.load.impl.PrintLoader;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.MongoCluster;
import com.mongodb.MongoClient;
import org.bson.Document;
import org.joor.Reflect;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MongoCollectionExtractorTest {

    @Test
    public void testGetSchema_ok() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\"_id\":{\"timestamp\":1590558820,\"machineIdentifier\":15723379,\"processIdentifier\":-29499," +
                "\"counter\":3855686},\"dataStore\":{\"url\":\"mongodb://127.0.0.1:27017\",\"database\":\"local\"," +
                "\"collection\":\"startup_log\",\"type\":\"COLLECTION\"},\"fields\":[],\"fieldStats\":[]," +
                "\"datasetStat\":{\"rowCount\":2.0,\"statDate\":\"May 27, 2020 11:41:30 AM\"}}";

        Document document = objectMapper.readValue(json, Document.class);

        MongoClient mockClient = mock(MongoClient.class, Mockito.RETURNS_DEEP_STUBS);
        when(mockClient.getDatabase("unden").getCollection("account").find().first()).thenReturn(document);

        MongoCluster mongoCluster = new MongoCluster(1L, "mongodb://127.0.0.1:27017", "", "");

        MongoCollectionExtractor extractor = new MongoCollectionExtractor(mongoCluster, "unden", "account");
        Reflect.on(extractor).set("client", mockClient);

        List<DatasetField> fields = extractor.getSchema();
        assertThat(fields, containsInAnyOrder(new DatasetField("_id.timestamp", "NUMBER", ""),
                new DatasetField("_id.machineIdentifier", "NUMBER", ""),
                new DatasetField("_id.processIdentifier", "NUMBER", ""),
                new DatasetField("_id.counter", "NUMBER", ""),
                new DatasetField("dataStore.url", "STRING", ""),
                new DatasetField("dataStore.database", "STRING", ""),
                new DatasetField("dataStore.collection", "STRING", ""),
                new DatasetField("dataStore.type", "STRING", ""),
                new DatasetField("fields", "ARRAY", ""),
                new DatasetField("fieldStats", "ARRAY", ""),
                new DatasetField("datasetStat.rowCount", "NUMBER", ""),
                new DatasetField("datasetStat.statDate", "STRING", "")));
    }

    @Test
    public void testOne() {
        MongoExtractor mongoExtractor = new MongoExtractor(new MongoCluster(1L, "mongodb://127.0.0.1:27017", "", ""));
        Iterator<Dataset> extract = mongoExtractor.extract();
        PrintLoader printLoader = new PrintLoader();
        while (extract.hasNext()) {
            Dataset next = extract.next();
            printLoader.load(next);
        }
    }

}
