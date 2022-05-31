package com.miotech.kun.dataquality.core.assertion;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.core.factory.MockGreaterThanAssertionFactory;
import com.miotech.kun.dataquality.core.factory.MockSQLMetricsFactory;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GreaterThanAssertionTest {

    @Test
    public void testDoFixValueAssert_withExpectation() {
        GreaterThanAssertion greaterThanAssertion = MockGreaterThanAssertionFactory.create("0");
        MetricsCollectedResult<String> metricsCollectedResult = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "1");

        boolean assertionResult = greaterThanAssertion.doFixValueAssert(metricsCollectedResult);
        assertTrue(assertionResult);
    }

    @Test
    public void testDoFixValueAssert_notAsExpected() {
        GreaterThanAssertion greaterThanAssertion = MockGreaterThanAssertionFactory.create("0");
        MetricsCollectedResult<String> metricsCollectedResult = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "0");

        boolean assertionResult = greaterThanAssertion.doFixValueAssert(metricsCollectedResult);
        assertFalse(assertionResult);
    }


}
