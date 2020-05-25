package com.miotech.kun.datadiscover.service;

import com.miotech.kun.datadiscover.model.bo.DatasetColumnRequest;
import com.miotech.kun.datadiscover.model.entity.DatasetColumn;
import com.miotech.kun.datadiscover.persistence.DatasetFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: JieChen
 * @created: 6/12/20
 */
@Service
public class DatasetFieldService {

    @Autowired
    DatasetFieldRepository datasetFieldRepository;

    public List<DatasetColumn> find(Long datasetGid) {
        return datasetFieldRepository.findByDatasetGid(datasetGid);
    }

    public DatasetColumn update(Long id, DatasetColumnRequest datasetColumnRequest) {
        return datasetFieldRepository.update(id, datasetColumnRequest);
    }
}
