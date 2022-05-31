package com.miotech.kun.dataquality.core.assertion;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.core.factory.MockRiseAssertionFactory;
import com.miotech.kun.dataquality.core.factory.MockSQLMetricsFactory;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RiseAssertionTest {

    @Test
    public void testDoVolatilityAssert_withExpectation() {
        RiseAssertion riseAssertion = MockRiseAssertionFactory.create("0", ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.THIS_TIME));
        MetricsCollectedResult<String> current = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "100");
        MetricsCollectedResult<String> benchmark = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "100");
        boolean assertionResult = riseAssertion.doVolatilityAssert(current, benchmark);
        assertTrue(assertionResult);
    }

    @Test
    public void testDoVolatilityAssert_reduce() {
        RiseAssertion riseAssertion = MockRiseAssertionFactory.create("0", ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.THIS_TIME));
        MetricsCollectedResult<String> current = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "50");
        MetricsCollectedResult<String> benchmark = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "100");
        boolean assertionResult = riseAssertion.doVolatilityAssert(current, benchmark);
        assertFalse(assertionResult);
    }

    @Test
    public void testDoVolatilityAssert_aboveUpperLimit() {
        RiseAssertion riseAssertion = MockRiseAssertionFactory.create("10", ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.THIS_TIME));
        MetricsCollectedResult<String> current = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "120");
        MetricsCollectedResult<String> benchmark = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "100");
        boolean assertionResult = riseAssertion.doVolatilityAssert(current, benchmark);
        assertFalse(assertionResult);
    }

}
