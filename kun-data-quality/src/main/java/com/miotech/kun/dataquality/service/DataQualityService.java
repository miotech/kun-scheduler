package com.miotech.kun.dataquality.service;

import com.miotech.kun.commons.query.JDBCQuery;
import com.miotech.kun.commons.query.JDBCQueryExecutor;
import com.miotech.kun.dataquality.model.bo.DataQualityRequest;
import com.miotech.kun.dataquality.model.bo.DeleteCaseResponse;
import com.miotech.kun.dataquality.model.bo.ValidateSqlRequest;
import com.miotech.kun.dataquality.model.entity.DataQualityCase;
import com.miotech.kun.dataquality.model.entity.DataQualityCaseBasic;
import com.miotech.kun.dataquality.model.entity.DimensionConfig;
import com.miotech.kun.dataquality.model.entity.ValidateSqlResult;
import com.miotech.kun.dataquality.persistence.DataQualityRepository;
import com.miotech.kun.security.service.BaseSecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public ValidateSqlResult validateSql(ValidateSqlRequest request) {
        JDBCQuery query = JDBCQuery.newBuilder()
                .datasetId(request.getDatasetId())
                .queryString(request.getSqlText())
                .build();

        try {
            JDBCQueryExecutor.getInstance().execute(query);
        } catch (Exception e) {
            log.error("Validate sql failed.", e);
            return ValidateSqlResult.failed();
        }
        return ValidateSqlResult.success();
    }

    public DimensionConfig getDimensionConfig(String dsType) {
        return dataQualityRepository.getDimensionConfig(dsType);
    }

    public Long addCase(DataQualityRequest dataQualityRequest) {
        Long currentTime = System.currentTimeMillis();
        dataQualityRequest.setCreateUser(getCurrentUser());
        dataQualityRequest.setCreateTime(currentTime);
        dataQualityRequest.setUpdateUser(getCurrentUser());
        dataQualityRequest.setUpdateTime(currentTime);
        return dataQualityRepository.addCase(dataQualityRequest);
    }

    public DataQualityCase getCase(Long id) {
        return dataQualityRepository.getCase(id);
    }

    public DataQualityCaseBasic getCaseBasic(Long id) {
        return dataQualityRepository.getCaseBasic(id);
    }

    public Long updateCase(Long id, DataQualityRequest dataQualityRequest) {
        dataQualityRequest.setUpdateUser(getCurrentUser());
        dataQualityRequest.setUpdateTime(System.currentTimeMillis());
        return dataQualityRepository.updateCase(id, dataQualityRequest);
    }

    public boolean isFullDelete(Long id) {
        return dataQualityRepository.isFullDelete(id);
    }

    public DeleteCaseResponse deleteCase(Long id, Long datasetId) {
        return dataQualityRepository.deleteCase(id, datasetId);
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
}
