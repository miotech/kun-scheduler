package com.miotech.kun.dataquality.core.factory;

import com.miotech.kun.dataquality.core.assertion.ComparisonPeriod;
import com.miotech.kun.dataquality.core.assertion.RiseAssertion;

public class MockRiseAssertionFactory {

    private MockRiseAssertionFactory() {
    }

    public static RiseAssertion create(String expectedValue, ComparisonPeriod comparisonPeriod) {
        return new RiseAssertion(expectedValue, comparisonPeriod);
    }

}
