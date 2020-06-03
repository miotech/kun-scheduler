package com.miotech.kun.metadata.client;

import com.google.gson.Gson;
import com.miotech.kun.metadata.extract.impl.hive.HiveTableExtractor;
import com.miotech.kun.metadata.model.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class HiveTableExtractorTest {
    private static Logger logger = LoggerFactory.getLogger(HiveTableExtractorTest.class);

    @Test
    public void testGetSchema() {
        HiveCluster.Builder clusterBuilder = HiveCluster.newBuilder();
        clusterBuilder.withMetaStoreUrl("jdbc:mysql://10.0.0.85:13306/hive")
                .withMetaStoreUsername("miotech")
                .withMetaStorePassword("Mi0Tech@2018")
                .withDataStoreUrl("jdbc:hive2://10.0.0.85:10000")
                .withDataStoreUsername("hive")
                .withDataStorePassword(null);
        HiveTableExtractor extractor = new HiveTableExtractor(clusterBuilder.build(), "sys", "dbs", null);
        List<DatasetField> schema = extractor.getSchema();
        logger.info("schema:" + new Gson().toJson(schema));
    }

    @Test
    public void testGetTableStats() {
        HiveCluster.Builder clusterBuilder = HiveCluster.newBuilder();
        clusterBuilder.withMetaStoreUrl("jdbc:mysql://10.0.0.85:13306/hive")
                .withMetaStoreUsername("miotech")
                .withMetaStorePassword("Mi0Tech@2018")
                .withDataStoreUrl("jdbc:hive2://10.0.0.85:10000")
                .withDataStoreUsername("hive")
                .withDataStorePassword(null);
        HiveTableExtractor extractor = new HiveTableExtractor(clusterBuilder.build(), "sys", "dbs", null);
        DatasetStat tableStats = extractor.getTableStats();
        logger.info("tableStats:" + new Gson().toJson(tableStats));
    }


    @Test
    public void testGetFieldStats() {
        HiveCluster.Builder clusterBuilder = HiveCluster.newBuilder();
        clusterBuilder.withMetaStoreUrl("jdbc:mysql://10.0.0.85:13306/hive")
                .withMetaStoreUsername("miotech")
                .withMetaStorePassword("Mi0Tech@2018")
                .withDataStoreUrl("jdbc:hive2://10.0.0.85:10000")
                .withDataStoreUsername("hive")
                .withDataStorePassword(null);
        HiveTableExtractor extractor = new HiveTableExtractor(clusterBuilder.build(), "sys", "dbs", null);
        DatasetField field = new DatasetField("db_id", new DatasetFieldType(DatasetFieldType.convertRawType("string"), "string"), null);
        DatasetFieldStat fieldStats = extractor.getFieldStats(field);
        logger.info("fieldStats:" + new Gson().toJson(fieldStats));
    }

    @Test
    public void testExtract() {
        HiveCluster.Builder clusterBuilder = HiveCluster.newBuilder();
        clusterBuilder.withMetaStoreUrl("jdbc:mysql://10.0.0.85:13306/hive")
                .withMetaStoreUsername("miotech")
                .withMetaStorePassword("Mi0Tech@2018")
                .withDataStoreUrl("jdbc:hive2://10.0.0.85:10000")
                .withDataStoreUsername("hive")
                .withDataStorePassword(null);
        HiveTableExtractor extractor = new HiveTableExtractor(clusterBuilder.build(), "sys", "dbs", null);
        DatasetField field = new DatasetField("db_id", new DatasetFieldType(DatasetFieldType.convertRawType("string"), "string"), null);
        Iterator<Dataset> extract = extractor.extract();
        while (extract.hasNext()) {
            Dataset next = extract.next();
            logger.info("dataset:" + new Gson().toJson(next));
        }
    }

}
