package com.miotech.kun.metadata.databuilder.extract.impl;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.databuilder.TestContainerUtil;
import com.miotech.kun.metadata.databuilder.client.JDBCClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.impl.postgres.PostgresTableExtractor;
import com.miotech.kun.metadata.databuilder.extract.tool.ConnectUrlUtil;
import com.miotech.kun.metadata.databuilder.extract.tool.UseDatabaseUtil;
import com.miotech.kun.metadata.databuilder.model.DatasetField;
import com.miotech.kun.metadata.databuilder.model.DatasetFieldStat;
import com.miotech.kun.metadata.databuilder.model.DatasetStat;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.util.List;

public class PostgresTableExtractorTest extends DatabaseTestBase {

    @Inject
    private TestContainerUtil containerUtil;

    private PostgreSQLContainer postgreSQLContainer;

    private PostgresTableExtractor postgresTableExtractor;

    @Before
    public void setUp() {
        super.setUp();
        postgreSQLContainer = containerUtil.initPostgres();

        DataSource pgDataSource = JDBCClient.getDataSource(UseDatabaseUtil.useSchema(
                ConnectUrlUtil.convertToConnectUrl(postgreSQLContainer.getHost(), postgreSQLContainer.getFirstMappedPort(),
                        postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword(), DatabaseType.POSTGRES),
                "test", "public"), postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword(), DatabaseType.POSTGRES);

        postgresTableExtractor = new PostgresTableExtractor(PostgresDataSource.newBuilder()
                .withId(1L)
                .withUrl(ConnectUrlUtil.convertToConnectUrl(postgreSQLContainer.getHost(), postgreSQLContainer.getFirstMappedPort(),
                        postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword(), DatabaseType.POSTGRES))
                .withUsername(postgreSQLContainer.getUsername())
                .withPassword(postgreSQLContainer.getPassword())
                .build(), "test", "public", "bar", new DatabaseOperator(pgDataSource));
    }

    @After
    public void tearDown() {
        super.tearDown();
        postgreSQLContainer.close();
    }

    @Test
    public void testGetSchema() {
        // execute biz logic
        List<DatasetField> schema = postgresTableExtractor.getSchema();

        MatcherAssert.assertThat(schema.size(), Matchers.is(1));
    }

    @Test
    public void testGetFieldStats() {
        // execute biz logic
        List<DatasetField> schema = postgresTableExtractor.getSchema();
        for (DatasetField datasetField : schema) {
            DatasetFieldStat fieldStats = postgresTableExtractor.getFieldStats(datasetField);
            MatcherAssert.assertThat(fieldStats, Matchers.notNullValue());
        }
    }

    @Test
    public void testGetTableStats() {
        // execute biz logic
        DatasetStat tableStats = postgresTableExtractor.getTableStats();
        MatcherAssert.assertThat(tableStats, Matchers.notNullValue());
    }

}
