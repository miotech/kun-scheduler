package com.miotech.kun.datadiscover.service;

import com.miotech.kun.datadiscover.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscover.model.bo.DatabaseRequest;
import com.miotech.kun.datadiscover.model.bo.DatabaseSearchRequest;
import com.miotech.kun.datadiscover.model.entity.Datasource;
import com.miotech.kun.datadiscover.model.entity.DatasourceBasicPage;
import com.miotech.kun.datadiscover.model.entity.DatasourcePage;
import com.miotech.kun.datadiscover.model.entity.DatasourceType;
import com.miotech.kun.datadiscover.persistence.DatasourceRepository;
import com.miotech.kun.security.service.BaseSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Service
public class DatasourceService extends BaseSecurityService {

    @Autowired
    DatasourceRepository datasourceRepository;

    public DatasourceBasicPage search(BasicSearchRequest basicSearchRequest) {
        return datasourceRepository.search(basicSearchRequest);
    }

    public DatasourcePage search(DatabaseSearchRequest databaseSearchRequest) {
        return datasourceRepository.search(databaseSearchRequest);
    }

    public List<DatasourceType> getAllTypes() {
        return datasourceRepository.getAllTypes();
    }

    public Datasource add(DatabaseRequest databaseRequest) throws SQLException {
        long currentTime = System.currentTimeMillis();
        databaseRequest.setCreateUser(getCurrentUser());
        databaseRequest.setCreateTime(currentTime);
        databaseRequest.setUpdateUser(getCurrentUser());
        databaseRequest.setUpdateTime(currentTime);
        return datasourceRepository.insert(databaseRequest);
    }

    public Datasource update(Long id, DatabaseRequest databaseRequest) throws SQLException {
        databaseRequest.setUpdateUser(getCurrentUser());
        databaseRequest.setUpdateTime(System.currentTimeMillis());
        return datasourceRepository.update(id, databaseRequest);
    }

    public void delete(Long id) {
        datasourceRepository.delete(id);
    }
}
