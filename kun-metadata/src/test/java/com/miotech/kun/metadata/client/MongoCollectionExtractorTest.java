package com.miotech.kun.metadata.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.metadata.extract.impl.mongo.MongoCollectionExtractor;
import com.miotech.kun.metadata.extract.impl.mongo.MongoExtractor;
import com.miotech.kun.metadata.load.impl.PrintLoader;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldType;
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
        assertThat(fields, containsInAnyOrder(new DatasetField("_id.timestamp", new DatasetFieldType(DatasetFieldType.convertRawType("NUMBER"), "NUMBER"), ""),
                new DatasetField("_id.machineIdentifier", new DatasetFieldType(DatasetFieldType.convertRawType("NUMBER"), "NUMBER"), ""),
                new DatasetField("_id.processIdentifier", new DatasetFieldType(DatasetFieldType.convertRawType("NUMBER"), "NUMBER"), ""),
                new DatasetField("_id.counter", new DatasetFieldType(DatasetFieldType.convertRawType("NUMBER"), "NUMBER"), ""),
                new DatasetField("dataStore.url", new DatasetFieldType(DatasetFieldType.convertRawType("STRING"), "STRING"), ""),
                new DatasetField("dataStore.database", new DatasetFieldType(DatasetFieldType.convertRawType("STRING"), "STRING"), ""),
                new DatasetField("dataStore.collection", new DatasetFieldType(DatasetFieldType.convertRawType("STRING"), "STRING"), ""),
                new DatasetField("dataStore.type", new DatasetFieldType(DatasetFieldType.convertRawType("STRING"), "STRING"), ""),
                new DatasetField("fields", new DatasetFieldType(DatasetFieldType.convertRawType("ARRAY"), "ARRAY"), ""),
                new DatasetField("fieldStats", new DatasetFieldType(DatasetFieldType.convertRawType("ARRAY"), "ARRAY"), ""),
                new DatasetField("datasetStat.rowCount", new DatasetFieldType(DatasetFieldType.convertRawType("NUMBER"), "NUMBER"), ""),
                new DatasetField("datasetStat.statDate", new DatasetFieldType(DatasetFieldType.convertRawType("STRING"), "STRING"), "")));
    }

}
