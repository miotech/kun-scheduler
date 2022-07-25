package com.miotech.kun.dataquality.core.factory;

import com.miotech.kun.dataquality.core.assertion.ComparisonPeriod;
import com.miotech.kun.dataquality.core.assertion.FallAssertion;

public class MockFallAssertionFactory {

    private MockFallAssertionFactory() {
    }

    public static FallAssertion create(String expectedValue, ComparisonPeriod comparisonPeriod) {
        return new FallAssertion(expectedValue, comparisonPeriod);
    }

}
