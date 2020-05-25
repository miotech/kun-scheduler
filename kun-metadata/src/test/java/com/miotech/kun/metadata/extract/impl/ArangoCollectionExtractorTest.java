package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldStat;
import com.miotech.kun.metadata.model.DatasetStat;
import com.miotech.kun.workflow.core.model.entity.CommonCluster;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ArangoCollectionExtractorTest {

    ArangoCollectionExtractor extractor;

    @Before
    public void setUp() throws Exception {
        CommonCluster cluster = CommonCluster.newBuilder()
                .withHostname("10.0.2.162")
                .withPort(8529)
                .withUsername("root")
                .withPassword("d@ta")
                .build();
        this.extractor = new ArangoCollectionExtractor(cluster, "miograph_unmerged_two", "mio_people_family_relations");
//        CommonCluster cluster = CommonCluster.newBuilder()
//                .withHostname("localhost")
//                .withPort(8529)
//                .withUsername("")
//                .withPassword("")
//                .build();
//
//        this.extractor = new ArangoCollectionExtractor(cluster, "_system", "test");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getSchema() {
        List<DatasetField> fields = extractor.getSchema();
        assert !fields.isEmpty();
    }

    @Test
    public void getFieldStats() {
        DatasetField datasetField = new DatasetField("relationDetail", "Text", "");
        DatasetFieldStat stat = extractor.getFieldStats(datasetField);
        assert stat.getNonnullCount() > 0;
    }

    @Test
    public void getTableStats() {
        DatasetStat stat = extractor.getTableStats();
        assert stat.getRowCount() > 0;
    }

    @Test
    public void getDataStore() {
    }
}