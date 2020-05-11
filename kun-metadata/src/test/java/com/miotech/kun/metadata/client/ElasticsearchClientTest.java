package com.miotech.kun.metadata.client;

import java.io.IOException;
import java.util.List;

public class ElasticsearchClientTest {

    @org.testng.annotations.Test
    public void testPerformSearchRequest() throws IOException {
        List<String> indexStr = ElasticsearchClient.getIndices(null);
    }
}