package com.miotech.kun.dataquality.mock;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.JDBCExpectationAssertion;
import com.miotech.kun.dataquality.core.JDBCExpectationAssertionResult;
import com.miotech.kun.dataquality.core.ValidationResult;

public class MockValidationResultFactory {

    private MockValidationResultFactory() {
    }

    public static ValidationResult create() {
        long expectationId = IdGenerator.getInstance().nextId();
        return create(expectationId);
    }

    public static ValidationResult create(Long expectationId) {
        return ValidationResult.newBuilder()
                .withExpectationId(expectationId)
                .withPassed(false)
                .withExecutionResult("error")
                .withAssertionResults(ImmutableList.of(JDBCExpectationAssertionResult.from(createAssertion(), "1")))
                .withUpdateTime(DateTimeUtils.now())
                .build();
    }

    public static JDBCExpectationAssertion createAssertion() {
        return new JDBCExpectationAssertion("count", JDBCExpectationAssertion.ComparisonOperator.EQUALS, "=", "NUMBER", "0");
    }

}
