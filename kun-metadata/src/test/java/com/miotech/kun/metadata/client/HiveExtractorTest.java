package com.miotech.kun.metadata.client;

import com.miotech.kun.metadata.extract.impl.hive.HiveExtractor;
import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.load.impl.PrintLoader;
import com.miotech.kun.metadata.model.ConfigurableDataSource;
import com.miotech.kun.metadata.model.Dataset;
import org.junit.Test;

import java.util.Iterator;

public class HiveExtractorTest {

    @Test
    public void testExtractor_mysql_hive() {
        ConfigurableDataSource.Builder builder = ConfigurableDataSource.newBuilder();

        HiveExtractor hiveExtractor = new HiveExtractor(builder.build());
        Iterator<Dataset> datasetIterator = hiveExtractor.extract();

        Loader printLoader = new PrintLoader();
        while (datasetIterator.hasNext()) {
            Dataset dataset = datasetIterator.next();
            printLoader.load(dataset);
        }

    }

    @Test
    public void testExtractor_glue_athena() {
        ConfigurableDataSource.Builder builder = ConfigurableDataSource.newBuilder();

        HiveExtractor hiveExtractor = new HiveExtractor(builder.build());
        Iterator<Dataset> datasetIterator = hiveExtractor.extract();

        Loader printLoader = new PrintLoader();
        while (datasetIterator.hasNext()) {
            Dataset dataset = datasetIterator.next();
            printLoader.load(dataset);
        }

    }

    @Test
    public void testExtractor_usePresto() {
        ConfigurableDataSource.Builder builder = ConfigurableDataSource.newBuilder();

        HiveExtractor hiveExtractor = new HiveExtractor(builder.build());
        Iterator<Dataset> datasetIterator = hiveExtractor.extract();

        Loader printLoader = new PrintLoader();
        while (datasetIterator.hasNext()) {
            Dataset dataset = datasetIterator.next();
            printLoader.load(dataset);
        }

    }

}
