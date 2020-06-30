package com.miotech.kun.datadiscover.service;

import com.miotech.kun.datadiscover.model.bo.DatasetFieldRequest;
import com.miotech.kun.datadiscover.model.bo.DatasetFieldSearchRequest;
import com.miotech.kun.datadiscover.model.entity.DatasetField;
import com.miotech.kun.datadiscover.model.entity.DatasetFieldPage;
import com.miotech.kun.datadiscover.persistence.DatasetFieldRepository;
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
