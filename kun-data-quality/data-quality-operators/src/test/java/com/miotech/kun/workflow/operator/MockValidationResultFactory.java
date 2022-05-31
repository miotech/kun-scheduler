package com.miotech.kun.workflow.operator;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.assertion.ComparisonOperator;
import com.miotech.kun.dataquality.core.expectation.AssertionResult;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;

public class MockValidationResultFactory {

    private MockValidationResultFactory() {
    }

    public static ValidationResult create(boolean passed) {
        Long expectationId = IdGenerator.getInstance().nextId();
        return ValidationResult.newBuilder()
                .withExpectationId(expectationId)
                .withExecutionResult(null)
                .withPassed(passed)
                .withContinuousFailingCount(0)
                .withAssertionResults(ImmutableList.of(AssertionResult.newBuilder()
                        .withField("n")
                        .withComparisonOperator(ComparisonOperator.EQUALS)
                        .withOperator(ComparisonOperator.EQUALS.getSymbol())
                        .withExpectedType("NUMBER")
                        .withExpectedValue("0")
                        .withOriginalValue("0")
                        .withBenchmarkValue("0")
                        .build()))
                .withUpdateTime(DateTimeUtils.now())
                .build();
    }

    public static ValidationResult create() {
        return create(true);
    }

}
