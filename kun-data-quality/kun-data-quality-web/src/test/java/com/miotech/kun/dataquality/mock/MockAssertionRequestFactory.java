package com.miotech.kun.dataquality.mock;

import com.miotech.kun.dataquality.core.assertion.ComparisonOperator;
import com.miotech.kun.dataquality.core.assertion.ComparisonPeriod;
import com.miotech.kun.dataquality.web.model.bo.AssertionRequest;

public class MockAssertionRequestFactory {

    private MockAssertionRequestFactory() {
    }

    public static AssertionRequest create() {
        return create(ComparisonOperator.ABSOLUTE, ComparisonPeriod.from(ComparisonPeriod.FixedPeriod.LAST_TIME));
    }

    public static AssertionRequest create(ComparisonOperator comparisonOperator, ComparisonPeriod comparisonPeriod) {
        AssertionRequest assertionRequest = new AssertionRequest();
        assertionRequest.setComparisonOperator(comparisonOperator.getSymbol());
        assertionRequest.setComparisonPeriod(comparisonPeriod.getDaysAgo());
        assertionRequest.setExpectedValue("0");

        return assertionRequest;
    }
}
