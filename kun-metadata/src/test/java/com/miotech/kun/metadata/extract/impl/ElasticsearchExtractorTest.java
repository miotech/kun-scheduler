package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.extract.impl.elasticsearch.ElasticsearchExtractor;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.ElasticSearchDataSource;
import org.junit.Test;

import java.util.Iterator;

public class ElasticsearchExtractorTest {

    @Test
    public void testExtract() {
        ElasticSearchDataSource cluster = ElasticSearchDataSource.newBuilder()
                .withDataStoreUrl("<es_ip>:11005")
                .withDataStoreUsername("")
                .withDataStorePassword("")
                .build();

        ElasticsearchExtractor extractor = new ElasticsearchExtractor( cluster);
        Iterator<Dataset> it = extractor.extract();

        while (it.hasNext()) {
            Dataset dataset = it.next();
            System.out.println(dataset);
        }
    }
}