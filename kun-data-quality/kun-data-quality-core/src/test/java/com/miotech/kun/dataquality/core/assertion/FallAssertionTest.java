package com.miotech.kun.dataquality.core.assertion;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.core.factory.MockFallAssertionFactory;
import com.miotech.kun.dataquality.core.factory.MockSQLMetricsFactory;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FallAssertionTest {

    @Test
    public void testDoVolatilityAssert_withExpectation() {
        FallAssertion fallAssertion = MockFallAssertionFactory.create("0", ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.THIS_TIME));
        MetricsCollectedResult<String> current = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "100");
        MetricsCollectedResult<String> benchmark = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "100");
        boolean assertionResult = fallAssertion.doVolatilityAssert(current, benchmark);
        assertTrue(assertionResult);
    }

    @Test
    public void testDoVolatilityAssert_increase() {
        FallAssertion fallAssertion = MockFallAssertionFactory.create("0", ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.THIS_TIME));
        MetricsCollectedResult<String> current = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "200");
        MetricsCollectedResult<String> benchmark = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "100");
        boolean assertionResult = fallAssertion.doVolatilityAssert(current, benchmark);
        assertFalse(assertionResult);
    }

    @Test
    public void testDoVolatilityAssert_belowLowerLimit() {
        FallAssertion fallAssertion = MockFallAssertionFactory.create("10", ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.THIS_TIME));
        MetricsCollectedResult<String> current = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "80");
        MetricsCollectedResult<String> benchmark = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "100");
        boolean assertionResult = fallAssertion.doVolatilityAssert(current, benchmark);
        assertFalse(assertionResult);
    }

}
