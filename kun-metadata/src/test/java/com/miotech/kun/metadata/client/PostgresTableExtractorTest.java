package com.miotech.kun.metadata.client;

import com.miotech.kun.metadata.extract.impl.postgres.PostgresTableExtractor;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.PostgresDataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

public class PostgresTableExtractorTest {

    @Test
    public void testGetSchema_ok() {
        PostgresDataSource.Builder builder = PostgresDataSource.newBuilder();
        builder.withUrl("jdbc:postgresql://192.168.1.211:5432/mdp").withUsername("docker").withPassword("docker")
                .withId(10L);

        PostgresTableExtractor postgresExtractor = new PostgresTableExtractor(builder.build(), "mdp", "public", "audit_log");

        List<DatasetField> fields = postgresExtractor.getSchema();
        MatcherAssert.assertThat(fields, Matchers.notNullValue());
    }

}
