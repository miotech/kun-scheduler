package com.miotech.kun.datadiscover.service;

import com.miotech.kun.datadiscover.model.bo.DatasetRequest;
import com.miotech.kun.datadiscover.model.bo.DatasetSearchRequest;
import com.miotech.kun.datadiscover.model.entity.Dataset;
import com.miotech.kun.datadiscover.model.entity.DatasetBasicPage;
import com.miotech.kun.datadiscover.persistence.DatasetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: JieChen
 * @created: 6/12/20
 */
@Service
public class DatasetService {

    @Autowired
    DatasetRepository datasetRepository;

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
