package com.miotech.kun.metadata.databuilder.extract.impl;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.metadata.databuilder.TestContainerUtil;
import com.miotech.kun.metadata.databuilder.extract.impl.arango.ArangoExtractor;
import com.miotech.kun.metadata.databuilder.model.ArangoDataSource;
import com.miotech.kun.metadata.databuilder.model.Dataset;
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
    private TestContainerUtil containerUtil;

    private ArangoContainer arangoContainer;

    private ArangoExtractor arangoExtractor;

    @Before
    public void setUp() {
        super.setUp();
        arangoContainer = containerUtil.initArango();

        arangoExtractor = new ArangoExtractor(ArangoDataSource.newBuilder()
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