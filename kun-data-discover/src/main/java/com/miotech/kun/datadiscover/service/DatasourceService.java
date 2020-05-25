package com.miotech.kun.datadiscover.service;

import com.miotech.kun.datadiscover.model.bo.DatabaseSearchRequest;
import com.miotech.kun.datadiscover.model.entity.Datasource;
import com.miotech.kun.datadiscover.model.entity.DatasourcePage;
import com.miotech.kun.datadiscover.model.entity.DatasourceType;
import com.miotech.kun.datadiscover.persistence.DatasourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: JieChen
 * @created: 6/12/20
 */
@Service
public class DatasourceService {

    @Autowired
    DatasourceRepository datasourceRepository;

    public DatasourcePage search(DatabaseSearchRequest databaseSearchRequest) {
        return datasourceRepository.search(databaseSearchRequest);
    }

    public List<DatasourceType> getAllTypes() {
        return datasourceRepository.getAllTypes();
    }

}
