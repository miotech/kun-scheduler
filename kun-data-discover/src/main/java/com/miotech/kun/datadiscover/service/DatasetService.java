package com.miotech.kun.datadiscover.service;

import com.miotech.kun.datadiscover.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscover.model.bo.DatasetRequest;
import com.miotech.kun.datadiscover.model.bo.DatasetSearchRequest;
import com.miotech.kun.datadiscover.model.entity.Dataset;
import com.miotech.kun.datadiscover.model.entity.DatasetBasicPage;
import com.miotech.kun.datadiscover.persistence.DatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Service
public class DatasetService {

    @Autowired
    DatasetRepository datasetRepository;

    public DatasetBasicPage search(BasicSearchRequest basicSearchRequest) {
        return datasetRepository.search(basicSearchRequest);
    }

    public DatasetBasicPage search(DatasetSearchRequest datasetSearchRequest) {
        return datasetRepository.search(datasetSearchRequest);
    }

    public Dataset find(Long id) {
        return datasetRepository.find(id);
    }

    public Dataset update(Long id, DatasetRequest datasetRequest) {
        return datasetRepository.update(id, datasetRequest);
    }
}
