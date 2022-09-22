package com.miotech.kun.datadiscovery.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.miotech.kun.metadata.core.model.connection.ConnScope;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-09-20 15:25
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasourceConnectionVO {

    private List<ConnectionInfoSecurityVO> userConnectionList;

    private ConnectionInfoSecurityVO dataConnection;

    private ConnectionInfoSecurityVO metadataConnection;

    private ConnectionInfoSecurityVO storageConnection;

    public DatasourceConnectionVO(List<ConnectionInfoSecurityVO> connectionInfos) {
        Map<ConnScope, List<ConnectionInfoSecurityVO>> map = connectionInfos.stream().collect(Collectors.groupingBy(ConnectionInfoSecurityVO::getConnScope));
        this.userConnectionList = Lists.newArrayList(map.get(ConnScope.USER_CONN));
        this.dataConnection = getConnScopeConnectionInfo(map, ConnScope.DATA_CONN);
        this.metadataConnection = getConnScopeConnectionInfo(map, ConnScope.METADATA_CONN);
        this.storageConnection = getConnScopeConnectionInfo(map, ConnScope.STORAGE_CONN);
    }

    private ConnectionInfoSecurityVO getConnScopeConnectionInfo(Map<ConnScope, List<ConnectionInfoSecurityVO>> map, ConnScope connScope) {
        List<ConnectionInfoSecurityVO> infos = map.get(connScope);
        if (CollectionUtils.isNotEmpty(infos)) {
            return infos.get(0);
        }
        return null;
    }

    @JsonIgnore
    public List<ConnectionInfoSecurityVO> getDatasourceConnectionList() {
        ArrayList<ConnectionInfoSecurityVO> connectionInfos = Lists.newArrayList(storageConnection, metadataConnection, dataConnection);
        connectionInfos.addAll(userConnectionList);
        return connectionInfos;
    }

}
