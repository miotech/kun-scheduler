package com.miotech.kun.dataquality.core.factory;

import com.miotech.kun.dataquality.core.assertion.EqualsAssertion;
import com.miotech.kun.dataquality.core.expectation.ExpectationTemplate;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.metadata.common.utils.JSONUtils;
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
                .withDisplayParameters(JSONUtils.toJsonString(ImmutableMap.of()))
                .build();
    }

    public static ExpectationTemplate create(String converter) {
        return ExpectationTemplate.newBuilder()
                .withName("CUSTOM_SQL")
                .withGranularity(Metrics.Granularity.CUSTOM.name())
                .withDescription("desc")
                .withConverter(converter)
                .withDisplayParameters(JSONUtils.toJsonString(ImmutableMap.of()))
                .build();
    }

    public static ExpectationTemplate create(String collectMethod, String granularity, String converter) {
        EqualsAssertion equalsAssertion = new EqualsAssertion("0");
        return ExpectationTemplate.newBuilder()
                .withName(collectMethod)
                .withGranularity(granularity)
                .withDescription("desc")
                .withConverter(converter)
                .withDisplayParameters(JSONUtils.toJsonString(equalsAssertion))
                .build();
    }

}
