package com.miotech.kun.metadata.databuilder.extract.impl;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.databuilder.TestContainerUtil;
import com.miotech.kun.metadata.databuilder.extract.impl.arango.ArangoCollectionExtractor;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import com.miotech.kun.metadata.databuilder.model.DatasetField;
import com.miotech.kun.metadata.databuilder.model.DatasetFieldStat;
import com.miotech.kun.metadata.databuilder.model.DatasetStat;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

@Ignore
public class ArangoCollectionExtractorTest extends DatabaseTestBase {

    @Inject
    private TestContainerUtil containerUtil;

    private ArangoContainer arangoContainer;

    private ArangoCollectionExtractor arangoCollectionExtractor;

    @Before
    public void setUp() {
        super.setUp();
        arangoContainer = containerUtil.initArango();

        arangoCollectionExtractor = new ArangoCollectionExtractor(ArangoDataSource.newBuilder()
                .withId(1L)
                .withUrl(arangoContainer.getHost() + ":" + arangoContainer.getPort())
                .withUsername(arangoContainer.getUser())
                .withPassword("")
                .build(), "test_db", "test_collection");
    }

    @After
    public void tearDown() {
        super.tearDown();
        arangoContainer.close();
    }

    @Test
    public void getSchema() {
        // execute biz logic
        List<DatasetField> schema = arangoCollectionExtractor.getSchema();

        // verify
        MatcherAssert.assertThat(schema.size(), Matchers.is(4));
    }

    @Test
    public void getFieldStats() {
        // execute biz logic
        List<DatasetField> schema = arangoCollectionExtractor.getSchema();
        for (DatasetField datasetField : schema) {
            DatasetFieldStat fieldStats = arangoCollectionExtractor.getFieldStats(datasetField);
            MatcherAssert.assertThat(fieldStats, Matchers.notNullValue());
        }
    }

    @Test
    public void getTableStats() {
        // execute biz logic
        DatasetStat tableStats = arangoCollectionExtractor.getTableStats();
        MatcherAssert.assertThat(tableStats, Matchers.notNullValue());
    }

}