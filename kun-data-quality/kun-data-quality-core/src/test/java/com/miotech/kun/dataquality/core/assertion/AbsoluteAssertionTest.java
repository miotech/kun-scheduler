package com.miotech.kun.dataquality.core.assertion;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.core.factory.MockAbsoluteAssertionFactory;
import com.miotech.kun.dataquality.core.factory.MockSQLMetricsFactory;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbsoluteAssertionTest {

    @Test
    public void testDoVolatilityAssert_withExpectation() {
        AbsoluteAssertion absoluteAssertion = MockAbsoluteAssertionFactory.create("0", ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.THIS_TIME));
        MetricsCollectedResult<String> current = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "100");
        MetricsCollectedResult<String> benchmark = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "100");
        boolean validateResult = absoluteAssertion.doVolatilityAssert(current, benchmark);
        assertTrue(validateResult);
    }

    @Test
    public void testDoVolatilityAssert_aboveUpperLimit() {
        AbsoluteAssertion absoluteAssertion = MockAbsoluteAssertionFactory.create("10", ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.THIS_TIME));
        MetricsCollectedResult<String> current = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "200");
        MetricsCollectedResult<String> benchmark = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "100");
        boolean validateResult = absoluteAssertion.doVolatilityAssert(current, benchmark);
        assertFalse(validateResult);
    }

    @Test
    public void testDoVolatilityAssert_belowLowerLimit() {
        AbsoluteAssertion absoluteAssertion = MockAbsoluteAssertionFactory.create("10", ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.THIS_TIME));
        MetricsCollectedResult<String> current = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "0");
        MetricsCollectedResult<String> benchmark = new MetricsCollectedResult<>(MockSQLMetricsFactory.create(), DateTimeUtils.now(), "100");
        boolean validateResult = absoluteAssertion.doVolatilityAssert(current, benchmark);
        assertFalse(validateResult);
    }

}
