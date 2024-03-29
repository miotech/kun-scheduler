package com.miotech.kun.dataquality.web.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ExpectationTemplate;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.common.dao.ExpectationRunDao;
import com.miotech.kun.dataquality.web.common.service.ExpectationTemplateService;
import com.miotech.kun.dataquality.web.model.bo.ExpectationRequest;
import com.miotech.kun.dataquality.web.model.bo.ExpectationsRequest;
import com.miotech.kun.dataquality.web.model.bo.MetricsRequest;
import com.miotech.kun.dataquality.web.model.bo.ValidateMetricsRequest;
import com.miotech.kun.dataquality.web.model.entity.*;
import com.miotech.kun.dataquality.web.persistence.DataQualityRepository;
import com.miotech.kun.dataquality.web.persistence.DatasetRepository;
import com.miotech.kun.dataquality.web.utils.Constants;
import com.miotech.kun.dataquality.web.utils.SQLParser;
import com.miotech.kun.dataquality.web.utils.SQLValidator;
import com.miotech.kun.operationrecord.common.anno.OperationRecord;
import com.miotech.kun.operationrecord.common.model.OperationRecordType;
import com.miotech.kun.security.service.BaseSecurityService;
import com.miotech.kun.workflow.core.model.task.CheckType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/16
 */
@Service
@Slf4j
public class DataQualityService extends BaseSecurityService {

    @Autowired
    DataQualityRepository dataQualityRepository;

    @Autowired
    DatasetRepository datasetRepository;

    @Autowired
    private ExpectationDao expectationDao;

    @Autowired
    private ExpectationRunDao expectationRunDao;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private SQLParser sqlParser;

    @Autowired
    private SQLValidator sqlValidator;

    @Autowired
    private ExpectationTemplateService expectationTemplateService;

    public List<DataQualityHistoryRecords> getHistory(List<Long> expectationIds, int limit) {
        if (CollectionUtils.isEmpty(expectationIds)) {
            return Lists.newArrayList();
        }

        return expectationDao.getHistoryOfTheLastNTimes(expectationIds, limit);
    }

    public ValidateMetricsResult validateSql(ValidateMetricsRequest validateMetricsRequest) {
        try {
            MetricsRequest metricsRequest = validateMetricsRequest.getMetrics();
            String sql = (String) metricsRequest.getPayload().get("sql");
            DatasetBasic selectedDataset = datasetRepository.findBasic(metricsRequest.getDatasetGid());
            String druidType = Constants.DATASOURCE_TO_DRUID_TYPE.get(selectedDataset.getDatasourceType());
            if (StringUtils.isEmpty(druidType)) {
                return ValidateMetricsResult.failed("Not supported data source.");
            }

            SQLParseResult sqlParseResult = sqlParser.parseQuerySQL(sql.trim().toLowerCase(), druidType);
            ValidateMetricsResult validateMetricsResult = sqlValidator.validate(sqlParseResult, validateMetricsRequest);
            if (!validateMetricsResult.isSuccess()) {
                return validateMetricsResult;
            }

            List<DatasetBasic> relatedDatasets = parseRelatedDatasets(sqlParseResult.getRelatedDatasetNames(), selectedDataset);
            return ValidateMetricsResult.success(relatedDatasets);
        } catch (Exception e) {
            return ValidateMetricsResult.failed(e.getMessage());
        }
    }

    public ExpectationVO getExpectation(Long id) {
        Expectation expectation = expectationDao.fetchById(id);
        List<DatasetBasic> relatedDatasets = expectationDao.getRelatedDatasets(id);
        return ExpectationVO.newBuilder()
                .withId(expectation.getExpectationId())
                .withName(expectation.getName())
                .withDescription(expectation.getDescription())
                .withTypes(expectation.getTypes())
                .withGranularity(expectation.getGranularity())
                .withTemplateName(expectation.getTemplate().getName())
                .withPayload(expectation.getPayload())
                .withDatasetGid(expectation.getDataset().getGid())
                .withRelatedTables(relatedDatasets)
                .withCaseType(expectation.getCaseType())
                .build();
    }

    public ExpectationBasics getExpectationBasics(ExpectationsRequest request) {
        return expectationDao.getExpectationBasics(request);
    }

    public List<Long> getAllCaseId() {
        return dataQualityRepository.getAllCaseId();
    }

    public List<Long> getAllTaskId() {
        return dataQualityRepository.getAllTaskId();
    }

    public void logDataQualityCaseResults(List<DataQualityCaseResult> results) {
        dataQualityRepository.logDataQualityCaseResults(results);
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.EXPECTATION_CREATE, args = {"#expectationRequest"})
    public Long createExpectation(ExpectationRequest expectationRequest) {
        String currentUsername = getCurrentUsername();
        Long dataSourceId = datasetRepository.findDataSourceIdByGid(expectationRequest.getDatasetGid());

        if(expectationRequest.getCaseType() == null){
            expectationRequest.setCaseType(CaseType.SKIP);
        }
        Expectation expectation = expectationRequest.convertTo(dataSourceId, currentUsername);
        expectationDao.create(expectation);
        updateRelatedDataset(expectation.getExpectationId(), expectationRequest.getRelatedDatasetGids());

        Long taskId = workflowService.executeExpectation(expectation.getExpectationId()).getTask().getId();
        expectationDao.updateTaskId(expectation.getExpectationId(), taskId);

        CheckType checkType = expectationRequest.getCheckType();
        workflowService.updateUpstreamTaskCheckType(expectationRequest.getDatasetGid(), checkType);
        return expectation.getExpectationId();
    }

    @OperationRecord(type = OperationRecordType.EXPECTATION_DELETE, args = {"#id"})
    public void deleteExpectation(Long id) {
        workflowService.deleteTaskByCase(id);
        expectationDao.deleteById(id);
        expectationRunDao.deleteByExpectationId(id);
        expectationDao.deleteAllRelatedDataset(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationRecord(type = OperationRecordType.EXPECTATION_UPDATE, args = {"#id", "#expectationRequest"})
    public void updateExpectation(Long id, ExpectationRequest expectationRequest) {
        String currentUsername = getCurrentUsername();
        Long dataSourceId = datasetRepository.findDataSourceIdByGid(expectationRequest.getDatasetGid());

        if(expectationRequest.getCaseType() == null){
            expectationRequest.setCaseType(CaseType.SKIP);
        }
        Expectation expectation = expectationRequest.convertTo(dataSourceId, currentUsername);
        expectationDao.updateById(id, expectation);
        updateRelatedDataset(id, expectationRequest.getRelatedDatasetGids());

        CheckType checkType = expectationRequest.getCheckType();
        workflowService.updateUpstreamTaskCheckType(expectationRequest.getDatasetGid(), checkType);

        runExpectation(id);
    }

    public List<ExpectationTemplate> getDisplayConfig(String granularity) {
        return expectationTemplateService.fetchByGranularity(granularity);
    }

    public void runExpectation(Long id) {
        Preconditions.checkNotNull(id);
        workflowService.executeExpectation(id);
    }

    public List<Long> getWorkflowTasksByDatasetId(Long datasetId){
        return dataQualityRepository.getWorkflowTasksByDatasetId(datasetId);
    }

    public void insertCaseRunWithTaskRun(List<CaseRun> caseRunList){
        dataQualityRepository.insertCaseRunWithTaskRun(caseRunList);
    }

    public void updateCaseRunStatus(CaseRun caseRun){
        dataQualityRepository.updateCaseRunStatus(caseRun.getCaseRunId(),caseRun.getStatus());
    }

    public CaseRun fetchCaseRunByCaseRunId(long caseRunId) {
        return dataQualityRepository.fetchCaseRunByCaseRunId(caseRunId);
    }

    private void updateRelatedDataset(Long id, List<Long> datasetIds) {
        expectationDao.deleteAllRelatedDataset(id);
        expectationDao.createRelatedDataset(id, datasetIds);
    }

    private List<DatasetBasic> parseRelatedDatasets(List<String> relatedDatasetNames, DatasetBasic selectedDataset) {
        List<DatasetBasic> relatedDatasets = Lists.newArrayList();
        for (String relatedDatasetName : relatedDatasetNames) {
            String[] tableArray = relatedDatasetName.split("\\.");
            String dbName = tableArray.length == 1 ? selectedDataset.getDatabase() : tableArray[0];
            dbName = dbName.replaceAll("\"", "");

            String tableName = tableArray[tableArray.length - 1]
                    .replaceAll("\"", "");

            if (dbName.equalsIgnoreCase(selectedDataset.getDatabase())
                    && tableName.equalsIgnoreCase(selectedDataset.getName())) {
                selectedDataset.setIsPrimary(true);
            } else {
                DatasetBasic datasetBasic = new DatasetBasic();
                datasetBasic.setName(tableName);
                datasetBasic.setDatabase(dbName);
                datasetBasic.setDatasource(selectedDataset.getDatasource());
                datasetBasic.setDatasourceType(selectedDataset.getDatasourceType());
                datasetBasic.setIsPrimary(false);

                DatasetBasic basic = datasetRepository.findDatasetId(datasetBasic);
                relatedDatasets.add(basic);
            }
        }

        relatedDatasets.add(selectedDataset);
        return relatedDatasets;
    }

}
