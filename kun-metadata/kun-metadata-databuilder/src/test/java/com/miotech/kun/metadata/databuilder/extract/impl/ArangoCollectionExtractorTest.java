package com.miotech.kun.metadata.databuilder.extract.impl;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldStat;
import com.miotech.kun.metadata.core.model.DatasetStat;
import com.miotech.kun.metadata.databuilder.TestContainerBuilder;
import com.miotech.kun.metadata.databuilder.constant.OperatorKey;
import com.miotech.kun.metadata.databuilder.extract.impl.arango.ArangoCollectionExtractor;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Ignore
public class ArangoCollectionExtractorTest extends DatabaseTestBase {

    @Inject
    private TestContainerBuilder containerBuilder;

    private ArangoContainer arangoContainer;

    private ArangoCollectionExtractor arangoCollectionExtractor;

    private String database = "test_db";
    private String collection = "test_collection";

    @Before
    public void setUp() {
        super.setUp();
        arangoContainer = containerBuilder.initArango();

        Props props = new Props();
        props.put(OperatorKey.EXTRACT_STATS, "false");

        arangoCollectionExtractor = new ArangoCollectionExtractor(props, ArangoDataSource.newBuilder()
                .withId(1L)
                .withUrl(arangoContainer.getHost() + ":" + arangoContainer.getPort())
                .withUsername(arangoContainer.getUser())
                .withPassword("")
                .build(), database, collection);
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
        assertThat(schema.size(), is(4));
    }

    @Test
    public void getFieldStats() {
        // execute biz logic
        List<DatasetField> schema = arangoCollectionExtractor.getSchema();
        for (DatasetField datasetField : schema) {
            DatasetFieldStat fieldStats = arangoCollectionExtractor.getFieldStats(datasetField);
            assertThat(fieldStats, notNullValue());
        }
    }

    @Test
    public void getTableStats() {
        // execute biz logic
        DatasetStat tableStats = arangoCollectionExtractor.getTableStats();
        assertThat(tableStats, notNullValue());
    }

    @Test
    public void getName() {
        // execute biz logic
        String collectionName = arangoCollectionExtractor.getName();
        assertThat(collectionName, is(collection));
    }

}