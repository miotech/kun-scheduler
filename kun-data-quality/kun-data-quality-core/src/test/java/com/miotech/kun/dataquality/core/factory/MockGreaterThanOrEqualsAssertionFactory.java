package com.miotech.kun.dataquality.core.factory;

import com.miotech.kun.dataquality.core.assertion.GreaterThanOrEqualsAssertion;

public class MockGreaterThanOrEqualsAssertionFactory {

    private MockGreaterThanOrEqualsAssertionFactory() {
    }

    public static GreaterThanOrEqualsAssertion create(String expectedValue) {
        return new GreaterThanOrEqualsAssertion(expectedValue);
    }

}
