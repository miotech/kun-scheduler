package com.miotech.kun.dataquality.mock;

import com.miotech.kun.dataquality.core.assertion.ComparisonOperator;
import com.miotech.kun.dataquality.core.expectation.AssertionResult;

public class MockAssertionResultFactory {

    private MockAssertionResultFactory() {
    }

    public static AssertionResult create() {
        return AssertionResult.newBuilder()
                .withField("n")
                .withComparisonOperator(ComparisonOperator.EQUALS)
                .withOperator(ComparisonOperator.EQUALS.getSymbol())
                .withExpectedType("number")
                .withExpectedValue("0")
                .withOriginalValue("0")
                .withBenchmarkValue("0")
                .build();
    }

}
