package com.miotech.kun.datadiscovery.service;

import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DataSourceRequest;
import com.miotech.kun.datadiscovery.model.bo.DataSourceSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.DataSource;
import com.miotech.kun.datadiscovery.model.entity.DataSourceBasicPage;
import com.miotech.kun.datadiscovery.model.entity.DataSourcePage;
import com.miotech.kun.datadiscovery.model.entity.DataSourceType;
import com.miotech.kun.datadiscovery.persistence.DataSourceRepository;
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
public class DataSourceService extends BaseSecurityService {

    @Autowired
    DataSourceRepository datasourceRepository;

    public DataSourceBasicPage search(BasicSearchRequest basicSearchRequest) {
        return datasourceRepository.search(basicSearchRequest);
    }

    public DataSourcePage search(DataSourceSearchRequest datasourceSearchRequest) {
        return datasourceRepository.search(datasourceSearchRequest);
    }

    public List<DataSourceType> getAllTypes() {
        return datasourceRepository.getAllTypes();
    }

    public DataSource add(DataSourceRequest dataSourceRequest) throws SQLException {
        long currentTime = System.currentTimeMillis();
        dataSourceRequest.setCreateUser(getCurrentUsername());
        dataSourceRequest.setCreateTime(currentTime);
        dataSourceRequest.setUpdateUser(getCurrentUsername());
        dataSourceRequest.setUpdateTime(currentTime);
        return datasourceRepository.insert(dataSourceRequest);
    }

    public DataSource update(Long id, DataSourceRequest dataSourceRequest) throws SQLException {
        dataSourceRequest.setUpdateUser(getCurrentUsername());
        dataSourceRequest.setUpdateTime(System.currentTimeMillis());
        return datasourceRepository.update(id, dataSourceRequest);
    }

    public void delete(Long id) {
        datasourceRepository.delete(id);
    }
}
