package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.workflow.core.model.entity.CommonCluster;
import org.junit.Test;

import java.util.Iterator;

public class ElasticsearchExtractorTest {

    @Test
    public void testExtract() {
        CommonCluster cluster = CommonCluster.newBuilder()
                .withHostname("<es_ip>")
                .withPort(11005)
                .withUsername("")
                .withPassword("")
                .build();
        ElasticsearchExtractor extractor = new ElasticsearchExtractor( cluster);
        Iterator<Dataset> it = extractor.extract();

        while (it.hasNext()) {
            Dataset dataset = it.next();
            System.out.println(dataset);
        }
    }
}