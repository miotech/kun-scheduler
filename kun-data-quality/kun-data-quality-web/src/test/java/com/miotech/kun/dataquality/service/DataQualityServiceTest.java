package com.miotech.kun.dataquality.service;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.dataquality.DataQualityTestBase;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.dataquality.mock.*;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.common.dao.ExpectationRunDao;
import com.miotech.kun.dataquality.web.model.ValidateSqlStatus;
import com.miotech.kun.dataquality.web.model.bo.ExpectationRequest;
import com.miotech.kun.dataquality.web.model.bo.ExpectationsRequest;
import com.miotech.kun.dataquality.web.model.bo.MetricsRequest;
import com.miotech.kun.dataquality.web.model.bo.ValidateMetricsRequest;
import com.miotech.kun.dataquality.web.model.entity.*;
import com.miotech.kun.dataquality.web.persistence.DatasetRepository;
import com.miotech.kun.dataquality.web.service.DataQualityService;
import com.miotech.kun.dataquality.web.service.WorkflowService;
import com.miotech.kun.workflow.client.WorkflowClient;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.task.CheckType;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class DataQualityServiceTest extends DataQualityTestBase {

    @Autowired
    private DataQualityService dataQualityService;

    @Autowired
    private ExpectationDao expectationDao;

    @Autowired
    private ExpectationRunDao expectationRunDao;

    @SpyBean
    private DatasetRepository datasetRepository;

    @SpyBean
    private WorkflowService workflowService;

    @MockBean
    private WorkflowClient workflowClient;

    @BeforeEach
    public void mock() {
        doAnswer(invocation -> {
            Long taskId = invocation.getArgument(0, Long.class);
            TaskRun taskRun = TaskRun.newBuilder().withTask(Task.newBuilder().withId(taskId).build())
                    .withId(WorkflowIdGenerator.nextTaskRunId()).build();
            return taskRun;
        }).when(workflowClient).executeTask(anyLong(), any());

        doAnswer(invocation -> {
            Task task = invocation.getArgument(0, Task.class);
            Task createdTask = task.cloneBuilder().withId(WorkflowIdGenerator.nextTaskId()).build();
            return createdTask;
        }).when(workflowClient).createTask(any(Task.class));

        doReturn(MockOperatorFactory.createOperator())
                .when(workflowClient)
                .saveOperator(anyString(), any());

        doReturn(Optional.of(MockOperatorFactory.createOperator())).when(workflowClient).getOperator(anyString());

        doReturn(MockOperatorFactory.createOperator()).when(workflowClient).getOperator(anyLong());
    }

    @Test
    public void testGetHistory_emptyArgs() {
        List<Long> expectationIds = ImmutableList.of();
        int limit = 1;
        List<DataQualityHistoryRecords> history = dataQualityService.getHistory(expectationIds, limit);
        assertThat(history, empty());
    }

    @Test
    public void testGetHistory() {
        ValidationResult vr = MockValidationResultFactory.create();
        expectationRunDao.create(vr);

        List<Long> expectationIds = ImmutableList.of(vr.getExpectationId());
        int limit = 1;
        List<DataQualityHistoryRecords> history = dataQualityService.getHistory(expectationIds, limit);
        assertThat(history.size(), is(1));
    }

    @Test
    public void testGetCase() {
        // mock
        doReturn(1L).when(datasetRepository).findDataSourceIdByGid(anyLong());
        doNothing().when(workflowService).updateUpstreamTaskCheckType(anyLong(), any(CheckType.class));

        // prepare
        ExpectationRequest expectationRequest = MockExpectationRequestFactory.create();
        Long expectationId = dataQualityService.createExpectation(expectationRequest);

        DatasetBasic datasetBasic = MockDatasetBasicFactory.create();
        doReturn(datasetBasic).when(datasetRepository).findBasic(expectationRequest.getDatasetGid());

        ExpectationVO expectationVO = dataQualityService.getExpectation(expectationId);
        assertThat(expectationVO.getId(), is(expectationId));
        assertThat(expectationVO.getName(), is(expectationRequest.getName()));
        assertThat(expectationVO.getDescription(), is(expectationRequest.getDescription()));
    }

    @Test
    public void testGetCasesByGid() {
        // mock
        doReturn(1L).when(datasetRepository).findDataSourceIdByGid(anyLong());
        doNothing().when(workflowService).updateUpstreamTaskCheckType(anyLong(), any(CheckType.class));

        // prepare
        ExpectationRequest expectationRequest = MockExpectationRequestFactory.create();
        Long expectationId = dataQualityService.createExpectation(expectationRequest);

        ExpectationsRequest expectationsRequest = new ExpectationsRequest();
        expectationsRequest.setGid(expectationRequest.getDatasetGid());
        ExpectationBasics caseBasics = dataQualityService.getExpectationBasics(expectationsRequest);
        assertThat(caseBasics.getExpectationBasics().size(), is(1));
        ExpectationBasic caseBasic = caseBasics.getExpectationBasics().get(0);
        assertThat(caseBasic.getId(), is(expectationId));
        assertThat(caseBasic.getName(), is(expectationRequest.getName()));
    }

    @Test
    public void testDeleteExpectation() {
        Expectation spec = MockExpectationFactory.create();
        expectationDao.create(spec);
        expectationDao.createRelatedDataset(spec.getExpectationId(), ImmutableList.of(spec.getDataset().getGid()));

        dataQualityService.deleteExpectation(spec.getExpectationId());

        Expectation specOfFetched = expectationDao.fetchById(spec.getExpectationId());
        assertThat(specOfFetched, nullValue());
    }

    @Test
    public void testUpdate() {
        // prepare
        Expectation spec = MockExpectationFactory.create();
        expectationDao.create(spec);
        expectationDao.createRelatedDataset(spec.getExpectationId(), ImmutableList.of(spec.getDataset().getGid()));

        // mock
        doReturn(1L).when(datasetRepository).findDataSourceIdByGid(anyLong());
        doNothing().when(workflowService).updateUpstreamTaskCheckType(anyLong(), any(CheckType.class));
        DatasetBasic datasetBasic = MockDatasetBasicFactory.create();
        doReturn(datasetBasic).when(datasetRepository).findBasic(anyLong());

        // execute
        ExpectationRequest expectationRequest = MockExpectationRequestFactory.create();
        dataQualityService.updateExpectation(spec.getExpectationId(), expectationRequest);

        // validate
        ExpectationVO expectation = dataQualityService.getExpectation(spec.getExpectationId());
        assertThat(expectation.getName(), is(expectationRequest.getName()));
        assertThat(expectation.getDescription(), is(expectationRequest.getDescription()));
        assertThat(expectation.getTypes(), containsInAnyOrder(expectationRequest.getTypes().toArray()));
        verify(workflowService, times(1)).executeExpectation(spec.getExpectationId());
    }

    @Test
    public void testValidateSQL() {
        String database = "test_db";
        String table = "test_table";
        String field = "id";
        String type = "HIVE";
        DatasetBasic datasetBasic = MockDatasetBasicFactory.create(database, table, type);

        String sqlText = String.format("select %s from %s.%s", field, database, table);
        ValidateMetricsRequest validateMetricsRequest = MockValidateMetricsRequestFactory.create(datasetBasic.getGid(), sqlText, field);
        MetricsRequest metricsRequest = validateMetricsRequest.getMetrics();
        doReturn(datasetBasic).when(datasetRepository).findBasic(metricsRequest.getDatasetGid());

        ValidateMetricsResult validateMetricsResult = dataQualityService.validateSql(validateMetricsRequest);
        assertThat(validateMetricsResult.getValidateStatus(), is(ValidateSqlStatus.SUCCESS.getFlag()));
    }

    @Test
    public void testValidateSQL_invalidDataSourceType() {
        String database = "test_db";
        String table = "test_table";
        String field = "id";
        String invalidType = "Mongo";
        DatasetBasic datasetBasic = MockDatasetBasicFactory.create(database, table, invalidType);

        String sqlText = String.format("select %s from %s.%s", field, database, table);
        ValidateMetricsRequest validateMetricsRequest = MockValidateMetricsRequestFactory.create(datasetBasic.getGid(), sqlText, field);
        MetricsRequest metricsRequest = validateMetricsRequest.getMetrics();
        doReturn(datasetBasic).when(datasetRepository).findBasic(metricsRequest.getDatasetGid());

        ValidateMetricsResult validateMetricsResult = dataQualityService.validateSql(validateMetricsRequest);
        assertThat(validateMetricsResult.getValidateStatus(), is(ValidateSqlStatus.FAILED.getFlag()));
        assertThat(validateMetricsResult.getMessage(), is("Not supported data source."));
    }

    @Test
    public void testValidateSQL_tableNameMismatch() {
        String database = "test_db";
        String table = "test_table";
        String other_table = "other_test_table";
        String field = "id";
        String invalidType = "HIVE";
        DatasetBasic datasetBasic = MockDatasetBasicFactory.create(database, other_table, invalidType);

        String sqlText = String.format("select %s from %s.%s", field, database, table);
        ValidateMetricsRequest validateMetricsRequest = MockValidateMetricsRequestFactory.create(datasetBasic.getGid(), sqlText, field);
        MetricsRequest metricsRequest = validateMetricsRequest.getMetrics();
        doReturn(datasetBasic).when(datasetRepository).findBasic(metricsRequest.getDatasetGid());

        ValidateMetricsResult validateMetricsResult = dataQualityService.validateSql(validateMetricsRequest);
        assertThat(validateMetricsResult.getValidateStatus(), is(ValidateSqlStatus.FAILED.getFlag()));
        assertThat(validateMetricsResult.getMessage(), is("Not related to current dataset."));
    }

    @Test
    public void testValidateSQL_columnNameMismatch() {
        String database = "test_db";
        String table = "test_table";
        String field = "id";
        String other_field = "other_id";
        String invalidType = "HIVE";
        DatasetBasic datasetBasic = MockDatasetBasicFactory.create(database, table, invalidType);

        String sqlText = String.format("select %s from %s.%s", field, database, table);
        ValidateMetricsRequest validateMetricsRequest = MockValidateMetricsRequestFactory.create(datasetBasic.getGid(), sqlText, other_field);
        MetricsRequest metricsRequest = validateMetricsRequest.getMetrics();
        doReturn(datasetBasic).when(datasetRepository).findBasic(metricsRequest.getDatasetGid());

        ValidateMetricsResult validateMetricsResult = dataQualityService.validateSql(validateMetricsRequest);
        assertThat(validateMetricsResult.getValidateStatus(), is(ValidateSqlStatus.FAILED.getFlag()));
        assertThat(validateMetricsResult.getMessage(), is("The column names returned in the SQL statement are inconsistent with the validation rules."));
    }

    @Test
    public void testValidateSQL_unsupportedSQL() {
        String database = "test_db";
        String table = "test_table";
        String field = "id";
        String invalidType = "HIVE";
        DatasetBasic datasetBasic = MockDatasetBasicFactory.create(database, table, invalidType);

        String sqlText = String.format("delete from %s.%s", field, database, table);
        ValidateMetricsRequest validateMetricsRequest = MockValidateMetricsRequestFactory.create(datasetBasic.getGid(), sqlText, field);
        MetricsRequest metricsRequest = validateMetricsRequest.getMetrics();
        doReturn(datasetBasic).when(datasetRepository).findBasic(metricsRequest.getDatasetGid());

        ValidateMetricsResult validateMetricsResult = dataQualityService.validateSql(validateMetricsRequest);
        assertThat(validateMetricsResult.getValidateStatus(), is(ValidateSqlStatus.FAILED.getFlag()));
        assertThat(validateMetricsResult.getMessage(), is("Only select query is supported."));
    }

}
