package com.miotech.kun.dataquality.core.factory;

import com.miotech.kun.dataquality.core.assertion.LessThanAssertion;

public class MockLessThanAssertionFactory {

    private MockLessThanAssertionFactory() {
    }

    public static LessThanAssertion create(String expectedValue) {
        return new LessThanAssertion(expectedValue);
    }

}
