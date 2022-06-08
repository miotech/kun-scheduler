package com.miotech.kun.workflow.operator.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.assertion.Assertion;
import com.miotech.kun.dataquality.core.expectation.*;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import com.miotech.kun.dataquality.core.metrics.SQLMetrics;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGobject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/14
 */
@Singleton
public class DataQualityClient {

    private static final String EXPECTATION_TABLE_NAME = "kun_dq_expectation";
    private static final String EXPECTATION_RUN_TABLE_NAME = "kun_dq_expectation_run";
    private static final String EXPECTATION_METRICS_COLLECTION_TABLE_NAME = "kun_dq_metrics_collection";
    private static final List<String> EXPECTATION_COLUMNS = ImmutableList.of("id", "name", "types", "description", "method",
            "metrics_config", "assertion_config", "trigger", "dataset_gid", "task_id", "case_type", "create_time",
            "update_time", "create_user", "update_user");
    private static final List<String> EXPECTATION_RUN_INSERT_COLUMNS = ImmutableList.of("expectation_id", "passed", "execution_result",
            "assertion_result", "continuous_failing_count", "update_time");

    private static final List<String> EXPECTATION_METRICS_COLLECTION_COLUMNS = ImmutableList.of("expectation_id", "execution_result", "collected_at");

    private final DatabaseOperator databaseOperator;
    private final DataSourceClient dataSourceClient;

    @Inject
    public DataQualityClient(DatabaseOperator databaseOperator, DataSourceClient dataSourceClient) {
        this.databaseOperator = databaseOperator;
        this.dataSourceClient = dataSourceClient;
    }

    public Expectation findById(Long id) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(EXPECTATION_COLUMNS.toArray(new String[0]))
                .from(EXPECTATION_TABLE_NAME)
                .where("id = ?")
                .getSQL();

        return databaseOperator.query(sql, rs -> {
            Expectation.Builder builder = Expectation.newBuilder();
            if (rs.next()) {
                Dataset dataset = buildDataset(rs);
                Metrics metrics = buildMetrics(rs, dataset);
                builder
                        .withExpectationId(rs.getLong("id"))
                        .withName(rs.getString("name"))
                        .withDescription(rs.getString("description"))
                        .withMethod(com.miotech.kun.workflow.utils.JSONUtils.jsonToObject(rs.getString("method"), ExpectationMethod.class))
                        .withMetrics(metrics)
                        .withAssertion(JSONUtils.jsonToObject(rs.getString("assertion_config"), Assertion.class))
                        .withTrigger(Expectation.ExpectationTrigger.valueOf(rs.getString("trigger")))
                        .withTaskId(rs.getLong("task_id"))
                        .withCaseType(CaseType.valueOf(rs.getString("case_type")))
                        .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("create_time")))
                        .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("update_time")))
                        .withCreateUser(rs.getString("create_user"))
                        .withUpdateUser(rs.getString("update_user"));

                builder.withDataset(dataset);
            }
            return builder.build();
        }, id);
    }



    public void record(ValidationResult vr, Long caseRunId) {
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
                transferObjectToPGObject(vr.getAssertionResults()),
                failedCount,
                vr.getUpdateTime()
                );

        String updateStatusSql = DefaultSQLBuilder.newBuilder()
                .update("kun_dq_case_run")
                .set("status")
                .where("case_run_id = ?")
                .asPrepared()
                .getSQL();
        String status = vr.isPassed() ? "SUCCESS" : "FAILED";
        databaseOperator.update(updateStatusSql, status, caseRunId);
    }

    public List<ValidationResult> fetchByExpectationId(Long expectationId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(EXPECTATION_RUN_INSERT_COLUMNS.toArray(new String[0]))
                .from(EXPECTATION_RUN_TABLE_NAME)
                .where("expectation_id = ?")
                .orderBy("id desc")
                .limit(1)
                .getSQL();
        return databaseOperator.fetchAll(sql, DataQualityClientRowMapper.INSTANCE, expectationId);
    }

    public void recordMetricsCollectedResult(Long expectationId, MetricsCollectedResult metricsCollectedResult) {
        String insertSql = DefaultSQLBuilder.newBuilder()
                .insert(EXPECTATION_METRICS_COLLECTION_COLUMNS.toArray(new String[0]))
                .into(EXPECTATION_METRICS_COLLECTION_TABLE_NAME)
                .asPrepared()
                .getSQL();
        databaseOperator.create(insertSql, expectationId,
                JSONUtils.toJsonString(metricsCollectedResult), metricsCollectedResult.getCollectedAt());
    }

    public MetricsCollectedResult<String> getTheResultCollectedNDaysAgo(Long expectationId, int nDaysAgo) {
        String sql = "select execution_result from kun_dq_metrics_collection where expectation_id = ? and collected_at < ? order by id desc limit 1";
        OffsetDateTime endOfNDaysAgo = DateTimeUtils.now().minusDays(nDaysAgo).withHour(23).withMinute(59).withSecond(59).withNano(999999000);
        String executionResult = databaseOperator.fetchOne(sql, rs -> rs.getString("execution_result"), expectationId, endOfNDaysAgo);
        if (StringUtils.isBlank(executionResult)) {
            return null;
        }

        return JSONUtils.jsonToObject(executionResult, new TypeReference<MetricsCollectedResult<String>>() {
        });
    }

    private Long getLatestFailingCount(Long expectationId) {
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

    private PGobject transferObjectToPGObject(Object obj) {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");
        try {
            if (obj == null) {
                jsonObject.setValue(null);
            } else {
                jsonObject.setValue(JSONUtils.toJsonString(obj));
            }
        } catch (SQLException e) {
            throw ExceptionUtils.wrapIfChecked(new RuntimeException(e));
        }
        return jsonObject;
    }

    private Metrics buildMetrics(ResultSet rs, Dataset dataset) throws SQLException {
        Metrics metrics = JSONUtils.jsonToObject(rs.getString("metrics_config"), Metrics.class);
        if (!(metrics instanceof SQLMetrics)) {
            throw new IllegalStateException("Invalid metrics type: " + metrics.getMetricsType().name());
        }

        SQLMetrics sqlMetrics = (SQLMetrics) metrics;
        return sqlMetrics.cloneBuilder().withDataset(dataset).build();
    }

    private Dataset buildDataset(ResultSet rs) throws SQLException {
        Long datasetGid = rs.getLong("dataset_gid");
        Long dataSourceId = dataSourceClient.getDataSourceIdByGid(datasetGid);
        DataSource dataSourceById = dataSourceClient.getDataSourceById(dataSourceId);
        return Dataset.builder().gid(datasetGid).dataSource(dataSourceById).build();
    }

    private static class DataQualityClientRowMapper implements ResultSetMapper<ValidationResult> {
        public static final DataQualityClient.DataQualityClientRowMapper INSTANCE = new DataQualityClient.DataQualityClientRowMapper();

        @Override
        public ValidationResult map(ResultSet rs) throws SQLException {
            String assertionResultStr = rs.getString("assertion_result");
            return ValidationResult.newBuilder()
                    .withExpectationId(rs.getLong("expectation_id"))
                    .withPassed(rs.getBoolean("passed"))
                    .withExecutionResult(rs.getString("execution_result"))
                    .withAssertionResults(StringUtils.isBlank(assertionResultStr) ? null : JSONUtils.jsonToObject(assertionResultStr,
                            new TypeReference<List<AssertionResult>>() {
                            }))
                    .withContinuousFailingCount(rs.getLong("continuous_failing_count"))
                    .withUpdateTime(com.miotech.kun.workflow.utils.DateTimeUtils.fromTimestamp(rs.getTimestamp("update_time")))
                    .build();
        }
    }

}
