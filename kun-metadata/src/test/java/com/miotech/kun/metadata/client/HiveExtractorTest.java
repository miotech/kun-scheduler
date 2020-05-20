package com.miotech.kun.metadata.client;


import com.miotech.kun.metadata.extract.impl.HiveExtractor;
import com.miotech.kun.metadata.load.GenericLoader;
import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.model.Dataset;
import org.testng.annotations.Test;

import java.util.Iterator;

public class HiveExtractorTest {

    @Test
    public void testExtractor() {
        HiveExtractor hiveExtractor = new HiveExtractor(null);
        Iterator<Dataset> datasetIterator = hiveExtractor.extract();

        Loader loader = new GenericLoader();
        while (datasetIterator.hasNext()) {
            Dataset dataset = datasetIterator.next();

            loader.load(dataset);
        }

    }

}
