package com.miotech.kun.metadata.common.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.common.dao.DataSourceDao;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.metadata.core.model.dataset.DataStoreType;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
import com.miotech.kun.metadata.core.model.vo.DataSourceSearchFilter;
import com.miotech.kun.metadata.core.model.vo.DatasourceTemplate;
import com.miotech.kun.metadata.core.model.vo.PaginationVO;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

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

    public PaginationVO<DataSource> fetchDataSources(int pageNum, int pageSize, String name) {
        DataSourceSearchFilter filter = new DataSourceSearchFilter(name, pageNum, pageSize);
        return new PaginationVO<>(pageNum, pageNum,
                dataSourceDao.fetchTotalCountWithFilter(filter), dataSourceDao.fetchWithFilter(filter));
    }

    public boolean isDatasourceExist(DataSource datasource) {
        return dataSourceDao.isDatasourceExist(datasource);
    }

    public Long getDataSourceIdByConnectionInfo(DataStoreType storeType, ConnectionInfo connectionInfo) {
        DatasourceType sourceType = covertStoreTypeToSourceType(storeType);
        DataSource dataSource = dataSourceDao.fetchDataSourceByConnectionInfo(sourceType, connectionInfo);
        if (dataSource != null) {
            return dataSource.getId();
        }
        return null;
    }

    public Optional<DataSource> getDatasourceById(Long datasourceId) {
        return dataSourceDao.findById(datasourceId);
    }

    private DatasourceType covertStoreTypeToSourceType(DataStoreType storeType) {
        DatasourceType sourceType;
        switch (storeType) {
            case POSTGRES_TABLE:
                sourceType = DatasourceType.POSTGRESQL;
                break;
            case MONGO_COLLECTION:
                sourceType = DatasourceType.MONGODB;
                break;
            case ELASTICSEARCH_INDEX:
                sourceType = DatasourceType.ELASTICSEARCH;
                break;
            case ARANGO_COLLECTION:
                sourceType = DatasourceType.ARANGO;
                break;
            case HIVE_TABLE:
                sourceType = DatasourceType.HIVE;
                break;
            default:
                throw new IllegalStateException("not support storeType :" + storeType);
        }
        return sourceType;
    }

    public DataSource create(DataSourceRequest dataSourceRequest) {
        DataSource dataSource = DataSource.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withName(dataSourceRequest.getName())
                .withConnectionConfig(dataSourceRequest.getConnectionConfig())
                .withDatasourceType(dataSourceRequest.getDatasourceType())
                .withTags(dataSourceRequest.getTags())
                .withCreateUser(dataSourceRequest.getCreateUser())
                .withCreateTime(OffsetDateTime.now())
                .withUpdateUser(dataSourceRequest.getUpdateUser())
                .withUpdateTime(OffsetDateTime.now())
                .build();
        boolean isExist = isDatasourceExist(dataSource);
        if (isExist) {
            throw new IllegalArgumentException("datasource with type " + dataSource.getDatasourceType()
                    + " and connection info " + dataSource.getConnectionConfig() + " is exist");
        }
        dataSourceDao.create(dataSource);
        return dataSource;
    }

    public DataSource update(Long id, DataSourceRequest dataSourceRequest) {
        DataSource dataSource = DataSource.newBuilder()
                .withId(id)
                .withName(dataSourceRequest.getName())
                .withConnectionConfig(dataSourceRequest.getConnectionConfig())
                .withDatasourceType(dataSourceRequest.getDatasourceType())
                .withTags(dataSourceRequest.getTags())
                .withUpdateUser(dataSourceRequest.getUpdateUser())
                .withUpdateTime(OffsetDateTime.now())
                .build();
        dataSourceDao.update(dataSource);
        return dataSourceDao.findById(id).orElse(null);
    }

    public void delete(Long id) {
        dataSourceDao.delete(id);
    }

    public List<DatasourceTemplate> getAllTypes() {
        return dataSourceDao.getAllTypes();
    }

    public DataSource fetchDatasource(Long datasourceId){
        Optional<DataSource> dataSource = dataSourceDao.findById(datasourceId);
        if(dataSource.isPresent()){
            return dataSource.get();
        }
        return null;
    }
}
