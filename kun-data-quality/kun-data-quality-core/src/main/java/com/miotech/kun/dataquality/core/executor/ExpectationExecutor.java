package com.miotech.kun.dataquality.core.executor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.dataquality.core.assertion.Assertion;
import com.miotech.kun.dataquality.core.assertion.AssertionSample;
import com.miotech.kun.dataquality.core.converter.ExpectationConverter;
import com.miotech.kun.dataquality.core.expectation.AssertionResult;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.core.metrics.CollectContext;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ExpectationExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ExpectationExecutor.class);
    private Expectation expectation;
    private ExpectationDatabaseOperator dbOperator;

    private Metrics metrics;
    private Assertion assertion;

    private List<Runnable> beforeExecuteHooks = Lists.newArrayList();

    private List<Runnable> afterExecuteHooks = Lists.newArrayList();

    public ExpectationExecutor(Expectation expectation, ExpectationDatabaseOperator dbOperator) {
        this.expectation = expectation;
        this.dbOperator = dbOperator;

        Map<String, Object> payload = this.expectation.getPayload();
        // get converter instance
        ExpectationConverter converter = getConverterInstance();

        // build metrics
        this.metrics = converter.convertMetrics(payload);
        // build assertion
        this.assertion = converter.convertAssertion(payload);
    }

    public void registerBeforeExecuteHook(Runnable hook) {
        this.beforeExecuteHooks.add(hook);
    }

    public void registerAfterExecuteHook(Runnable hook) {
        this.afterExecuteHooks.add(hook);
    }

    public final boolean execute() {
        // run before execute hooks
        executeHooks(this.beforeExecuteHooks);

        // run expectation
        boolean isPassed = run();

        // run after execute hooks
        executeHooks(this.afterExecuteHooks);

        return isPassed;
    }

    private void executeHooks(List<Runnable> hooks) {
        for (Runnable hook : hooks) {
            try {
                hook.run();
            } catch (Exception e) {
                logger.error("execute hook failed, msg: {}", e);
            }
        }
    }

    private boolean run() {
        ValidationResult.Builder vrb = ValidationResult.newBuilder()
                .withExpectationId(this.expectation.getExpectationId())
                .withUpdateTime(DateTimeUtils.now());

        // validate
        boolean isSuccess = false;
        boolean isPassed = false;
        try {
            // collect metrics
            CollectContext context = new CollectContext(expectation.getDataset().getDataSource());
            MetricsCollectedResult<String> currentMetricsCollectedResult = metrics.collect(context);

            // record metrics
            dbOperator.recordMetricsCollectedResult(expectation.getExpectationId(), currentMetricsCollectedResult);

            // get benchmark value
            MetricsCollectedResult<String> theResultCollectedNDaysAgo = dbOperator.getTheResultCollectedNDaysAgo(expectation.getExpectationId(),
                    assertion.getComparisonPeriod().getDaysAgo());
            // assert
            isPassed = assertion.doAssert(AssertionSample.of(currentMetricsCollectedResult, theResultCollectedNDaysAgo));

            vrb.withAssertionResults(ImmutableList.of(AssertionResult.from(metrics, assertion,
                    currentMetricsCollectedResult, theResultCollectedNDaysAgo)));

            isSuccess = true;
        } catch (Exception e) {
            logger.error(String.format("caseId=%d %s", this.expectation.getExpectationId(), "Failed to run test case."), e);
            vrb.withExecutionResult(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
        } finally {
            vrb.withPassed(isPassed);
            dbOperator.recordValidationResult(vrb.build());
        }

        return isSuccess;
    }

    private final ExpectationConverter getConverterInstance() {
        try {
            String converterClassName = this.expectation.getTemplate().getConverter();
            Class<?> converterClazz = Class.forName(converterClassName);
            return (ExpectationConverter) converterClazz.newInstance();
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }



}
