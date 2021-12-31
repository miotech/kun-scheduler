package com.miotech.kun.dataquality.web.service;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.query.JDBCQuery;
import com.miotech.kun.commons.query.JDBCQueryExecutor;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.core.ExpectationMethod;
import com.miotech.kun.dataquality.core.ExpectationSpec;
import com.miotech.kun.dataquality.core.JDBCExpectationAssertion;
import com.miotech.kun.dataquality.core.JDBCExpectationMethod;
import com.miotech.kun.dataquality.web.common.dao.ExpectationDao;
import com.miotech.kun.dataquality.web.model.TemplateType;
import com.miotech.kun.dataquality.web.model.bo.DataQualitiesRequest;
import com.miotech.kun.dataquality.web.model.bo.ExpectationBO;
import com.miotech.kun.dataquality.web.model.bo.ValidateSqlRequest;
import com.miotech.kun.dataquality.web.model.entity.*;
import com.miotech.kun.dataquality.web.persistence.DataQualityRepository;
import com.miotech.kun.dataquality.web.persistence.DatasetRepository;
import com.miotech.kun.dataquality.web.utils.Constants;
import com.miotech.kun.security.service.BaseSecurityService;
import com.miotech.kun.workflow.core.model.task.CheckType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private WorkflowService workflowService;

    public List<DataQualityHistoryRecords> getHistory(List<Long> expectationIds, int limit) {
        if (CollectionUtils.isEmpty(expectationIds)) {
            return Lists.newArrayList();
        }

        return expectationDao.getHistoryOfTheLastNTimes(expectationIds, limit);
    }

    public ValidateSqlResult validateSql(ValidateSqlRequest request) {
        List<DatasetBasic> relatedDatasets = Lists.newArrayList();
        try {
            DatasetBasic selectedDataset = datasetRepository.findBasic(request.getDatasetId());
            String druidType = Constants.DATASOURCE_TO_DRUID_TYPE.get(selectedDataset.getDatasourceType());
            if (StringUtils.isEmpty(druidType)) {
                return ValidateSqlResult.failed("Not supported data source.");
            }

            List<String> relatedDatasetNames = this.parseQuerySQL(request.getSqlText().trim().toLowerCase(), druidType);

            boolean validateSelectedDataset = false;
            for (String relatedDatasetName : relatedDatasetNames) {
                String[] tableArray = relatedDatasetName.split("\\.");
                String dbName = tableArray.length == 1 ? selectedDataset.getDatabase() : tableArray[0];
                dbName = dbName.replaceAll("\"", "");

                String tableName = tableArray[tableArray.length - 1]
                        .replaceAll("\"", "");

                if (!validateSelectedDataset
                        && dbName.equalsIgnoreCase(selectedDataset.getDatabase())
                        && tableName.equalsIgnoreCase(selectedDataset.getName())) {
                    validateSelectedDataset = true;
                } else {
                    DatasetBasic datasetBasic = new DatasetBasic();
                    datasetBasic.setName(tableName);
                    datasetBasic.setDatabase(dbName);
                    datasetBasic.setDatasource(selectedDataset.getDatasource());
                    datasetBasic.setDatasourceType(selectedDataset.getDatasourceType());
                    datasetBasic.setIsPrimary(false);
                    relatedDatasets.add(datasetRepository.findDatasetId(datasetBasic));
                }
            }
            if (!validateSelectedDataset) {
                return ValidateSqlResult.failed("Not related to current dataset.");
            }
            selectedDataset.setIsPrimary(true);
            relatedDatasets.add(0, selectedDataset);

            JDBCQuery query = JDBCQuery.newBuilder()
                    .datasetId(request.getDatasetId())
                    .queryString(request.getSqlText())
                    .build();
            JDBCQueryExecutor.getInstance().execute(query);

        } catch (Exception e) {
            return ValidateSqlResult.failed(e.getMessage());
        }
        return ValidateSqlResult.success(relatedDatasets);
    }

    public List<String> parseQuerySQL(String querySql, String dbType) {
        List<SQLStatement> stmts = SQLUtils.parseStatements(querySql, dbType);
        SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(dbType);
        stmts.get(0).accept(statVisitor);

        Map<TableStat.Name, TableStat> tables = statVisitor.getTables();
        tables.forEach((name, tableStat) -> {
            if (tableStat.getCreateCount() > 0
                    || tableStat.getDropCount() > 0
                    || tableStat.getAlterCount() > 0
                    || tableStat.getInsertCount() > 0
                    || tableStat.getDeleteCount() > 0
                    || tableStat.getUpdateCount() > 0
                    || tableStat.getMergeCount() > 0
                    || tableStat.getCreateIndexCount() > 0
                    || tableStat.getDropIndexCount() > 0) {
                throw new RuntimeException("Only select query is supported.");
            }
            if (tableStat.getSelectCount() == 0) {
                throw new RuntimeException("No select query is specified.");
            }
        });
        return tables.keySet().stream().map(x -> x.getName()).collect(Collectors.toList());
    }

    public DimensionConfig getDimensionConfig(String dsType) {
        DimensionConfig dimensionConfig = new DimensionConfig();
        dimensionConfig.getDimensionConfigs().add(getCustomDimensionConfig());
        return dimensionConfig;
    }

    public DataQualityCase getCase(Long id) {
        ExpectationSpec expectationSpec = expectationDao.fetchById(id);
        List<DatasetBasic> relatedDatasets = expectationDao.getRelatedDatasets(id);
        DataQualityCase result = new DataQualityCase();
        result.setId(expectationSpec.getExpectationId());
        result.setName(expectationSpec.getName());
        result.setDescription(expectationSpec.getDescription());
        result.setDimension(TemplateType.CUSTOMIZE.name());

        JSONObject dimensionConfig = new JSONObject();
        dimensionConfig.put("sql", ((JDBCExpectationMethod) expectationSpec.getMethod()).getSql());
        result.setDimensionConfig(dimensionConfig);
        result.setValidateRules(convertFromExpectationMethod(expectationSpec.getMethod()));
        result.setRelatedTables(relatedDatasets);
        result.setTypes(expectationSpec.getTypes());
        result.setIsBlocking(expectationSpec.isBlocking());

        return result;
    }

    private List<DataQualityRule> convertFromExpectationMethod(ExpectationMethod method) {
        List<DataQualityRule> result = Lists.newArrayList();
        ExpectationMethod.Mode mode = method.getMode();
        switch (mode) {
            case JDBC:
                JDBCExpectationMethod jdbcExpectationMethod = (JDBCExpectationMethod) method;
                List<JDBCExpectationAssertion> assertions = jdbcExpectationMethod.getAssertions();
                for (JDBCExpectationAssertion assertion : assertions) {
                    DataQualityRule rule = new DataQualityRule();
                    rule.setField(assertion.getField());
                    rule.setOperator(assertion.getComparisonOperator().getSymbol());
                    rule.setExpectedValue(assertion.getExpectedValue());
                    rule.setExpectedType(assertion.getExpectedType());
                    result.add(rule);
                }
        }
        return result;
    }

    public DataQualityCaseBasic getCaseBasic(Long id) {
        return dataQualityRepository.getCaseBasic(id);
    }

    public DataQualityCaseBasics getCasesByGid(DataQualitiesRequest request) {
        return expectationDao.getExpectationBasic(request);
    }

    public void saveTaskId(Long caseId, Long taskId) {
        dataQualityRepository.saveTaskId(caseId, taskId);
    }

    public Long getLatestTaskId(Long caseId) {
        return dataQualityRepository.getLatestTaskId(caseId);
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
    public Long createExpectation(ExpectationBO expectationBO) {
        String currentUsername = getCurrentUsername();
        expectationBO.setCreateUser(currentUsername);
        expectationBO.setCreateTime(DateTimeUtils.now());
        expectationBO.setUpdateUser(currentUsername);
        expectationBO.setUpdateTime(expectationBO.getCreateTime());

        Long dataSourceId = datasetRepository.findDataSourceIdByGid(expectationBO.getDatasetGid());
        ExpectationSpec expectationSpec = expectationBO.convertTo(dataSourceId);
        expectationDao.create(expectationSpec);
        updateRelatedDataset(expectationSpec.getExpectationId(), expectationBO.getRelatedTableIds());

        Long taskId = workflowService.executeTask(expectationSpec.getExpectationId()).getTask().getId();
        expectationDao.updateTaskId(expectationSpec.getExpectationId(), taskId);

        CheckType checkType = expectationBO.getCheckType();
        workflowService.updateUpstreamTaskCheckType(expectationBO.getDatasetGid(), checkType);
        return expectationSpec.getExpectationId();
    }

    public void deleteExpectation(Long id) {
        workflowService.deleteTaskByCase(id);
        expectationDao.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateExpectation(Long id, ExpectationBO expectationBO) {
        String currentUsername = getCurrentUsername();
        expectationBO.setUpdateUser(currentUsername);
        expectationBO.setUpdateTime(DateTimeUtils.now());

        Long dataSourceId = datasetRepository.findDataSourceIdByGid(expectationBO.getDatasetGid());
        ExpectationSpec expectationSpec = expectationBO.convertTo(dataSourceId);
        expectationDao.updateById(id, expectationSpec);
        updateRelatedDataset(id, expectationBO.getRelatedTableIds());

        CheckType checkType = expectationBO.getCheckType();
        workflowService.updateUpstreamTaskCheckType(expectationBO.getDatasetGid(), checkType);
    }

    private void updateRelatedDataset(Long id, List<Long> datasetIds) {
        expectationDao.deleteAllRelatedDataset(id);
        expectationDao.createRelatedDataset(id, datasetIds);
    }

    private JSONObject getCustomDimensionConfig() {
        JSONObject customDimension = new JSONObject();
        customDimension.put("dimension", TemplateType.CUSTOMIZE);
        JSONArray customFields = new JSONArray();
        JSONObject sqlField = createCustomField("sql", 1, "SQL", true);
        customFields.add(sqlField);
        customDimension.put("fields", customFields);
        return customDimension;
    }

    private JSONObject createCustomField(String key,
                                         Integer order,
                                         String format,
                                         Boolean require) {
        JSONObject field = new JSONObject();
        field.put("key", key);
        field.put("order", order);
        field.put("format", format);
        field.put("require", require);
        return field;
    }
}
