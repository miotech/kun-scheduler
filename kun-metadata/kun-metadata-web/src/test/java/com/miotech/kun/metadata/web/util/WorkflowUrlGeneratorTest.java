package com.miotech.kun.metadata.web.util;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import java.util.UUID;

import static com.miotech.kun.metadata.web.constant.PropKey.WORKFLOW_URL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WorkflowUrlGeneratorTest {

    private WorkflowUrlGenerator generator;
    private static final String operatorName = "Test Operator";

    @Before
    public void setUp() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                Properties properties = new Properties();
                properties.setProperty(WORKFLOW_URL, "http://your-domain");
                bind(Properties.class).toInstance(properties);
            }
        });
        generator = injector.getInstance(WorkflowUrlGenerator.class);
    }

    @Test
    public void testGenerateSearchOperatorUrl() {
        String url = generator.generateSearchOperatorUrl(operatorName);
        assertThat(url, is("http://your-domain/operators?name%3DTest+Operator"));
    }

    @Test
    public void testGenerateCreateOperatorUrl() {
        String url = generator.generateCreateOperatorUrl();
        assertThat(url, is("http://your-domain/operators"));
    }

    @Test
    public void testBuildFetchStatusUrl() {
        String id = UUID.randomUUID().toString();
        String url = generator.buildFetchStatusUrl(id);
        assertThat(url, is(String.format("http://your-domain/taskruns/%s/status", id)));
    }

    @Test
    public void testGenerateSearchTaskUrl() {
        String url = generator.generateSearchTaskUrl();
        assertThat(url, is("http://your-domain/tasks?name%3DMetadata+DataBuilder+Task"));
    }

    @Test
    public void testGenerateCreateTaskUrl() {
        String url = generator.generateCreateTaskUrl();
        assertThat(url, is("http://your-domain/tasks"));
    }

    @Test
    public void testGenerateRunTaskUrl() {
        String url = generator.generateRunTaskUrl();
        assertThat(url, is("http://your-domain/tasks/_run"));
    }

}
