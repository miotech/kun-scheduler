package com.miotech.kun.workflow.operator;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.dataquality.core.expectation.AssertionResult;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.core.metrics.MetricsCollectedResult;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.workflow.operator.client.DataQualityClient;
import com.miotech.kun.workflow.operator.client.DataSourceClient;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import static org.mockito.Mockito.doReturn;

import static com.miotech.kun.workflow.operator.DataQualityConfiguration.INFRA_BASE_URL;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class DataQualityClientTest extends DatabaseTestBase {

    private static final String TABLE_NAME = "kun_dq_expectation";
    private static final List<String> COLUMNS = ImmutableList.of("id", "name", "types", "description", "method", "metrics_config", "assertion_config", "trigger",
            "dataset_gid", "task_id", "case_type", "create_time", "update_time", "create_user", "update_user");

    @Inject
    private DataQualityClient dataQualityClient;

    @Inject
    private DataSourceClient dataSourceClient;

    @Inject
    private DatabaseOperator databaseOperator;

    @Override
    protected void configuration() {
        super.configuration();
        setFlywayLocation("webapp_sql");
        Props props = new Props();
        props.put(INFRA_BASE_URL, "http://kun-infra");
        bind(Props.class, props);
        bind(DataSourceClient.class, Mockito.mock(DataSourceClient.class));
    }

    @Test
    public void testFindById_empty() {
        long id = IdGenerator.getInstance().nextId();
        Expectation expectation = dataQualityClient.findById(id);
        assertThat(expectation.getExpectationId(), nullValue());
        assertThat(expectation.getName(), nullValue());
        assertThat(expectation.getTypes(), nullValue());
        assertThat(expectation.getDescription(), nullValue());
        assertThat(expectation.getMethod(), nullValue());
        assertThat(expectation.getTrigger(), nullValue());
        assertThat(expectation.getMetrics(), nullValue());
        assertThat(expectation.getAssertion(), nullValue());
        assertThat(expectation.getTaskId(), nullValue());
    }

    @Test
    public void testFindById_createThenFind() {
        Expectation expectation = MockExpectationFactory.create();
        createExpectation(expectation);

        Long dataSourceId = IdGenerator.getInstance().nextId();
        DataSource dataSource = DataSource.newBuilder().withId(dataSourceId).build();
        doReturn(dataSourceId).when(dataSourceClient).getDataSourceIdByGid(expectation.getDataset().getGid());
        doReturn(dataSource).when(dataSourceClient).getDataSourceById(dataSourceId);

        Expectation fetched = dataQualityClient.findById(expectation.getExpectationId());
        assertThat(expectation, sameBeanAs(fetched).ignoring("dataset").ignoring("metrics").ignoring("assertion"));
        assertThat(expectation.getDataset().getGid(), is(fetched.getDataset().getGid()));
        assertThat(expectation.getMetrics().getName(), is(fetched.getMetrics().getName()));
        assertThat(expectation.getAssertion().getExpectedValue(), is(fetched.getAssertion().getExpectedValue()));
    }

    @Test
    public void testRecord_validationResultSuccess() {
        ValidationResult validationResult = MockValidationResultFactory.create();
        Long caseRunId = IdGenerator.getInstance().nextId();
        dataQualityClient.record(validationResult, caseRunId);

        List<ValidationResult> validationResults = dataQualityClient.fetchByExpectationId(validationResult.getExpectationId());
        assertThat(validationResults.size(), is(1));
        ValidationResult fetched = validationResults.get(0);
        assertThat(fetched, sameBeanAs(validationResult).ignoring("assertionResults"));
        assertThat(fetched.getAssertionResults().size(), is(1));
        AssertionResult assertionResult = fetched.getAssertionResults().get(0);
        assertThat(assertionResult, sameBeanAs(validationResult.getAssertionResults().get(0)));
    }

    @Test
    public void testRecord_validationResultFailed() {
        ValidationResult validationResult = MockValidationResultFactory.create(false);
        Long caseRunId = IdGenerator.getInstance().nextId();
        dataQualityClient.record(validationResult, caseRunId);

        List<ValidationResult> validationResults = dataQualityClient.fetchByExpectationId(validationResult.getExpectationId());
        assertThat(validationResults.size(), is(1));
        ValidationResult fetched = validationResults.get(0);
        assertThat(fetched, sameBeanAs(validationResult).ignoring("assertionResults").ignoring("continuousFailingCount"));
        assertThat(fetched.getAssertionResults().size(), is(1));
        assertThat(fetched.getContinuousFailingCount(), is(1L));
        AssertionResult assertionResult = fetched.getAssertionResults().get(0);
        assertThat(assertionResult, sameBeanAs(validationResult.getAssertionResults().get(0)));
    }

    @Test
    public void testRecordMetricsCollectedResult() {
        Long expectationId = IdGenerator.getInstance().nextId();
        MetricsCollectedResult metricsCollectedResult = MockMetricsCollectedResultFactory.create();
        dataQualityClient.recordMetricsCollectedResult(expectationId, metricsCollectedResult);

        MetricsCollectedResult<String> theResultCollectedNDaysAgo = dataQualityClient.getTheResultCollectedNDaysAgo(expectationId, 0);
        assertThat(theResultCollectedNDaysAgo.getValue(), is(metricsCollectedResult.getValue()));
    }

    private void createExpectation(Expectation expectation) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(COLUMNS.toArray(new String[0]))
                .into(TABLE_NAME)
                .asPrepared()
                .getSQL();
        databaseOperator.update(sql,
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

}
