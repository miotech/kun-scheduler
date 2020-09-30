package com.miotech.kun.metadata.databuilder.extract.impl;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldStat;
import com.miotech.kun.metadata.core.model.DatasetStat;
import com.miotech.kun.metadata.databuilder.TestContainerBuilder;
import com.miotech.kun.metadata.databuilder.constant.OperatorKey;
import com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch.ElasticSearchIndexExtractor;
import com.miotech.kun.metadata.databuilder.model.ElasticSearchDataSource;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ElasticSearchIndexExtractorTest extends DatabaseTestBase {

    @Inject
    private TestContainerBuilder containerBuilder;

    private ElasticsearchContainer elasticsearchContainer;

    private ElasticSearchIndexExtractor elasticSearchIndexExtractor;

    private String index = "test_index";

    @Before
    public void setUp() {
        super.setUp();
        elasticsearchContainer = containerBuilder.initEs();

        Props props = new Props();
        props.put(OperatorKey.EXTRACT_STATS, "false");

        ElasticSearchDataSource dataSource = ElasticSearchDataSource.newBuilder()
                .withId(1L)
                .withUrl(elasticsearchContainer.getHost() + ":" + elasticsearchContainer.getFirstMappedPort())
                .withUsername("elastic")
                .withPassword("changeme")
                .build();

        elasticSearchIndexExtractor = new ElasticSearchIndexExtractor(props, dataSource, index);
    }

    @After
    public void tearDown() {
        super.tearDown();
        elasticsearchContainer.close();
    }

    @Test
    public void getSchema() {
        // execute biz logic
        List<DatasetField> schema = elasticSearchIndexExtractor.getSchema();

        // verify
        assertThat(schema.size(), Matchers.is(2));
    }

    @Test
    public void getFieldStats() {
        // execute biz logic
        List<DatasetField> schema = elasticSearchIndexExtractor.getSchema();
        for (DatasetField datasetField : schema) {
            DatasetFieldStat fieldStats = elasticSearchIndexExtractor.getFieldStats(datasetField);
            assertThat(fieldStats, notNullValue());
        }
    }

    @Test
    public void getTableStats() {
        // execute biz logic
        DatasetStat tableStats = elasticSearchIndexExtractor.getTableStats();
        assertThat(tableStats, notNullValue());
    }

    @Test
    public void getName() {
        // execute biz logic
        String indexName = elasticSearchIndexExtractor.getName();
        assertThat(indexName, is(index));
    }

}