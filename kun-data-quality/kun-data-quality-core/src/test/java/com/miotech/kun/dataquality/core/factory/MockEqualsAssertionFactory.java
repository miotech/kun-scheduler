package com.miotech.kun.dataquality.core.factory;

import com.miotech.kun.dataquality.core.assertion.EqualsAssertion;

public class MockEqualsAssertionFactory {

    private MockEqualsAssertionFactory() {
    }

    public static EqualsAssertion create(String expectedValue) {
        return new EqualsAssertion(expectedValue);
    }

}
