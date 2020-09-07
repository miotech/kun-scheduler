package com.miotech.kun.metadata.databuilder.extract.impl;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.TestContainerUtil;
import com.miotech.kun.metadata.databuilder.extract.impl.elasticsearch.ElasticSearchIndexExtractor;
import com.miotech.kun.metadata.databuilder.model.ElasticSearchDataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.Iterator;

public class ElasticsearchExtractorTest extends DatabaseTestBase {

    @Inject
    private TestContainerUtil containerUtil;

    private ElasticsearchContainer elasticsearchContainer;

    private ElasticSearchIndexExtractor elasticsearchExtractor;

    @Before
    public void setUp() {
        super.setUp();
        elasticsearchContainer = containerUtil.initEs();

        ElasticSearchDataSource dataSource = ElasticSearchDataSource.newBuilder()
                .withId(1L)
                .withUrl(elasticsearchContainer.getHost() + ":" + elasticsearchContainer.getFirstMappedPort())
                .withUsername("elastic")
                .withPassword("changeme")
                .build();

        elasticsearchExtractor = new ElasticSearchIndexExtractor(dataSource, "test_index");
    }

    @After
    public void tearDown() {
        super.tearDown();
        elasticsearchContainer.close();
    }

    @Test
    public void testExtract() {
        // execute biz logic
        Iterator<Dataset> extract = elasticsearchExtractor.extract();

        int count = 0;
        while (extract.hasNext()) {
            extract.next();
            count++;
        }

        // verify
        MatcherAssert.assertThat(count, Matchers.is(1));
    }
}