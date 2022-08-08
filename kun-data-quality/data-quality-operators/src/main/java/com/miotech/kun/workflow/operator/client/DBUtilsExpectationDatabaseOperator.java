package com.miotech.kun.workflow.operator.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.core.executor.ExpectationDatabaseOperator;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.workflow.operator.ExpectationContextHolder;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

@Singleton
public class DBUtilsExpectationDatabaseOperator implements ExpectationDatabaseOperator {

    private static final String EXPECTATION_RUN_TABLE_NAME = "kun_dq_expectation_run";
    private static final String CASE_RUN_TABLE_NAME = "kun_dq_case_run";

    private static final String EXPECTATION_METRICS_COLLECTION_TABLE_NAME = "kun_dq_metrics_collection";
    private static final List<String> EXPECTATION_RUN_INSERT_COLUMNS = ImmutableList.of("expectation_id", "passed", "execution_result",
            "assertion_result", "continuous_failing_count", "update_time");

    private static final List<String> EXPECTATION_METRICS_COLLECTION_COLUMNS = ImmutableList.of("expectation_id", "execution_result", "collected_at");

    private DatabaseOperator databaseOperator;

    @Inject
    public DBUtilsExpectationDatabaseOperator(DatabaseOperator databaseOperator) {
        this.databaseOperator = databaseOperator;
    }

    @Override
    public void recordMetricsCollectedResult(Long expectationId, MetricsCollectedResult metricsCollectedResult) {
        String insertSql = DefaultSQLBuilder.newBuilder()
                .insert(EXPECTATION_METRICS_COLLECTION_COLUMNS.toArray(new String[0]))
                .into(EXPECTATION_METRICS_COLLECTION_TABLE_NAME)
                .asPrepared()
                .getSQL();
        databaseOperator.create(insertSql, expectationId,
                JSONUtils.toJsonString(metricsCollectedResult), metricsCollectedResult.getCollectedAt());
    }

    @Override
    public MetricsCollectedResult<String> getTheResultCollectedNDaysAgo(Long expectationId, int nDaysAgo) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("execution_result")
                .from(EXPECTATION_METRICS_COLLECTION_TABLE_NAME)
                .where("expectation_id = ? and collected_at < ?")
                .orderBy("id desc")
                .limit(1)
                .getSQL();
        OffsetDateTime endOfNDaysAgo = DateTimeUtils.now().minusDays(nDaysAgo).withHour(23).withMinute(59).withSecond(59).withNano(999999000);
        String executionResult = databaseOperator.fetchOne(sql, rs -> rs.getString("execution_result"), expectationId, endOfNDaysAgo);
        if (StringUtils.isBlank(executionResult)) {
            return null;
        }

        return JSONUtils.jsonToObject(executionResult, new TypeReference<MetricsCollectedResult<String>>() {
        });
    }

    @Override
    public void recordValidationResult(ValidationResult vr) {
        long failedCount = 0;
        if (!vr.isPassed()) {
            failedCount = getLatestFailingCount(vr.getExpectationId()) + 1;
        }

        String sql = DefaultSQLBuilder.newBuilder()
                .insert(EXPECTATION_RUN_INSERT_COLUMNS.toArray(new String[0]))
                .into(EXPECTATION_RUN_TABLE_NAME)
                .asPrepared()
                .getSQL();

        databaseOperator.create(sql,
                vr.getExpectationId(),
                vr.isPassed(),
                vr.getExecutionResult(),
                JSONUtils.toJsonString(vr.getAssertionResults()),
                failedCount,
                vr.getUpdateTime()
        );

        String updateStatusSql = DefaultSQLBuilder.newBuilder()
                .update(CASE_RUN_TABLE_NAME)
                .set("status")
                .where("case_run_id = ?")
                .asPrepared()
                .getSQL();
        String status = vr.isPassed() ? "SUCCESS" : "FAILED";

        Long taskRunId = ExpectationContextHolder.getContext().getTaskRunId();
        databaseOperator.update(updateStatusSql, status, taskRunId);
    }

    public Long getLatestFailingCount(Long expectationId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("continuous_failing_count")
                .from(EXPECTATION_RUN_TABLE_NAME)
                .where("expectation_id = ?")
                .orderBy("update_time desc")
                .limit(1)
                .getSQL();

        Long latestFailingCount = databaseOperator.fetchOne(sql, rs -> rs.getLong("continuous_failing_count"), expectationId);
        if (latestFailingCount == null) {
            return 0L;
        }
        return latestFailingCount;
    }
}
