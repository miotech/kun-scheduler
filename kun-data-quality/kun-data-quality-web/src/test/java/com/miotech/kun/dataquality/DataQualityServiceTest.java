package com.miotech.kun.dataquality;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.ExpectationSpec;
import com.miotech.kun.dataquality.core.ValidationResult;
import com.miotech.kun.dataquality.mock.*;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.common.dao.ExpectationRunDao;
import com.miotech.kun.dataquality.web.model.bo.DataQualitiesRequest;
import com.miotech.kun.dataquality.web.model.bo.ValidateSqlRequest;
import com.miotech.kun.dataquality.web.model.entity.*;
import com.miotech.kun.dataquality.web.persistence.DatasetRepository;
import com.miotech.kun.dataquality.web.service.DataQualityService;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doReturn;

public class DataQualityServiceTest extends DataQualityTestBase {

    @Autowired
    private DataQualityService dataQualityService;

    @Autowired
    private ExpectationDao expectationDao;

    @Autowired
    private ExpectationRunDao expectationRunDao;

    @SpyBean
    private DatasetRepository datasetRepository;

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
    public void testGetDimensionConfig() {
        DimensionConfig dimensionConfig = dataQualityService.getDimensionConfig(StringUtils.EMPTY);
        assertThat(dimensionConfig, notNullValue());
        JSONArray dimensionConfigs = dimensionConfig.getDimensionConfigs();
        assertThat(dimensionConfigs.size(), is(1));
        JSONObject jsonObject = (JSONObject) dimensionConfigs.get(0);
        assertThat(jsonObject.size(), is(2));
    }

    @Test
    public void testGetCase() {
        ExpectationSpec spec = MockExpectationSpecFactory.create();
        expectationDao.create(spec);
        expectationDao.createRelatedDataset(spec.getExpectationId(), ImmutableList.of(spec.getDataset().getGid()));
        DatasetBasic datasetBasic = MockDatasetBasicFactory.create();
        doReturn(datasetBasic).when(datasetRepository).findBasic(spec.getDataset().getGid());

        DataQualityCase dataQualityCase = dataQualityService.getCase(spec.getExpectationId());
        assertThat(dataQualityCase.getId(), is(spec.getExpectationId()));
        assertThat(dataQualityCase.getName(), is(spec.getName()));
        assertThat(dataQualityCase.getDescription(), is(spec.getDescription()));
    }

    @Test
    public void testGetCasesByGid() {
        ExpectationSpec spec = MockExpectationSpecFactory.create();
        expectationDao.create(spec);
        expectationDao.createRelatedDataset(spec.getExpectationId(), ImmutableList.of(spec.getDataset().getGid()));

        DataQualitiesRequest dataQualitiesRequest = new DataQualitiesRequest();
        dataQualitiesRequest.setGid(spec.getDataset().getGid());
        DataQualityCaseBasics caseBasics = dataQualityService.getCasesByGid(dataQualitiesRequest);
        assertThat(caseBasics.getDqCases().size(), is(1));
        DataQualityCaseBasic caseBasic = caseBasics.getDqCases().get(0);
        assertThat(caseBasic.getId(), is(spec.getExpectationId()));
        assertThat(caseBasic.getName(), is(spec.getName()));
        assertThat(caseBasic.getUpdater(), is(spec.getUpdateUser()));
        assertThat(caseBasic.getTaskId(), is(spec.getTaskId()));
    }

    @Test
    public void testDeleteExpectation() {
        ExpectationSpec spec = MockExpectationSpecFactory.create();
        expectationDao.create(spec);
        expectationDao.createRelatedDataset(spec.getExpectationId(), ImmutableList.of(spec.getDataset().getGid()));

        dataQualityService.deleteExpectation(spec.getExpectationId());

        ExpectationSpec specOfFetched = expectationDao.fetchById(spec.getExpectationId());
        assertThat(specOfFetched, nullValue());
    }

    @Test
    public void testValidateSQL() {
        String database = "test_db";
        String table = "test_table";
        String field = "id";
        String type = "AWS";
        DatasetBasic datasetBasic = MockDatasetBasicFactory.create(database, table, type);

        Long datasetId = IdGenerator.getInstance().nextId();
        String sqlText = String.format("select %s from %s.%s", field, database, table);
        DataQualityRule dataQualityRule = MockDataQualityRuleFactory.create(field, "=", "number", "1", "1");
        ValidateSqlRequest vsr = MockValidateSqlRequestFactory.create(datasetId, sqlText, ImmutableList.of(dataQualityRule));
        doReturn(datasetBasic).when(datasetRepository).findBasic(vsr.getDatasetId());

        ValidateSqlResult validateSqlResult = dataQualityService.validateSql(vsr);
        assertThat(validateSqlResult.getValidateStatus(), is(0));
    }

}
