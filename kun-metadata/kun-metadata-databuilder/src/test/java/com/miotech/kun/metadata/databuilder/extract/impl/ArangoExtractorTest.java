package com.miotech.kun.metadata.databuilder.extract.impl;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.TestContainerBuilder;
import com.miotech.kun.metadata.databuilder.constant.OperatorKey;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.extract.impl.arango.ArangoExtractor;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Iterator;

@Ignore
public class ArangoExtractorTest extends DatabaseTestBase {

    @Inject
    private TestContainerBuilder containerBuilder;

    private ArangoContainer arangoContainer;

    private ArangoExtractor arangoExtractor;

    @Before
    public void setUp() {
        arangoContainer = containerBuilder.initArango();

        Props props = new Props();
        props.put(OperatorKey.EXTRACT_STATS, "false");

        arangoExtractor = new ArangoExtractor(props, ArangoDataSource.newBuilder()
                .withId(1L)
                .withUrl(arangoContainer.getHost() + ":" + arangoContainer.getPort())
                .withUsername(arangoContainer.getUser())
                .withPassword("")
                .build());
    }

    @After
    public void tearDown() {
        super.tearDown();
        arangoContainer.close();
    }

    @Test
    public void extract() {
        // execute biz logic
        Iterator<Dataset> extract = arangoExtractor.extract();

        int count = 0;
        while (extract.hasNext()) {
            extract.next();
            count++;
        }

        // verify
        MatcherAssert.assertThat(count, Matchers.is(1));
    }
}