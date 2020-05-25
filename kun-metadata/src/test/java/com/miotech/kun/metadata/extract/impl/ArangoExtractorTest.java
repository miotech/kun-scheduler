package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.workflow.core.model.entity.CommonCluster;
import org.junit.Test;

import java.util.Iterator;

public class ArangoExtractorTest {

    @Test
    public void extract() {
        CommonCluster cluster = CommonCluster.newBuilder()
                .withHostname("10.0.2.162")
                .withPort(8529)
                .withUsername("root")
                .withPassword("d@ta")
                .build();

        ArangoExtractor arangoExtractor = new ArangoExtractor(cluster);
        Iterator<Dataset> it = arangoExtractor.extract();
        if(it.hasNext()){
            System.out.println(it);
        }
    }
}