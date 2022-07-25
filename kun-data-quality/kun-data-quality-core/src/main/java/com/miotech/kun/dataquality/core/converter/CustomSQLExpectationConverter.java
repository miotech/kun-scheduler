package com.miotech.kun.dataquality.core.converter;

import com.miotech.kun.dataquality.core.assertion.*;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.core.metrics.SQLMetrics;

import java.util.Map;

public class CustomSQLExpectationConverter implements ExpectationConverter {

    @Override
    public Metrics convertMetrics(Map<String, Object> payload) {
        String sql = (String) payload.get("sql");
        String field = (String) payload.get("field");
        return SQLMetrics.newBuilder()
                .withSql(sql)
                .withField(field)
                .build();
    }

    @Override
    public Assertion convertAssertion(Map<String, Object> payload) {
        ComparisonOperator comparisonOperator = ComparisonOperator.convertFrom((String) payload.get("comparisonOperator"));
        String expectedValue = (String) payload.get("expectedValue");
        Integer comparisonPeriod = (Integer) payload.get("comparisonPeriod");
        switch (comparisonOperator) {
            case EQUALS:
                return new EqualsAssertion(expectedValue);
            case LESS_THAN:
                return new LessThanAssertion(expectedValue);
            case LESS_THAN_OR_EQUALS:
                return new LessThanOrEqualsAssertion(expectedValue);
            case GREATER_THAN:
                return new GreaterThanAssertion(expectedValue);
            case GREATER_THAN_OR_EQUALS:
                return new GreaterThanOrEqualsAssertion(expectedValue);
            case NOT_EQUALS:
                return new NotEqualsAssertion(expectedValue);
            case ABSOLUTE:
                return new AbsoluteAssertion(expectedValue, new ComparisonPeriod(comparisonPeriod));
            case RISE:
                return new RiseAssertion(expectedValue, new ComparisonPeriod(comparisonPeriod));
            case FALL:
                return new FallAssertion(expectedValue, new ComparisonPeriod(comparisonPeriod));
            default:
                throw new IllegalArgumentException("Invalid comparisonOperator: " + comparisonOperator);
        }
    }

}
