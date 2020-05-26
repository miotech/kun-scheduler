package com.miotech.kun.metadata.client;

import com.miotech.kun.metadata.extract.impl.PostgresExtractor;
import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.load.impl.PrintLoader;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.PostgresCluster;
import org.junit.Test;

import java.util.Iterator;

public class PostgresExtractorTest extends DatabaseTestBase {

    @Test
    public void testExtractor() {
        PostgresCluster.Builder builder = PostgresCluster.newBuilder();
        builder.withUrl("jdbc:postgresql://192.168.1.211:5432/mdp").withUsername("docker").withPassword("docker")
                .withClusterId(1L);

        PostgresExtractor postgresExtractor = new PostgresExtractor(builder.build());
        Iterator<Dataset> datasetIterator = postgresExtractor.extract();

        Loader printLoader = new PrintLoader();
        while (datasetIterator.hasNext()) {
            Dataset dataset = datasetIterator.next();
            printLoader.load(dataset);
        }
    }

}
