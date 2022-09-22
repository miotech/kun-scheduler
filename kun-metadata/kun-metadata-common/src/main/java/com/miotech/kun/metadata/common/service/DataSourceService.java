package com.miotech.kun.metadata.common.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.common.dao.DataSourceDao;
import com.miotech.kun.metadata.common.utils.DatasourceDsiFormatter;
import com.miotech.kun.metadata.core.model.connection.*;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DataSourceBasicInfo;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import com.miotech.kun.metadata.core.model.vo.DataSourceBasicInfoRequest;
import com.miotech.kun.metadata.core.model.vo.DataSourceRequest;
import com.miotech.kun.metadata.core.model.vo.DataSourceSearchFilter;
import com.miotech.kun.metadata.core.model.vo.PaginationVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class DataSourceService {

    private final DataSourceDao dataSourceDao;
    private final ConnectionService connectionService;
    private final Logger logger = LoggerFactory.getLogger(DataSourceService.class);

    @Inject
    public DataSourceService(DataSourceDao dataSourceDao, ConnectionService connectionService) {
        this.dataSourceDao = dataSourceDao;
        this.connectionService = connectionService;
    }

    public List<Long> fetchDataSourceIdByType(String typeName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(typeName), "typeName should not be empty");
        return dataSourceDao.fetchDataSourceIdByType(typeName);
    }

    public PaginationVO<DataSource> fetchDataSources(int pageNum, int pageSize, String name) {
        DataSourceSearchFilter filter = new DataSourceSearchFilter(name, pageNum, pageSize);
        Integer count = this.fetchTotalCountWithFilter(filter);
        if (count <= 0) {
            return new PaginationVO<>(pageNum, pageNum, count, Lists.newArrayList());
        }
        List<DataSourceBasicInfo> dataSourceBasicInfoList = this.fetchWithFilter(filter);
        List<DataSource> collect = dataSourceBasicInfoList.stream().map(this::convertToDatasource).collect(Collectors.toList());
        return new PaginationVO<>(pageNum, pageNum, count, collect);
    }

    public List<DataSource> fetchDataSources() {
        List<DataSourceBasicInfo> dataSourceBasicInfoList = dataSourceDao.fetchList();
        return dataSourceBasicInfoList.stream().map(this::convertToDatasource).collect(Collectors.toList());
    }

    public List<DataSourceBasicInfo> fetchWithFilter(DataSourceSearchFilter filter) {
        List<DataSourceBasicInfo> dataSourceBasicInfoList = dataSourceDao.fetchWithFilter(filter);
        if (CollectionUtils.isEmpty(dataSourceBasicInfoList)) {
            return Lists.newArrayList();
        }
        return dataSourceBasicInfoList;
    }

    public Integer fetchTotalCountWithFilter(DataSourceSearchFilter filter) {
        return dataSourceDao.fetchTotalCountWithFilter(filter);
    }

    private DataSource convertToDatasource(@Nullable DataSourceBasicInfo dataSourceBasicInfo) {
        if (Objects.isNull(dataSourceBasicInfo)) {
            return null;
        }
        Optional<DatasourceConnection> datasourceConnection = connectionService.fetchDatasourceConnection(dataSourceBasicInfo.getId());
        if (!datasourceConnection.isPresent()) {
            logger.error("No connection available for data source:{}", dataSourceBasicInfo.getId());
            throw new IllegalStateException(String.format("No connection available for data source:%s", dataSourceBasicInfo.getId()));
        }
        return DataSource
                .newBuilder()
                .withId(dataSourceBasicInfo.getId())
                .withName(Objects.requireNonNull(dataSourceBasicInfo).getName())
                .withDatasourceConfigInfo(dataSourceBasicInfo.getDatasourceConfigInfo())
                .withDatasourceType(dataSourceBasicInfo.getDatasourceType())
                .withTags(dataSourceBasicInfo.getTags())
                .withCreateUser(dataSourceBasicInfo.getCreateUser())
                .withCreateTime(dataSourceBasicInfo.getCreateTime())
                .withUpdateUser(dataSourceBasicInfo.getUpdateUser())
                .withUpdateTime(dataSourceBasicInfo.getUpdateTime())
                .withDatasourceConnection(datasourceConnection.get())
                .build();
    }

    public boolean isDatasourceExist(DataSourceBasicInfo dataSourceBasicInfo) {
        return dataSourceDao.isDatasourceExistByDsi(dataSourceBasicInfo);
    }

    public Long fetchDataSourceByConnectionInfo(DatasourceType datasourceType, ConnectionConfigInfo connectionConfigInfo) {
        // TODO: Unable to determine the uniqueness of the hive table datasource!!!!
        List<ConnectionInfo> dataConnectionList = connectionService.fetchConnScopeConnection(ConnScope.DATA_CONN);
        if (DatasourceType.HIVE.equals(datasourceType)) {
            Optional<ConnectionInfo> firstHiveServer = dataConnectionList.stream()
                    .filter(connectionInfo -> connectionInfo.getConnectionType().equals(ConnectionType.HIVE_SERVER))
                    .findFirst();
            if (firstHiveServer.isPresent()) {
                return firstHiveServer.get().getDatasourceId();
            }
            Optional<ConnectionInfo> firstAthena = dataConnectionList.stream()
                    .filter(connectionInfo -> connectionInfo.getConnectionType().equals(ConnectionType.ATHENA))
                    .findFirst();
            if (firstAthena.isPresent()) {
                return firstAthena.get().getDatasourceId();
            }
        }
        Set<Long> datasourceIds = connectionService.fetchConnScopeConnection(ConnScope.DATA_CONN).stream()
                .filter(connectionInfo -> connectionInfo.getConnectionConfigInfo().sameDatasource(connectionConfigInfo))
                .map(ConnectionInfo::getDatasourceId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(datasourceIds)) {
            return null;
        }
        if (datasourceIds.size() > 1) {
            logger.error("Different data sources but the same configuration,ids:{}", datasourceIds);
        }
        return datasourceIds.stream().findFirst().orElse(null);
    }

    public Optional<DataSource> getDatasourceById(Long datasourceId) {
        return fetchDatasource(datasourceId);

    }

    public DataSource create(DataSourceRequest dataSourceRequest) {
        Optional<DataSourceBasicInfo> dataSourceBasicInfoOpt = checkName(dataSourceRequest.getDatasourceType(), dataSourceRequest.getName());
        if (dataSourceBasicInfoOpt.isPresent()) {
            throw new IllegalArgumentException("datasource with type " + dataSourceBasicInfoOpt.get().getDatasourceType()
                    + " and name " + dataSourceBasicInfoOpt.get().getName() + " is exist");
        }
        OffsetDateTime now = OffsetDateTime.now();
        DataSourceBasicInfo dataSourceBasicInfo = DataSourceBasicInfo.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withName(dataSourceRequest.getName())
                .withDatasourceConfigInfo(dataSourceRequest.getDatasourceConfigInfo())
                .withDsi(DatasourceDsiFormatter.getDsi(dataSourceRequest.getDatasourceType(), dataSourceRequest.getDatasourceConfigInfo()))
                .withDatasourceType(dataSourceRequest.getDatasourceType())
                .withTags(dataSourceRequest.getTags())
                .withCreateUser(dataSourceRequest.getCreateUser())
                .withCreateTime(now)
                .withUpdateUser(dataSourceRequest.getUpdateUser())
                .withUpdateTime(now)
                .build();
        boolean isExist = isDatasourceExist(dataSourceBasicInfo);
        if (isExist) {
            throw new IllegalArgumentException("datasource with type " + dataSourceBasicInfo.getDatasourceType()
                    + " and connection info " + dataSourceBasicInfo.getDatasourceConfigInfo() + " is exist");
        }
        dataSourceDao.create(dataSourceBasicInfo);
        dataSourceRequest.getDatasourceConnection().getDatasourceConnectionList().stream().map(mock_conn -> connectionService.addConnection(dataSourceBasicInfo.getId(), mock_conn)).collect(Collectors.toList());
        return fetchDatasource(dataSourceBasicInfo.getId()).orElse(null);
    }

    public DataSourceBasicInfo create(DataSourceBasicInfoRequest dataSourceBasicInfoRequest) {
        Optional<DataSourceBasicInfo> dataSourceBasicInfoOpt = checkName(dataSourceBasicInfoRequest.getDatasourceType(), dataSourceBasicInfoRequest.getName());
        if (dataSourceBasicInfoOpt.isPresent()) {
            throw new IllegalArgumentException("datasource with type " + dataSourceBasicInfoOpt.get().getDatasourceType()
                    + " and name " + dataSourceBasicInfoOpt.get().getName() + " is exist");
        }
        OffsetDateTime now = OffsetDateTime.now();
        DataSourceBasicInfo dataSourceBasicInfo = DataSourceBasicInfo.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withName(dataSourceBasicInfoRequest.getName())
                .withDatasourceConfigInfo(dataSourceBasicInfoRequest.getDatasourceConfigInfo())
                .withDsi(DatasourceDsiFormatter.getDsi(dataSourceBasicInfoRequest.getDatasourceType(), dataSourceBasicInfoRequest.getDatasourceConfigInfo()))
                .withDatasourceType(dataSourceBasicInfoRequest.getDatasourceType())
                .withTags(dataSourceBasicInfoRequest.getTags())
                .withCreateUser(dataSourceBasicInfoRequest.getCreateUser())
                .withCreateTime(now)
                .withUpdateUser(dataSourceBasicInfoRequest.getUpdateUser())
                .withUpdateTime(now)
                .build();
        boolean isExist = isDatasourceExist(dataSourceBasicInfo);
        if (isExist) {
            throw new IllegalArgumentException("datasource with type " + dataSourceBasicInfo.getDatasourceType()
                    + " and connection info " + dataSourceBasicInfo.getDatasourceConfigInfo() + " is exist");
        }
        dataSourceDao.create(dataSourceBasicInfo);
        return dataSourceBasicInfo;
    }

    private Optional<DataSourceBasicInfo> checkName(DatasourceType datasourceType, String name) {
        return dataSourceDao.fetchByTypeAndName(datasourceType, name);
    }


    public DataSourceBasicInfo update(Long id, DataSourceBasicInfoRequest dataSourceBasicInfoRequest) {
        Optional<DataSourceBasicInfo> dataSourceBasicInfoOpt = checkName(dataSourceBasicInfoRequest.getDatasourceType(), dataSourceBasicInfoRequest.getName());
        if (dataSourceBasicInfoOpt.isPresent() && Boolean.FALSE.equals(dataSourceBasicInfoOpt.get().getId().equals(id))) {
            throw new IllegalArgumentException("datasource with type " + dataSourceBasicInfoOpt.get().getDatasourceType()
                    + " and name" + dataSourceBasicInfoOpt.get().getName() + " is exist");
        }
        DataSourceBasicInfo dataSourceBasicInfo = DataSourceBasicInfo.newBuilder()
                .withId(id)
                .withName(dataSourceBasicInfoRequest.getName())
                .withDatasourceConfigInfo(dataSourceBasicInfoRequest.getDatasourceConfigInfo())
                .withDsi(DatasourceDsiFormatter.getDsi(dataSourceBasicInfoRequest.getDatasourceType(), dataSourceBasicInfoRequest.getDatasourceConfigInfo()))
                .withDatasourceType(dataSourceBasicInfoRequest.getDatasourceType())
                .withTags(dataSourceBasicInfoRequest.getTags())
                .withUpdateUser(dataSourceBasicInfoRequest.getUpdateUser())
                .withUpdateTime(OffsetDateTime.now())
                .build();
        dataSourceDao.update(dataSourceBasicInfo);
        return dataSourceDao.findById(id).orElse(null);

    }

    public void delete(Long id) {
        dataSourceDao.delete(id);
        connectionService.deleteConnectionByDatasource(id);
    }


    public Optional<DataSource> fetchDatasource(Long datasourceId) {
        DataSourceBasicInfo basicInfo = dataSourceDao.findById(datasourceId).orElse(null);
        if (Objects.isNull(basicInfo)) {
            return Optional.empty();
        }
        return Optional.ofNullable(convertToDatasource(basicInfo));
    }


}
