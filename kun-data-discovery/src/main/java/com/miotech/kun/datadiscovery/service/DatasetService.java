package com.miotech.kun.datadiscovery.service;

import com.miotech.kun.datadiscovery.model.entity.LineageDatasetBasic;
import com.miotech.kun.datadiscovery.persistence.DatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Service("DataDiscoveryDatasetService")
public class DatasetService {

    @Autowired
    DatasetRepository datasetRepository;

    public List<LineageDatasetBasic> getDatasets(List<Long> datasetGids) {
        return datasetRepository.getDatasets(datasetGids);
    }

}
