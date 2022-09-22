package com.miotech.kun.metadata.web.controller;

import com.google.inject.Inject;
import com.miotech.kun.commons.web.annotation.RequestBody;
import com.miotech.kun.commons.web.annotation.RouteMapping;
import com.miotech.kun.commons.web.annotation.RouteVariable;
import com.miotech.kun.metadata.common.service.ConnectionService;
import com.miotech.kun.metadata.core.model.connection.ConnScope;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import com.miotech.kun.metadata.core.model.vo.ConnectionBasicInfoVo;
import com.miotech.kun.metadata.core.model.vo.ConnectionRequest;
import com.miotech.kun.metadata.web.model.vo.AcknowledgementVO;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description: connection controller
 * @author: zemin  huang
 * @create: 2022-09-19 15:44
 **/
public class ConnectionController {
    @Inject
    ConnectionService connectionService;


    @RouteMapping(url = "/connection", method = "POST")
    public ConnectionBasicInfoVo createConnection(@RequestBody ConnectionRequest connectionRequest) {
        return convertVo(connectionService.addConnection(connectionRequest));
    }

    @RouteMapping(url = "/connection/{id}", method = "PUT")
    public ConnectionBasicInfoVo updateConnection(@RouteVariable Long id, @RequestBody ConnectionRequest connectionRequest) {
        return convertVo(connectionService.updateConnection(id, connectionRequest));
    }

    @RouteMapping(url = "/connection/{id}", method = "DELETE")
    public AcknowledgementVO deleteConnection(@RouteVariable Long id) {
        connectionService.deleteConnection(id);
        return new AcknowledgementVO("Delete success");
    }

    @RouteMapping(url = "/connection/{id}", method = "GET")
    public ConnectionBasicInfoVo getConnectionInfo(@RouteVariable Long id) {
        return convertVo(connectionService.findConnection(id).orElse(null));
    }

    @RouteMapping(url = "/connection/{datasourceId}/{connScope}", method = "GET")
    public List<ConnectionBasicInfoVo> getConnectionInfoListByDatasource(@RouteVariable Long datasourceId, @RouteVariable ConnScope connScope) {
        return connectionService.fetchDatasourceConnectionList(datasourceId, connScope).stream().map(this::convertVo).collect(Collectors.toList());
    }

    private ConnectionBasicInfoVo convertVo(ConnectionInfo entity) {
        if (Objects.isNull(entity)) {
            return null;
        }

        return ConnectionBasicInfoVo.newBuilder()
                .withId(entity.getId())
                .withDatasourceId(entity.getDatasourceId())
                .withName(entity.getName())
                .withConnScope(entity.getConnScope())
                .withConnectionConfigInfo(entity.getConnectionConfigInfo())
                .withDescription(entity.getDescription())
                .withCreateUser(entity.getCreateUser())
                .withCreateTime(entity.getCreateTime())
                .withUpdateTime(entity.getUpdateTime())
                .withUpdateUser(entity.getUpdateUser())
                .build();

    }
}
