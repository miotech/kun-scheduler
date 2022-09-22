package com.miotech.kun.metadata.common.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.common.dao.ConnectionDao;
import com.miotech.kun.metadata.core.model.connection.ConnScope;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.metadata.core.model.connection.DatasourceConnection;
import com.miotech.kun.metadata.core.model.vo.ConnectionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @program: kun
 * @description: connection
 * @author: zemin  huang
 * @create: 2022-09-13 09:54
 **/
@Singleton
public class ConnectionService {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionService.class);
    private final ConnectionDao connectionDao;

    @Inject
    public ConnectionService(ConnectionDao connectionDao) {
        this.connectionDao = connectionDao;
    }

    public ConnectionInfo addConnection(Long datasourceId, ConnectionInfo connectionInfo) {
        return connectionDao.addConnection(datasourceId, connectionInfo);
    }

    public ConnectionInfo addConnection(ConnectionRequest connectionRequest) {
        ConnectionInfo connectionInfo = ConnectionInfo.newBuilder().
                withDatasourceId(connectionRequest.getDatasourceId())
                .withName(connectionRequest.getName())
                .withConnScope(connectionRequest.getConnScope())
                .withConnectionConfigInfo(connectionRequest.getConnectionConfigInfo())
                .withCreateUser(connectionRequest.getCreateUser())
                .withUpdateUser(connectionRequest.getUpdateUser())
                .build();
        return addConnection(connectionRequest.getDatasourceId(), connectionInfo);
    }

    public ConnectionInfo updateConnection(Long connId, ConnectionInfo connectionInfo) {
        return connectionDao.saveOrUpdateConnection(connId, connectionInfo);
    }

    public ConnectionInfo updateConnection(Long connId, ConnectionRequest connectionRequest) {
        ConnectionInfo connectionInfo = ConnectionInfo.newBuilder().
                withId(connId)
                .withDatasourceId(connectionRequest.getDatasourceId())
                .withName(connectionRequest.getName())
                .withConnScope(connectionRequest.getConnScope())
                .withConnectionConfigInfo(connectionRequest.getConnectionConfigInfo())
                .withDescription(connectionRequest.getDescription())
                .withUpdateUser(connectionRequest.getUpdateUser())
                .build();
        return updateConnection(connectionRequest.getDatasourceId(), connectionInfo);
    }

    public Optional<ConnectionInfo> findConnection(Long connId) {
        return connectionDao.findConnection(connId);
    }

    public Optional<DatasourceConnection> fetchDatasourceConnection(Long datasourceId) {
        return connectionDao.fetchDatasourceConnection(datasourceId);
    }

    public List<ConnectionInfo> fetchDatasourceConnectionList(Long datasourceId, ConnScope connScope) {
        return connectionDao.fetchDatasourceConnectionList(datasourceId, connScope);

    }

    public List<ConnectionInfo> fetchConnScopeConnection(ConnScope connScope) {
        return connectionDao.fetchConnScopeConnection(connScope);

    }

    public ConnectionInfo deleteConnection(Long id) {
        Optional<ConnectionInfo> connection = findConnection(id);
        return connection.map(connectionDao::removeConnection).orElse(null);
    }

    public void deleteConnectionByDatasource(Long datasourceId) {
        connectionDao.removeConnectionByDatasource(datasourceId);
    }
}
