package com.miotech.kun.metadata.databuilder.extract.impl;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.TestContainerUtil;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.impl.postgres.PostgresExtractor;
import com.miotech.kun.metadata.databuilder.extract.tool.ConnectUrlUtil;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Iterator;

public class PostgresExtractorTest extends DatabaseTestBase {

    @Inject
    private TestContainerUtil containerUtil;

    private PostgreSQLContainer postgreSQLContainer;

    private PostgresExtractor postgresExtractor;

    @Before
    public void setUp() {
        super.setUp();
        postgreSQLContainer = containerUtil.initPostgres();

        postgresExtractor = new PostgresExtractor(PostgresDataSource.newBuilder()
                .withId(1L)
                .withUrl(ConnectUrlUtil.convertToConnectUrl(postgreSQLContainer.getHost(), postgreSQLContainer.getFirstMappedPort(),
                        postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword(), DatabaseType.POSTGRES))
                .withUsername(postgreSQLContainer.getUsername())
                .withPassword(postgreSQLContainer.getPassword())
                .build());
    }

    @After
    public void tearDown() {
        super.tearDown();
        postgreSQLContainer.close();
    }

    @Test
    public void testExtract() {
        // execute biz logic
        Iterator<Dataset> extract = postgresExtractor.extract();

        int count = 0;
        while (extract.hasNext()) {
            extract.next();
            count++;
        }

        MatcherAssert.assertThat(count, Matchers.is(1));
    }
}
