package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.extract.impl.elasticsearch.ElasticSearchIndexExtractor;
import com.miotech.kun.metadata.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ElasticSearchIndexExtractorTest {

    ElasticSearchIndexExtractor extractor;

    @Before
    public void setUp() throws Exception {
        CommonCluster cluster = CommonCluster.newBuilder()
                .withDataStoreUrl("<es_ip>:11005")
                .withDataStoreUsername("")
                .withDataStorePassword("")
                .build();


//                .withHostname("<es_ip>:11005")
//                .withPort(11005)
//                .withUsername("")
//                .withPassword("")
//                .build();
        this.extractor = new ElasticSearchIndexExtractor(cluster, "mio-narrative-cn");
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
        DatasetField datasetField = new DatasetField("labelExtendInfo.companyId", new DatasetFieldType(DatasetFieldType.Type.CHARACTER, "TEXT"), "");
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