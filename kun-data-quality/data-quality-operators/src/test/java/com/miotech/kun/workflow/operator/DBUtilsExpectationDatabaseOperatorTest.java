package com.miotech.kun.workflow.operator;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import com.miotech.kun.dataquality.core.metrics.SQLMetrics;
import com.miotech.kun.workflow.operator.client.DBUtilsExpectationDatabaseOperator;
import com.miotech.kun.workflow.operator.mock.MockSQLMetricsFactory;
import com.miotech.kun.workflow.operator.mock.MockValidationResultFactory;
import com.miotech.kun.workflow.testing.executor.MockOperatorContextImpl;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DBUtilsExpectationDatabaseOperatorTest extends DatabaseTestBase {

    @Inject
    private DBUtilsExpectationDatabaseOperator databaseOperator;

    @Override
    protected void configuration() {
        super.configuration();
        setFlywayLocation("webapp_sql");
    }

    @Test
    public void testRecordMetricsCollectedResult() {
        Long expectationId = IdGenerator.getInstance().nextId();
        SQLMetrics sqlMetrics = MockSQLMetricsFactory.create();
        String value = "1";
        MetricsCollectedResult<String> result = new MetricsCollectedResult<>(sqlMetrics, DateTimeUtils.now(), value);
        databaseOperator.recordMetricsCollectedResult(expectationId, result);

        int nDaysAgo = 0;
        MetricsCollectedResult<String> theResultCollectedNDaysAgo = databaseOperator.getTheResultCollectedNDaysAgo(expectationId, nDaysAgo);
        assertThat(theResultCollectedNDaysAgo.getValue(), is(value));
    }

    @Test
    public void testRecordValidationResult() {
        Long taskRunId = IdGenerator.getInstance().nextId();
        MockOperatorContextImpl operatorContext = new MockOperatorContextImpl(null, taskRunId, null);
        ExpectationContextHolder.setContext(operatorContext);

        ValidationResult validationResult = MockValidationResultFactory.create(false);
        databaseOperator.recordValidationResult(validationResult);

        Long count = databaseOperator.getLatestFailingCount(validationResult.getExpectationId());
        assertThat(count, is(1L));
    }

}
