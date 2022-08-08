package com.miotech.kun.workflow.operator.mock;

import com.miotech.kun.dataquality.core.expectation.ExpectationTemplate;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

public class MockExpectationTemplateFactory {

    private MockExpectationTemplateFactory() {
    }

    public static ExpectationTemplate create() {
        return ExpectationTemplate.newBuilder()
                .withName("CUSTOM_SQL")
                .withGranularity(Metrics.Granularity.CUSTOM.name())
                .withDescription("desc")
                .withConverter("")
                .withDisplayParameters(com.miotech.kun.metadata.common.utils.JSONUtils.toJsonString(ImmutableMap.of()))
                .build();
    }

}
