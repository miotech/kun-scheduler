package com.miotech.kun.metadata.client;

import com.miotech.kun.metadata.extract.impl.mongo.MongoExtractor;
import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.load.impl.PrintLoader;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.MongoCluster;
import org.junit.Test;

import java.util.Iterator;

public class MongoExtractorTest {

    @Test
    public void testExtract() {
        MongoExtractor extractor = new MongoExtractor(new MongoCluster(1L, "mongodb://127.0.0.1:27017", "", ""));
        Iterator<Dataset> datasets = extractor.extract();

        Loader printLoader = new PrintLoader();
        while (datasets.hasNext()) {
            Dataset dataset = datasets.next();
            printLoader.load(dataset);
        }

    }

}
