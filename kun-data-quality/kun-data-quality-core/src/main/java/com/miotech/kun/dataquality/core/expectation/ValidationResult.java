package com.miotech.kun.dataquality.core.expectation;

import java.time.OffsetDateTime;
import java.util.List;

public class ValidationResult {

    private final long expectationId;

    private final boolean passed;

    private final String executionResult;

    private final List<AssertionResult> assertionResults;

    private final long continuousFailingCount;

    private final OffsetDateTime updateTime;

    public ValidationResult(long expectationId, boolean passed, String executionResult, List<AssertionResult> assertionResults, long continuousFailingCount, OffsetDateTime updateTime) {
        this.expectationId = expectationId;
        this.passed = passed;
        this.executionResult = executionResult;
        this.assertionResults = assertionResults;
        this.continuousFailingCount = continuousFailingCount;
        this.updateTime = updateTime;
    }

    public long getExpectationId() {
        return expectationId;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getExecutionResult() {
        return executionResult;
    }

    public List<AssertionResult> getAssertionResults() {
        return assertionResults;
    }

    public long getContinuousFailingCount() {
        return continuousFailingCount;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return newBuilder()
                .withExpectationId(this.expectationId)
                .withPassed(this.passed)
                .withExecutionResult(this.executionResult)
                .withAssertionResults(this.assertionResults)
                .withContinuousFailingCount(this.continuousFailingCount)
                .withUpdateTime(this.updateTime)
                ;
    }

    public static final class Builder {
        private long expectationId;
        private boolean passed;
        private String executionResult;
        private List<AssertionResult> assertionResults;
        private long continuousFailingCount;
        private OffsetDateTime updateTime;

        private Builder() {
        }

        public Builder withExpectationId(long expectationId) {
            this.expectationId = expectationId;
            return this;
        }

        public Builder withPassed(boolean passed) {
            this.passed = passed;
            return this;
        }

        public Builder withExecutionResult(String executionResult) {
            this.executionResult = executionResult;
            return this;
        }

        public Builder withAssertionResults(List<AssertionResult> assertionResults) {
            this.assertionResults = assertionResults;
            return this;
        }

        public Builder withContinuousFailingCount(long continuousFailingCount) {
            this.continuousFailingCount = continuousFailingCount;
            return this;
        }

        public Builder withUpdateTime(OffsetDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public ValidationResult build() {
            return new ValidationResult(expectationId, passed, executionResult, assertionResults, continuousFailingCount, updateTime);
        }
    }
}
