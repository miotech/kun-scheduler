package com.miotech.kun.dataquality.mock;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;

import java.time.OffsetDateTime;

public class MockValidationResultFactory {

    private MockValidationResultFactory() {
    }

    public static ValidationResult create() {
        long expectationId = IdGenerator.getInstance().nextId();
        return create(expectationId);
    }

    public static ValidationResult create(boolean passed) {
        long expectationId = IdGenerator.getInstance().nextId();
        return create(expectationId, passed);
    }

    public static ValidationResult create(boolean passed, OffsetDateTime updateTime) {
        long expectationId = IdGenerator.getInstance().nextId();
        return create(expectationId, passed, updateTime);
    }

    public static ValidationResult create(Long expectationId) {
        return create(expectationId, false);
    }

    public static ValidationResult create(Long expectationId, boolean passed) {
        return create(expectationId, passed, DateTimeUtils.now());
    }

    public static ValidationResult create(Long expectationId, boolean passed, OffsetDateTime updateTime) {
        return ValidationResult.newBuilder()
                .withExpectationId(expectationId)
                .withPassed(passed)
                .withExecutionResult("error")
                .withAssertionResults(ImmutableList.of(MockAssertionResultFactory.create()))
                .withUpdateTime(updateTime)
                .build();
    }

}
