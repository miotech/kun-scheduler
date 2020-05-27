package com.miotech.kun.metadata.client;

import com.miotech.kun.metadata.extract.impl.postgres.PostgresExtractor;
import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.load.impl.PrintLoader;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.PostgresCluster;
import org.junit.Test;

import java.util.Iterator;

public class PostgresExtractorTest {

    @Test
    public void testExtractor() {
        PostgresCluster.Builder builder = PostgresCluster.newBuilder();
        /*builder.withUrl("jdbc:postgresql://192.168.1.211:5432/mdp").withUsername("docker").withPassword("docker")
                .withClusterId(1L);*/
        builder.withUrl("jdbc:postgresql://192.168.1.62:5432/si-dump").withUsername("postgres").withPassword("Mi0ying2017")
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
