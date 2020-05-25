package com.miotech.kun.metadata.client;

import com.miotech.kun.metadata.extract.impl.HiveExtractor;
import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.load.impl.PrintLoader;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.workflow.core.model.entity.HiveCluster;
import org.junit.Test;

import java.util.Iterator;

public class HiveExtractorTest {

    @Test
    public void testExtractor() {
        HiveCluster.Builder builder = HiveCluster.newBuilder();
        builder.withDataStoreUrl("jdbc:hive2://10.0.0.85:10000").withDataStoreUsername("hive").withDataStorePassword(null)
                .withMetaStoreUrl("jdbc:mysql://10.0.0.85:13306/hive").withMetaStoreUsername("miotech").withMetaStorePassword("Mi0Tech@2018")
                .withClusterId(1L);

        HiveExtractor hiveExtractor = new HiveExtractor(builder.build());
        Iterator<Dataset> datasetIterator = hiveExtractor.extract();

        Loader printLoader = new PrintLoader();
//        Loader pgLoader = new PostgresLoader("jdbc:mysql://localhost:3306/unden?useSSL=false", "root", "123456");
        while (datasetIterator.hasNext()) {
            Dataset dataset = datasetIterator.next();
            printLoader.load(dataset);
//            pgLoader.load(dataset);
        }

    }

}
