package com.miotech.kun.dataquality.core.assertion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import org.apache.commons.lang3.math.NumberUtils;

public class EqualsAssertion extends FixedValueAssertion {

    @JsonCreator
    public EqualsAssertion(@JsonProperty("expectedType") String expectedType,
                           @JsonProperty("expectedValue") String expectedValue) {
        super(expectedType, expectedValue, ComparisonOperator.EQUALS);
    }

    @Override
    public boolean doFixValueAssert(MetricsCollectedResult<String> metricsCollectedResult) {
        String expectedValue = getExpectedValue();
        String originalValue = metricsCollectedResult.getValue();
        if (NumberUtils.isParsable(expectedValue) || NumberUtils.isParsable(originalValue)) {
            return Double.parseDouble(expectedValue) == Double.parseDouble(originalValue);
        }

        return expectedValue.equals(originalValue);
    }

}
