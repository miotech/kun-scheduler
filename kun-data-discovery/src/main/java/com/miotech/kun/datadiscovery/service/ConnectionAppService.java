package com.miotech.kun.datadiscovery.service;

import com.google.common.base.Preconditions;
import com.miotech.kun.common.model.AcknowledgementVO;
import com.miotech.kun.datadiscovery.model.entity.SecurityInfo;
import com.miotech.kun.datadiscovery.model.enums.ConnectionRole;
import com.miotech.kun.datadiscovery.model.enums.SecurityModule;
import com.miotech.kun.datadiscovery.model.vo.ConnectionInfoSecurityVO;
import com.miotech.kun.datadiscovery.model.vo.ConnectionInfoVO;
import com.miotech.kun.datadiscovery.model.vo.DatasourceConnectionVO;
import com.miotech.kun.datadiscovery.util.CollectionSolver;
import com.miotech.kun.datadiscovery.util.JSONUtils;
import com.miotech.kun.datadiscovery.util.convert.AppBasicConversionService;
import com.miotech.kun.metadata.core.model.connection.ConnScope;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.metadata.core.model.vo.ConnectionBasicInfoVo;
import com.miotech.kun.metadata.core.model.vo.ConnectionRequest;
import com.miotech.kun.security.common.KunRole;
import com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp;
import com.miotech.kun.security.facade.rpc.ScopeRole;
import com.miotech.kun.security.service.BaseSecurityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-09-20 14:31
 **/
@Service
@Slf4j
public class ConnectionAppService extends BaseSecurityService {
    @Value("${metadata.base-url:localhost:8084}")
    String url;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SecurityRpcClient securityRpcClient;

    private static final SecurityModule SECURITY_MODULE = SecurityModule.CONNECTION;

    public void updateSecurityConfig(Long connId, ConnScope connScope, List<String> newUser) {
        switch (connScope) {
            case DATA_CONN:
            case STORAGE_CONN:
            case METADATA_CONN:
                if (existSecurityInfo(connId, ConnectionRole.CONNECTION_MANAGER, getCurrentUsername())) {
                    break;
                }
                securityRpcClient.addScopeOnSpecifiedRole(SECURITY_MODULE.name(), ConnectionRole.CONNECTION_MANAGER.getName(), getCurrentUsername(), Collections.singletonList(connId.toString()));
                break;
            case USER_CONN:
                List<String> oldUser = getSecurityUserList(connId);
                CollectionSolver<String> solver = new CollectionSolver<>(oldUser, newUser);
                Collection<String> remove = solver.subtractRemove();
                log.info("subtractRemove user:{}", remove);
                remove.forEach(user -> securityRpcClient.deleteScopeOnSpecifiedRole(SECURITY_MODULE.name(), ConnectionRole.CONNECTION_USER.getName(), user, Collections.singletonList(connId.toString())));
                Collection<String> add = solver.subtractAdd();
                log.info("subtractAdd user:{}", add);
                add.stream().filter(user -> !existSecurityInfo(connId, ConnectionRole.CONNECTION_USER, user))
                        .forEach(user -> securityRpcClient.addScopeOnSpecifiedRole(SECURITY_MODULE.name(), ConnectionRole.CONNECTION_USER.getName(), user, Collections.singletonList(connId.toString())));
                break;
            default:
                throw new IllegalArgumentException(String.format("conn scope type not support:%s", connScope));

        }

    }


    public ConnectionInfoVO findBasicInfoById(Long id) {
        ConnectionBasicInfoVo connectionInfo = findById(id);
        if (Objects.isNull(connectionInfo)) {
            throw new IllegalArgumentException(String.format("connection not exist:%s", id));
        }
        return AppBasicConversionService.getSharedInstance().convert(connectionInfo, ConnectionInfoVO.class);
    }

    public ConnectionInfoSecurityVO findSecurityConnectionInfoById(Long id) {
        ConnectionBasicInfoVo connectionInfo = findById(id);
        return convert(connectionInfo);
    }

    public ConnectionInfoSecurityVO convert(ConnectionBasicInfoVo connectionInfo) {
        ConnectionInfoSecurityVO connectionInfoSecurityVO = AppBasicConversionService.getSharedInstance().convert(connectionInfo, ConnectionInfoSecurityVO.class);
        return getConnectionInfoSecurityVO(connectionInfoSecurityVO);
    }

    public ConnectionInfoSecurityVO convertConnectionInfo(ConnectionInfo connectionInfo) {
        ConnectionInfoSecurityVO connectionInfoSecurityVO = AppBasicConversionService.getSharedInstance().convert(connectionInfo, ConnectionInfoSecurityVO.class);
        return getConnectionInfoSecurityVO(connectionInfoSecurityVO);
    }

    private ConnectionInfoSecurityVO getConnectionInfoSecurityVO(ConnectionInfoSecurityVO connectionInfoSecurityVO) {
        if (connectionInfoSecurityVO != null) {
            connectionInfoSecurityVO.setSecurityInfo(getSecurityInfo(connectionInfoSecurityVO.getId()));
            connectionInfoSecurityVO.setSecurityUserList(getSecurityUserList(connectionInfoSecurityVO.getId()));
        }
        return connectionInfoSecurityVO;
    }

    public static ConnectionInfo convert(ConnectionInfoSecurityVO vo) {
        Preconditions.checkNotNull(vo);
        return ConnectionInfo.newBuilder()
                .withId(vo.getId())
                .withName(vo.getName())
                .withConnScope(vo.getConnScope())
                .withDatasourceId(vo.getDatasourceId())
                .withConnectionConfigInfo(vo.getConnectionConfigInfo())
                .withDescription(vo.getDescription())
                .withCreateUser(vo.getCreateUser())
                .withUpdateUser(vo.getUpdateUser())
                .withUpdateTime(vo.getUpdateTime())
                .withCreateTime(vo.getCreateTime())
                .withDeleted(vo.getDeleted())
                .build();
    }

    private SecurityInfo getSecurityInfo(Long connId) {
        RoleOnSpecifiedResourcesResp roleOnSpecifiedResources = securityRpcClient.findRoleOnSpecifiedResources(SECURITY_MODULE.name(), Collections.singletonList(String.valueOf(connId)));
        KunRole kunRole = ConnectionRole.CONNECTION_USER;
        if (Objects.nonNull(roleOnSpecifiedResources)) {
            List<ScopeRole> scopeRolesList = roleOnSpecifiedResources.getScopeRolesList();
            if (CollectionUtils.isNotEmpty(scopeRolesList)) {
                log.info(" connection,scopeRolesList:{}", scopeRolesList);
                kunRole = scopeRolesList.stream().filter(Objects::nonNull).map(ScopeRole::getRolename).map(SECURITY_MODULE.getRoleMap()::get).filter(Objects::nonNull).max(Comparator.comparing(KunRole::rank)).orElse(kunRole);
            }
        }
        SecurityInfo securityInfo = new SecurityInfo();
        securityInfo.setSecurityModule(SECURITY_MODULE);
        securityInfo.setSourceSystemId(connId);
        securityInfo.setKunRole(kunRole);
        securityInfo.setOperations(kunRole.getUserOperation());
        return securityInfo;
    }

    private boolean existSecurityInfo(Long connId, KunRole kunRole, String user) {
        RoleOnSpecifiedResourcesResp roleOnSpecifiedResources = securityRpcClient.findRoleOnSpecifiedResourcesByUser(SECURITY_MODULE.name(), Collections.singletonList(String.valueOf(connId)), user);
        if (Objects.isNull(roleOnSpecifiedResources)) {
            return false;

        }
        List<ScopeRole> scopeRolesList = roleOnSpecifiedResources.getScopeRolesList();
        if (CollectionUtils.isEmpty(scopeRolesList)) {
            return false;
        }
        return scopeRolesList.stream().filter(Objects::nonNull).map(ScopeRole::getRolename).anyMatch(s -> s.equals(kunRole.getName()));

    }

    private List<String> getSecurityUserList(Long connId) {
        return securityRpcClient.findUserList(SECURITY_MODULE.name(), ConnectionRole.CONNECTION_USER.getName(), connId);
    }

    public ConnectionBasicInfoVo createConnection(ConnectionInfoSecurityVO vo) {
        vo.setCreateUser(getCurrentUsername());
        vo.setUpdateUser(getCurrentUsername());
        ConnectionRequest request = convertToRequest(vo);
        ConnectionBasicInfoVo connection = createConnection(request);
        updateSecurityConfig(connection.getId(), connection.getConnScope(), vo.getSecurityUserList());
        return connection;
    }

    public static ConnectionRequest convertToRequest(ConnectionInfoVO conn) {
        return ConnectionRequest.newBuilder()
                .withConnectionConfigInfo(conn.getConnectionConfigInfo())
                .withConnScope(conn.getConnScope())
                .withCreateUser(conn.getCreateUser())
                .withDatasourceId(conn.getDatasourceId())
                .withDescription(conn.getDescription())
                .withName(conn.getName())
                .withUpdateUser(conn.getUpdateUser())
                .build();
    }

    public ConnectionBasicInfoVo updateConnection(Long id, ConnectionInfoSecurityVO vo) {
        vo.setUpdateUser(getCurrentUsername());
        ConnectionRequest request = convertToRequest(vo);
        ConnectionBasicInfoVo connection = updateConnection(id, request);
        updateSecurityConfig(connection.getId(), connection.getConnScope(), vo.getSecurityUserList());
        return connection;
    }

    /********************************infra client request****************************************/

    private ConnectionBasicInfoVo findById(Long id) {
        String findByIdUrl = url + String.format("/connection/%d", id);
        ConnectionBasicInfoVo connectionInfo = restTemplate.exchange(findByIdUrl, HttpMethod.GET, null, ConnectionBasicInfoVo.class).getBody();
        if (Objects.isNull(connectionInfo)) {
            throw new IllegalArgumentException(String.format("connection not exist:%s", id));
        }
        return connectionInfo;
    }

    private ConnectionBasicInfoVo createConnection(ConnectionRequest request) {
        String createUrl = url + "/connection";
        return restTemplate
                .exchange(createUrl, HttpMethod.POST, new HttpEntity(request), ConnectionBasicInfoVo.class)
                .getBody();
    }

    public ConnectionBasicInfoVo updateConnection(Long id, ConnectionRequest request) {
        String updateUrl = url + "/connection/{id}";
        return restTemplate
                .exchange(updateUrl, HttpMethod.PUT, new HttpEntity(request), ConnectionBasicInfoVo.class, id)
                .getBody();
    }

    public AcknowledgementVO deleteConnection(Long id) {
        String createUrl = url + "/connection/{id}";
        return restTemplate
                .exchange(createUrl, HttpMethod.DELETE, null, AcknowledgementVO.class, id)
                .getBody();
    }


    public List<ConnectionInfoSecurityVO> createDatasourceConnection(Long datasourceId, DatasourceConnectionVO datasourceConnection) {
        List<ConnectionInfoSecurityVO> userConnectionList = datasourceConnection.getUserConnectionList();
        userConnectionList.forEach(connectionInfo -> connectionInfo.setConnScope(ConnScope.USER_CONN));
        ConnectionInfoSecurityVO baseConnection = userConnectionList.get(0);
        if (Objects.isNull(datasourceConnection.getDataConnection())) {
            ConnectionInfoSecurityVO connectionInfoSecurityVO = copyConnectionBaseInfo(baseConnection);
            datasourceConnection.setDataConnection(connectionInfoSecurityVO);
        }
        ConnectionInfoSecurityVO dataConnection = datasourceConnection.getDataConnection();
        dataConnection.setConnScope(ConnScope.DATA_CONN);
        dataConnection.setName("dataConnection");

        if (Objects.isNull(datasourceConnection.getMetadataConnection())) {
            ConnectionInfoSecurityVO connectionInfoSecurityVO = copyConnectionBaseInfo(baseConnection);
            datasourceConnection.setMetadataConnection(connectionInfoSecurityVO);
        }
        ConnectionInfoSecurityVO metadataConnection = datasourceConnection.getMetadataConnection();
        metadataConnection.setConnScope(ConnScope.METADATA_CONN);
        metadataConnection.setName("metadataConnection");

        if (Objects.isNull(datasourceConnection.getStorageConnection())) {
            ConnectionInfoSecurityVO connectionInfoSecurityVO = copyConnectionBaseInfo(baseConnection);
            datasourceConnection.setStorageConnection(connectionInfoSecurityVO);
        }
        ConnectionInfoSecurityVO storageConnection = datasourceConnection.getStorageConnection();
        storageConnection.setConnScope(ConnScope.STORAGE_CONN);
        storageConnection.setName("storageConnection");
        List<ConnectionInfoSecurityVO> datasourceConnectionList = datasourceConnection.getDatasourceConnectionList();
        List<ConnectionInfoSecurityVO> collect = datasourceConnectionList.stream().peek(conn -> {
            conn.setDatasourceId(datasourceId);
            ConnectionBasicInfoVo connection = createConnection(conn);
            conn.setId(connection.getId());
        }).collect(Collectors.toList());
        return collect;

    }

    private ConnectionInfoSecurityVO copyConnectionBaseInfo(ConnectionInfoSecurityVO baseConnection) {
        String string = JSONUtils.toJsonString(baseConnection);
        return JSONUtils.jsonToObject(string, ConnectionInfoSecurityVO.class);
    }

    public List<ConnectionInfoSecurityVO> updateDatasourceConnection(Long id, DatasourceConnectionVO datasourceConnection) {
        List<ConnectionInfoSecurityVO> userConnectionList = datasourceConnection.getUserConnectionList();
        return userConnectionList.stream().peek(conn -> {
            ConnectionBasicInfoVo connection;
            if (Objects.isNull(conn.getId())) {
                conn.setDatasourceId(id);
                conn.setConnScope(ConnScope.USER_CONN);
                connection = createConnection(conn);
            } else {
                connection = updateConnection(conn.getId(), conn);
            }
            conn.setId(connection.getId());
        }).collect(Collectors.toList());
    }
}
