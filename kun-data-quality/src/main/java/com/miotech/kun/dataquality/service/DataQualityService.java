package com.miotech.kun.dataquality.service;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.query.JDBCQuery;
import com.miotech.kun.commons.query.JDBCQueryExecutor;
import com.miotech.kun.dataquality.model.bo.*;
import com.miotech.kun.dataquality.model.entity.*;
import com.miotech.kun.dataquality.persistence.DataQualityRepository;
import com.miotech.kun.dataquality.persistence.DatasetRepository;
import com.miotech.kun.dataquality.utils.Constants;
import com.miotech.kun.security.service.BaseSecurityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<DataQualityHistoryRecords> getHistory(DataQualityHistoryRequest request) {
        return dataQualityRepository.getHistory(request);
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

                if(!validateSelectedDataset
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
            if(!validateSelectedDataset) {
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
        return dataQualityRepository.getDimensionConfig(dsType);
    }

    public Long addCase(DataQualityRequest dataQualityRequest) {
        Long currentTime = System.currentTimeMillis();
        dataQualityRequest.setCreateUser(getCurrentUsername());
        dataQualityRequest.setCreateTime(currentTime);
        dataQualityRequest.setUpdateUser(getCurrentUsername());
        dataQualityRequest.setUpdateTime(currentTime);
        return dataQualityRepository.addCase(dataQualityRequest);
    }

    public DataQualityCase getCase(Long id) {
        return dataQualityRepository.getCase(id);
    }

    public DataQualityCaseBasic getCaseBasic(Long id) {
        return dataQualityRepository.getCaseBasic(id);
    }

    public DataQualityCaseBasics getCasesByGid(DataQualitiesRequest request) {
        return dataQualityRepository.getCaseBasics(request);
    }

    public Long updateCase(Long id, DataQualityRequest dataQualityRequest) {
        dataQualityRequest.setUpdateUser(getCurrentUsername());
        dataQualityRequest.setUpdateTime(System.currentTimeMillis());
        return dataQualityRepository.updateCase(id, dataQualityRequest);
    }

    public DeleteCaseResponse deleteCase(Long id) {
        return dataQualityRepository.deleteCase(id);
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
}
