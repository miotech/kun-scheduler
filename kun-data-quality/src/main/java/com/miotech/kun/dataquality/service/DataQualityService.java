package com.miotech.kun.dataquality.service;

import com.miotech.kun.dataquality.model.bo.DataQualityRequest;
import com.miotech.kun.dataquality.model.entity.DataQualityCase;
import com.miotech.kun.dataquality.model.entity.DataQualityCaseBasic;
import com.miotech.kun.dataquality.model.entity.DimensionConfig;
import com.miotech.kun.dataquality.persistence.DataQualityRepository;
import com.miotech.kun.security.service.BaseSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Jie Chen
 * @created: 2020/7/16
 */
@Service
public class DataQualityService extends BaseSecurityService {

    @Autowired
    DataQualityRepository dataQualityRepository;

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

    public Long deleteCase(Long id, Long datasetId) {
        return dataQualityRepository.deleteCase(id, datasetId);
    }

    public void saveTaskId(Long caseId, Long taskId) {
        dataQualityRepository.saveTaskId(caseId, taskId);
    }

    public Long getLatestTaskId(Long caseId) {
        return dataQualityRepository.getLatestTaskId(caseId);
    }
}
