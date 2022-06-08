package com.miotech.kun.dataquality.core.factory;

import com.miotech.kun.dataquality.core.assertion.AbsoluteAssertion;
import com.miotech.kun.dataquality.core.assertion.ComparisonPeriod;

public class MockAbsoluteAssertionFactory {

    private MockAbsoluteAssertionFactory() {
    }

    public static AbsoluteAssertion create(String expectedValue, ComparisonPeriod comparisonPeriod) {
        return new AbsoluteAssertion("NUMBER", expectedValue, comparisonPeriod);
    }

}
