package com.miotech.kun.metadata.databuilder.extract.impl;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.TestContainerBuilder;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.constant.OperatorKey;
import com.miotech.kun.metadata.databuilder.extract.impl.mongo.MongoExtractor;
import com.miotech.kun.metadata.databuilder.extract.tool.ConnectUrlUtil;
import com.miotech.kun.metadata.databuilder.model.MongoDataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;

import java.util.Iterator;

public class MongoExtractorTest extends DatabaseTestBase {

    @Inject
    private TestContainerBuilder containerBuilder;

    private MongoDBContainer mongoDBContainer;

    private MongoExtractor mongoExtractor;

    @Before
    public void setUp() {
        super.setUp();
        mongoDBContainer = containerBuilder.initMongo();

        Props props = new Props();
        props.put(OperatorKey.EXTRACT_STATS, "false");

        mongoExtractor = new MongoExtractor(props, MongoDataSource.newBuilder()
                .withId(1L)
                .withUrl(ConnectUrlUtil.convertToConnectUrl(
                        mongoDBContainer.getHost(), mongoDBContainer.getFirstMappedPort(), "", "", DatabaseType.MONGO))
                .build());
    }

    @After
    public void tearDown() {
        super.tearDown();
        mongoDBContainer.close();
    }

    @Test
    public void testExtract() {
        // execute biz logic
        Iterator<Dataset> extract = mongoExtractor.extract();

        int count = 0;
        while (extract.hasNext()) {
            extract.next();
            count++;
        }

        // verify
        MatcherAssert.assertThat(count, Matchers.is(10));
    }

}
