package com.miotech.kun.dataquality.core.executor;

import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;

public interface ExpectationDatabaseOperator {

    void recordMetricsCollectedResult(Long expectationId, MetricsCollectedResult metricsCollectedResult);

    MetricsCollectedResult<String> getTheResultCollectedNDaysAgo(Long expectationId, int nDaysAgo);

    void recordValidationResult(ValidationResult vr);

}
