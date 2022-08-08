package com.miotech.kun.dataquality.core.factory;

import com.miotech.kun.dataquality.core.assertion.LessThanOrEqualsAssertion;

public class MockLessThanOrEqualsAssertionFactory {

    private MockLessThanOrEqualsAssertionFactory() {
    }

    public static LessThanOrEqualsAssertion create(String expectedValue) {
        return new LessThanOrEqualsAssertion(expectedValue);
    }

}
