package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.metadata.core.model.connection.ConnScope;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfigInfo;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-09-19 15:51
 **/
@JsonDeserialize(builder = ConnectionRequest.Builder.class)
public class ConnectionRequest {
    private final Long datasourceId;
    private final String name;
    private final ConnScope connScope;
    private final ConnectionConfigInfo connectionConfigInfo;
    private final String description;
    private final String updateUser;
    private final String createUser;

    public ConnectionRequest(Long datasourceId, String name, ConnScope connScope, ConnectionConfigInfo connectionConfigInfo, String description, String updateUser, String createUser) {
        this.datasourceId = datasourceId;
        this.name = name;
        this.connScope = connScope;
        this.connectionConfigInfo = connectionConfigInfo;
        this.description = description;
        this.updateUser = updateUser;
        this.createUser = createUser;
    }

    public Long getDatasourceId() {
        return datasourceId;
    }

    public String getName() {
        return name;
    }

    public ConnScope getConnScope() {
        return connScope;
    }

    public ConnectionConfigInfo getConnectionConfigInfo() {
        return connectionConfigInfo;
    }

    public String getDescription() {
        return description;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public String getCreateUser() {
        return createUser;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private Long datasourceId;
        private String name;
        private ConnScope connScope;
        private ConnectionConfigInfo connectionConfigInfo;
        private String description;
        private String updateUser;
        private String createUser;

        private Builder() {
        }

        public static Builder aConnectionRequest() {
            return new Builder();
        }

        public Builder withDatasourceId(Long datasourceId) {
            this.datasourceId = datasourceId;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withConnScope(ConnScope connScope) {
            this.connScope = connScope;
            return this;
        }

        public Builder withConnectionConfigInfo(ConnectionConfigInfo connectionConfigInfo) {
            this.connectionConfigInfo = connectionConfigInfo;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withUpdateUser(String updateUser) {
            this.updateUser = updateUser;
            return this;
        }

        public Builder withCreateUser(String createUser) {
            this.createUser = createUser;
            return this;
        }

        public ConnectionRequest build() {
            return new ConnectionRequest(datasourceId, name, connScope, connectionConfigInfo, description, updateUser, createUser);
        }
    }
}
