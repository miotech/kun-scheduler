package com.miotech.kun.dataquality.core.assertion;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.core.factory.MockLessThanOrEqualsAssertionFactory;
import com.miotech.kun.dataquality.core.factory.MockSQLMetricsFactory;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LessThanOrEqualsAssertionTest {

    @Test
    public void testDoFixValueAssert_equals() {
        LessThanOrEqualsAssertion lessThanOrEqualsAssertion = MockLessThanOrEqualsAssertionFactory.create("0");
        MetricsCollectedResult<String> metricsCollectedResult = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "0");

        boolean assertionResult = lessThanOrEqualsAssertion.doFixValueAssert(metricsCollectedResult);
        assertTrue(assertionResult);
    }

    @Test
    public void testDoFixValueAssert_less() {
        LessThanOrEqualsAssertion lessThanOrEqualsAssertion = MockLessThanOrEqualsAssertionFactory.create("0");
        MetricsCollectedResult<String> metricsCollectedResult = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "-1");

        boolean assertionResult = lessThanOrEqualsAssertion.doFixValueAssert(metricsCollectedResult);
        assertTrue(assertionResult);
    }

    @Test
    public void testDoFixValueAssert_notAsExpected() {
        LessThanOrEqualsAssertion lessThanOrEqualsAssertion = MockLessThanOrEqualsAssertionFactory.create("0");
        MetricsCollectedResult<String> metricsCollectedResult = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "1");

        boolean assertionResult = lessThanOrEqualsAssertion.doFixValueAssert(metricsCollectedResult);
        assertFalse(assertionResult);
    }


}
