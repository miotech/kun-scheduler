package com.miotech.kun.dataquality.core.factory;

import com.miotech.kun.dataquality.core.assertion.GreaterThanAssertion;

public class MockGreaterThanAssertionFactory {

    private MockGreaterThanAssertionFactory() {
    }

    public static GreaterThanAssertion create(String expectedValue) {
        return new GreaterThanAssertion("NUMBER", expectedValue);
    }

}
