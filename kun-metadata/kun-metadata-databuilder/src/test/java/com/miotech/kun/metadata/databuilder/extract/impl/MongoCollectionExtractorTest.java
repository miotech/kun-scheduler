package com.miotech.kun.metadata.databuilder.extract.impl;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.databuilder.TestContainerUtil;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.impl.mongo.MongoCollectionExtractor;
import com.miotech.kun.metadata.databuilder.extract.tool.ConnectUrlUtil;
import com.miotech.kun.metadata.databuilder.model.DatasetField;
import com.miotech.kun.metadata.databuilder.model.DatasetStat;
import com.miotech.kun.metadata.databuilder.model.MongoDataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;

import java.util.List;

public class MongoCollectionExtractorTest extends DatabaseTestBase {

    @Inject
    private TestContainerUtil containerUtil;

    private MongoDBContainer mongoDBContainer;

    private MongoCollectionExtractor mongoCollectionExtractor;

    @Before
    public void setUp() {
        super.setUp();
        mongoDBContainer = containerUtil.initMongo();

        mongoCollectionExtractor = new MongoCollectionExtractor(MongoDataSource.newBuilder()
                .withId(1L)
                .withUrl(ConnectUrlUtil.convertToConnectUrl(
                        mongoDBContainer.getHost(), mongoDBContainer.getFirstMappedPort(), "", "", DatabaseType.MONGO))
                .build(), "admin", "system.keys");
    }

    @After
    public void tearDown() {
        super.tearDown();
        mongoDBContainer.close();
    }

    @Test
    public void testGetSchema() {
        // execute biz logic
        List<DatasetField> schema = mongoCollectionExtractor.getSchema();

        // verify
        MatcherAssert.assertThat(schema.size(), Matchers.is(27));
    }

    @Test
    public void testGetTableStats() {
        // execute biz logic
        DatasetStat tableStats = mongoCollectionExtractor.getTableStats();
        MatcherAssert.assertThat(tableStats, Matchers.notNullValue());
    }


}
