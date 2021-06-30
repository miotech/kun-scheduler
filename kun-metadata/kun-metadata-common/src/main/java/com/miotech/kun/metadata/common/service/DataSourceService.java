package com.miotech.kun.metadata.common.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.common.dao.DataSourceDao;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Singleton
public class DataSourceService {

    private DataSourceDao dataSourceDao;

    @Inject
    public DataSourceService(DataSourceDao dataSourceDao) {
        this.dataSourceDao = dataSourceDao;
    }

    public List<Long> fetchDataSourceIdByType(String typeName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(typeName), "typeName should not be empty");
        return dataSourceDao.fetchDataSourceIdByType(typeName);
    }

}
