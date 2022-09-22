package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;
import com.miotech.kun.metadata.core.model.datasource.ConnectionTemplate;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public class DatasourceConnection {
    private final List<ConnectionInfo> userConnectionList;
    private final ConnectionInfo dataConnection;
    private final ConnectionInfo metadataConnection;
    private final ConnectionInfo storageConnection;

    @JsonCreator
    public DatasourceConnection(
            @JsonProperty("userConnectionList") List<ConnectionInfo> userConnectionList,
            @JsonProperty("dataConnection") ConnectionInfo dataConnection,
            @JsonProperty("metadataConnection") ConnectionInfo metadataConnection,
            @JsonProperty("storageConnection") ConnectionInfo storageConnection) {
        this.userConnectionList = userConnectionList;
        this.dataConnection = dataConnection;
        this.metadataConnection = metadataConnection;
        this.storageConnection = storageConnection;
    }

    public DatasourceConnection(List<ConnectionInfo> connectionInfos) {
        if (CollectionUtils.isEmpty(connectionInfos)) {
            throw new IllegalArgumentException(" connection list can not be empty");
        }
        Map<ConnScope, List<ConnectionInfo>> map = connectionInfos.stream().collect(Collectors.groupingBy(ConnectionInfo::getConnScope));
        this.userConnectionList = Lists.newArrayList(map.get(ConnScope.USER_CONN));
        this.dataConnection = getConnScopeConnectionInfo(map, ConnScope.DATA_CONN);
        this.metadataConnection = getConnScopeConnectionInfo(map, ConnScope.METADATA_CONN);
        this.storageConnection = getConnScopeConnectionInfo(map, ConnScope.STORAGE_CONN);
    }

    private ConnectionInfo getConnScopeConnectionInfo(Map<ConnScope, List<ConnectionInfo>> map, ConnScope connScope) {
        List<ConnectionInfo> infos = map.get(connScope);
        if (CollectionUtils.isNotEmpty(infos)) {
            return infos.get(0);
        }
        return null;
    }

    public List<ConnectionInfo> getUserConnectionList() {
        return userConnectionList.stream().sorted(Comparator.comparing(ConnectionInfo::getCreateTime)).collect(Collectors.toList());
    }

    public ConnectionInfo getDataConnection() {
        return dataConnection;

    }

    public ConnectionInfo getMetadataConnection() {
        return metadataConnection;
    }

    public ConnectionInfo getStorageConnection() {
        return storageConnection;
    }

    public List<ConnectionInfo> getDatasourceConnectionList() {
        ArrayList<ConnectionInfo> connectionInfos = Lists.newArrayList(storageConnection, metadataConnection, dataConnection);
        connectionInfos.addAll(userConnectionList);
        return connectionInfos;
    }

    public Builder cloneBuilder() {
        return newBuilder()
                .withUserConnectionList(userConnectionList)
                .withDataConnection(dataConnection)
                .withMetadataConnection(metadataConnection)
                .withStorageConnection(storageConnection);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private List<ConnectionInfo> userConnectionList;
        private ConnectionInfo dataConnection;
        private ConnectionInfo metadataConnection;
        private ConnectionInfo storageConnection;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withUserConnectionList(List<ConnectionInfo> userConnectionList) {
            this.userConnectionList = userConnectionList;
            return this;
        }

        public Builder withDataConnection(ConnectionInfo dataConnection) {
            this.dataConnection = dataConnection;
            return this;
        }

        public Builder withMetadataConnection(ConnectionInfo metadataConnection) {
            this.metadataConnection = metadataConnection;
            return this;
        }

        public Builder withStorageConnection(ConnectionInfo storageConnection) {
            this.storageConnection = storageConnection;
            return this;
        }

        public DatasourceConnection build() {
            return new DatasourceConnection(userConnectionList, dataConnection, metadataConnection, storageConnection);
        }
    }
}
