package com.miotech.kun.dataquality.core.assertion;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.core.factory.MockLessThanAssertionFactory;
import com.miotech.kun.dataquality.core.factory.MockSQLMetricsFactory;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LessThanAssertionTest {

    @Test
    public void testDoFixValueAssert_withExpectation() {
        LessThanAssertion lessThanAssertion = MockLessThanAssertionFactory.create("0");
        MetricsCollectedResult<String> metricsCollectedResult = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "-1");

        boolean assertionResult = lessThanAssertion.doFixValueAssert(metricsCollectedResult);
        assertTrue(assertionResult);
    }

    @Test
    public void testDoFixValueAssert_notAsExpected() {
        LessThanAssertion lessThanAssertion = MockLessThanAssertionFactory.create("0");
        MetricsCollectedResult<String> metricsCollectedResult = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "0");

        boolean assertionResult = lessThanAssertion.doFixValueAssert(metricsCollectedResult);
        assertFalse(assertionResult);
    }


}
