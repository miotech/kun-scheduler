package com.miotech.kun.metadata.common.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.common.dao.DataSourceDao;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DataSourceRequest;
import com.miotech.kun.metadata.core.model.datasource.DataSourceSearchFilter;
import com.miotech.kun.metadata.core.model.datasource.DataSourceType;
import com.miotech.kun.metadata.core.model.vo.PaginationVO;

import java.time.OffsetDateTime;
import java.util.List;

@Singleton
public class DataSourceService {

    private DataSourceDao dataSourceDao;

    @Inject
    public DataSourceService(DataSourceDao dataSourceDao) {
        this.dataSourceDao = dataSourceDao;
    }

    public PaginationVO<DataSource> fetchDataSources(int pageNum, int pageSize, String name) {
        DataSourceSearchFilter filter = new DataSourceSearchFilter(name, pageNum, pageSize);
        return new PaginationVO<>(pageNum, pageNum,
                dataSourceDao.fetchTotalCountWithFilter(filter), dataSourceDao.fetchWithFilter(filter));
    }

    public DataSource create(DataSourceRequest dataSourceRequest) {
        DataSource dataSource = DataSource.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withName(dataSourceRequest.getName())
                .withConnectionInfo(dataSourceRequest.getConnectionInfo())
                .withTypeId(dataSourceRequest.getTypeId())
                .withTags(dataSourceRequest.getTags())
                .withCreateUser(dataSourceRequest.getCreateUser())
                .withCreateTime(OffsetDateTime.now())
                .withUpdateUser(dataSourceRequest.getUpdateUser())
                .withUpdateTime(OffsetDateTime.now())
                .build();
        dataSourceDao.create(dataSource);
        return dataSource;
    }

    public DataSource update(Long id, DataSourceRequest dataSourceRequest) {
        DataSource dataSource = DataSource.newBuilder()
                .withId(id)
                .withName(dataSourceRequest.getName())
                .withConnectionInfo(dataSourceRequest.getConnectionInfo())
                .withTypeId(dataSourceRequest.getTypeId())
                .withTags(dataSourceRequest.getTags())
                .withUpdateUser(dataSourceRequest.getUpdateUser())
                .withUpdateTime(OffsetDateTime.now())
                .build();
        dataSourceDao.update(dataSource);
        return dataSourceDao.findById(id).get();
    }

    public void delete(Long id) {
        dataSourceDao.delete(id);
    }

    public List<DataSourceType> getAllTypes() {
        return dataSourceDao.getAllTypes();
    }
}
