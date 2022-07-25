package com.miotech.kun.dataquality.core.converter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.miotech.kun.dataquality.core.assertion.*;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.core.metrics.SQLMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

public class CustomSQLExpectationConverterTest {

    @Test
    public void testConvertMetrics() {
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("sql", "select count(1) c from demo");
        payload.put("field", "c");

        CustomSQLExpectationConverter converter = new CustomSQLExpectationConverter();
        Metrics metrics = converter.convertMetrics(payload);

        assertThat(metrics, isA(SQLMetrics.class));
        SQLMetrics sqlMetrics = (SQLMetrics) metrics;
        assertThat(sqlMetrics.getSql(), is("select count(1) c from demo"));
        assertThat(sqlMetrics.getField(), is("c"));
    }

    @ParameterizedTest
    @MethodSource("assertionParams")
    public void testConvertAssertion(Map<String, Object> payload, Assertion assertion) {
        CustomSQLExpectationConverter converter = new CustomSQLExpectationConverter();
        Assertion assertionOfConvert = converter.convertAssertion(payload);

        assertThat(assertionOfConvert, isA(assertion.getClass()));
    }

    public static Stream<Arguments> assertionParams() {
        return Stream.of(
                Arguments.of(ImmutableMap.of("comparisonOperator", "=", "expectedValue", "0", "comparisonPeriod", 0), new EqualsAssertion("0")),
                Arguments.of(ImmutableMap.of("comparisonOperator", ">", "expectedValue", "0", "comparisonPeriod", 0), new GreaterThanAssertion("0")),
                Arguments.of(ImmutableMap.of("comparisonOperator", ">=", "expectedValue", "0", "comparisonPeriod", 0), new GreaterThanOrEqualsAssertion("0")),
                Arguments.of(ImmutableMap.of("comparisonOperator", "<", "expectedValue", "0", "comparisonPeriod", 0), new LessThanAssertion("0")),
                Arguments.of(ImmutableMap.of("comparisonOperator", "<=", "expectedValue", "0", "comparisonPeriod", 0), new LessThanOrEqualsAssertion("0")),
                Arguments.of(ImmutableMap.of("comparisonOperator", "!=", "expectedValue", "0", "comparisonPeriod", 0), new NotEqualsAssertion("0")),
                Arguments.of(ImmutableMap.of("comparisonOperator", "|x|", "expectedValue", "0", "comparisonPeriod", ComparisonPeriod.FixedPeriod.LAST_TIME.getDaysAgo()), new AbsoluteAssertion("0", ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.LAST_TIME))),
                Arguments.of(ImmutableMap.of("comparisonOperator", "+", "expectedValue", "0", "comparisonPeriod", ComparisonPeriod.FixedPeriod.LAST_TIME.getDaysAgo()), new RiseAssertion("0", ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.LAST_TIME))),
                Arguments.of(ImmutableMap.of("comparisonOperator", "-", "expectedValue", "0", "comparisonPeriod", ComparisonPeriod.FixedPeriod.LAST_TIME.getDaysAgo()), new FallAssertion("0", ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.LAST_TIME)))

        );

    }

}
