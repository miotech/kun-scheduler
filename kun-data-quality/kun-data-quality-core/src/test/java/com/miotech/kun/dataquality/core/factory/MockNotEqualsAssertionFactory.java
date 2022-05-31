package com.miotech.kun.dataquality.core.factory;

import com.miotech.kun.dataquality.core.assertion.NotEqualsAssertion;

public class MockNotEqualsAssertionFactory {

    private MockNotEqualsAssertionFactory() {
    }

    public static NotEqualsAssertion create(String expectedValue) {
        return new NotEqualsAssertion("NUMBER", expectedValue);
    }

}
