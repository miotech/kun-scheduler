package com.miotech.kun.dataquality.web.model.bo;

import com.miotech.kun.dataquality.core.assertion.*;
import lombok.Data;

@Data
public class AssertionRequest {

    private String comparisonOperator;

    private int comparisonPeriod = ComparisonPeriod.FixedPeriod.LAST_TIME.getDaysAgo();

    private String expectedValue;

    public Assertion convertTo() {
        ComparisonOperator comparisonOperator = ComparisonOperator.convertFrom(this.comparisonOperator);
        switch (comparisonOperator) {
            case EQUALS:
                return new EqualsAssertion(null, this.expectedValue);
            case LESS_THAN:
                return new LessThanAssertion(null, this.expectedValue);
            case LESS_THAN_OR_EQUALS:
                return new LessThanOrEqualsAssertion(null, this.expectedValue);
            case GREATER_THAN:
                return new GreaterThanAssertion(null, this.expectedValue);
            case GREATER_THAN_OR_EQUALS:
                return new GreaterThanOrEqualsAssertion(null, this.expectedValue);
            case NOT_EQUALS:
                return new NotEqualsAssertion(null, this.expectedValue);
            case ABSOLUTE:
                return new AbsoluteAssertion(null, this.expectedValue, ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.forValue(comparisonPeriod)));
            case RISE:
                return new RiseAssertion(null, this.expectedValue, ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.forValue(comparisonPeriod)));
            case FALL:
                return new FallAssertion(null, this.expectedValue, ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.forValue(comparisonPeriod)));
            default:
                throw new IllegalArgumentException("Invalid comparisonOperator: " + comparisonOperator);
        }
    }

    public static AssertionRequest convertFrom(Assertion assertion) {
        AssertionRequest assertionRequest = new AssertionRequest();
        assertionRequest.setComparisonOperator(assertion.getComparisonOperator().getSymbol());
        if (assertion instanceof VolatilityAssertion) {
            VolatilityAssertion volatilityAssertion = (VolatilityAssertion) assertion;
            assertionRequest.setComparisonPeriod(volatilityAssertion.getComparisonPeriod().getDaysAgo());
        } else {
            assertionRequest.setComparisonPeriod(ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.THIS_TIME).getDaysAgo());
        }
        assertionRequest.setExpectedValue(assertion.getExpectedValue());

        return assertionRequest;
    }

}
