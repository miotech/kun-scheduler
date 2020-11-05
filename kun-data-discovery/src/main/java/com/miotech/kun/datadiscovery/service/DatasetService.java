package com.miotech.kun.datadiscovery.service;

import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.persistence.DatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Service
public class DatasetService {

    @Autowired
    DatasetRepository datasetRepository;

    public List<LineageDatasetBasic> getDatasets(List<Long> datasetGids) {
        return datasetRepository.getDatasets(datasetGids);
    }

    public List<Database> getAllDatabase() {
        return datasetRepository.getAllDatabase();
    }

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
