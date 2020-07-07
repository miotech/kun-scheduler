package com.miotech.kun.datadiscovery.service;

import com.miotech.kun.datadiscovery.model.bo.DatasetFieldRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetFieldSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.DatasetField;
import com.miotech.kun.datadiscovery.model.entity.DatasetFieldPage;
import com.miotech.kun.datadiscovery.persistence.DatasetFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Service
public class DatasetFieldService {

    @Autowired
    DatasetFieldRepository datasetFieldRepository;

    public DatasetFieldPage find(Long datasetGid, DatasetFieldSearchRequest searchRequest) {
        return datasetFieldRepository.findByDatasetGid(datasetGid, searchRequest);
    }

    public DatasetField update(Long id, DatasetFieldRequest datasetFieldRequest) {
        return datasetFieldRepository.update(id, datasetFieldRequest);
    }
}
