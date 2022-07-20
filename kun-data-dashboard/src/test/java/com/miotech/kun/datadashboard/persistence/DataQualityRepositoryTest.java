package com.miotech.kun.datadashboard.persistence;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadashboard.DataDashboardTestBase;
import com.miotech.kun.datadashboard.factory.MockExpectationFactory;
import com.miotech.kun.datadashboard.factory.MockValidationResultFactory;
import com.miotech.kun.datadashboard.model.bo.TestCasesRequest;
import com.miotech.kun.datadashboard.model.entity.AbnormalDatasets;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class DataQualityRepositoryTest extends DataDashboardTestBase {

    private static final String EXPECTATION_TABLE_NAME = "kun_dq_expectation";
    private static final String EXPECTATION_RUN_TABLE_NAME = "kun_dq_expectation_run";
    private static final List<String> EXPECTATION_INSERT_COLUMNS = ImmutableList.of("id", "name", "types", "description", "method", "metrics_config", "assertion_config", "trigger",
            "dataset_gid", "task_id", "case_type", "create_time", "update_time", "create_user", "update_user");
    private static final List<String> EXPECTATION_RUN_INSERT_COLUMNS = ImmutableList.of("expectation_id", "passed", "execution_result", "assertion_result", "continuous_failing_count", "update_time");


    @Autowired
    private DataQualityRepository dataQualityRepository;

    @Test
    public void testGetAbnormalDatasets_empty() {
        TestCasesRequest request = new TestCasesRequest();
        AbnormalDatasets abnormalDatasets = dataQualityRepository.getAbnormalDatasets(request);
        assertThat(abnormalDatasets.getAbnormalDatasets(), empty());
    }

    @Test
    public void testGetSuccessCount_expectationAlreadyDeleted() {
        Long expectationId = IdGenerator.getInstance().nextId();
        ValidationResult validationResult = MockValidationResultFactory.create(expectationId, true);
        insertExpectationRun(validationResult);

        Long totalCaseCount = dataQualityRepository.getTotalCaseCount();
        Long successCount = dataQualityRepository.getSuccessCount();
        Long expectationRunTotalCount = getExpectationRunTotalCount();
        assertThat(totalCaseCount, is(0L));
        assertThat(successCount, is(0L));
        assertThat(expectationRunTotalCount, is(1L));
    }

    @Test
    public void testGetSuccessCount_sameUpdateTime() {
        Expectation expectation1 = MockExpectationFactory.create();
        Expectation expectation2 = expectation1.cloneBuilder().withExpectationId(IdGenerator.getInstance().nextId()).withName("expectation2").withUpdateTime(expectation1.getUpdateTime()).build();

        insertExpectation(expectation1);
        insertExpectation(expectation2);

        Long expectationTotalCount = getExpectationTotalCount();
        assertThat(expectationTotalCount, is(2L));

        Long expectationId = expectation1.getExpectationId();
        ValidationResult validationResult = MockValidationResultFactory.create(expectationId, true).cloneBuilder().withUpdateTime(expectation1.getUpdateTime()).build();
        insertExpectationRun(validationResult);

        Long successCount = dataQualityRepository.getSuccessCount();
        assertThat(successCount, is(1L));
    }

    @Test
    public void testGetLongExistingCount() {
        long continuousFailingCountBelowThreshold = 1;
        long continuousFailingCountAboveThreshold = 100;

        Expectation expectation = MockExpectationFactory.create();
        insertExpectation(expectation);
        ValidationResult validationResult1 = MockValidationResultFactory.create(expectation.getExpectationId(), false).cloneBuilder().withContinuousFailingCount(continuousFailingCountBelowThreshold).build();
        ValidationResult validationResult2 = MockValidationResultFactory.create(expectation.getExpectationId(), false).cloneBuilder().withContinuousFailingCount(continuousFailingCountAboveThreshold).build();
        insertExpectationRun(validationResult1);
        insertExpectationRun(validationResult2);

        Long longExistingCount = dataQualityRepository.getLongExistingCount();
        assertThat(longExistingCount, is(1L));
    }

    private Long getExpectationTotalCount() {
        String sql = "select count(1) c from kun_dq_expectation";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    private Long getExpectationRunTotalCount() {
        String sql = "select count(1) c from kun_dq_expectation_run";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    private void insertExpectation(Expectation expectation) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(EXPECTATION_INSERT_COLUMNS.toArray(new String[0]))
                .into(EXPECTATION_TABLE_NAME)
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(sql,
                expectation.getExpectationId(),
                expectation.getName(),
                StringUtils.join(expectation.getTypes(), ","),
                expectation.getDescription(),
                JSONUtils.toJsonString(expectation.getMethod()),
                JSONUtils.toJsonString(expectation.getMetrics()),
                JSONUtils.toJsonString(expectation.getAssertion()),
                expectation.getTrigger().name(),
                expectation.getDataset().getGid(),
                expectation.getTaskId(),
                expectation.getCaseType().name(),
                expectation.getCreateTime(),
                expectation.getUpdateTime(),
                expectation.getCreateUser(),
                expectation.getUpdateUser());
    }

    private void insertExpectationRun(ValidationResult validationResult) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(EXPECTATION_RUN_INSERT_COLUMNS.toArray(new String[0]))
                .into(EXPECTATION_RUN_TABLE_NAME)
                .asPrepared()
                .getSQL();

        jdbcTemplate.update(sql,
                validationResult.getExpectationId(),
                validationResult.isPassed(),
                validationResult.getExecutionResult(),
                JSONUtils.toJsonString(validationResult.getAssertionResults()),
                validationResult.getContinuousFailingCount(),
                validationResult.getUpdateTime());
    }

}
