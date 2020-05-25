package com.miotech.kun.metadata.client;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class ElasticsearchClientTest {

    @Test
    public void testPerformSearchRequest() throws IOException {
        List<String> indexStr = ElasticsearchClient.getIndices(null);
    }
}