package com.miotech.kun.metadata.core.model.connection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * @program: kun
 * @description: connection info
 * @author: zemin  huang
 * @create: 2022-09-13 10:04
 **/
@JsonDeserialize(builder = ConnectionInfo.Builder.class)
public class ConnectionInfo {
    private final Long id;
    private final Long datasourceId;
    private final String name;
    private final ConnScope connScope;
    private final ConnectionConfigInfo connectionConfigInfo;
    private final String description;
    private final Boolean deleted;
    private final String updateUser;
    private final OffsetDateTime updateTime;
    private final String createUser;
    private final OffsetDateTime createTime;

    public ConnectionInfo(Long id, Long datasourceId, String name, ConnScope connScope, ConnectionConfigInfo connectionConfigInfo,
                          String description, Boolean deleted, String updateUser, OffsetDateTime updateTime, String createUser, OffsetDateTime createTime) {
        this.id = id;
        this.datasourceId = datasourceId;
        this.name = name;
        this.connScope = connScope;
        this.connectionConfigInfo = connectionConfigInfo;
        this.description = description;
        this.deleted = deleted;
        this.updateUser = updateUser;
        this.updateTime = updateTime;
        this.createUser = createUser;
        this.createTime = createTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getDatasourceId() {
        return datasourceId;
    }

    public ConnectionConfigInfo getConnectionConfigInfo() {
        return connectionConfigInfo;
    }

    public ConnectionType getConnectionType() {
        return connectionConfigInfo.getConnectionType();
    }


    public String getDescription() {
        return description;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public ConnScope getConnScope() {
        return connScope;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return newBuilder()
                .withId(id)
                .withDatasourceId(datasourceId)
                .withName(name)
                .withConnScope(connScope)
                .withConnectionConfigInfo(connectionConfigInfo)
                .withDescription(description)
                .withDeleted(deleted)
                .withUpdateUser(updateUser)
                .withUpdateTime(updateTime)
                .withCreateUser(createUser)
                .withCreateTime(createTime);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionInfo that = (ConnectionInfo) o;
        return Objects.equals(id, that.id) && Objects.equals(datasourceId, that.datasourceId) && Objects.equals(name, that.name) && connScope == that.connScope && Objects.equals(connectionConfigInfo, that.connectionConfigInfo) && Objects.equals(description, that.description) && Objects.equals(deleted, that.deleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, datasourceId, name, connScope, connectionConfigInfo, description, deleted);
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private Long id;
        private Long datasourceId;
        private String name;
        private ConnScope connScope;
        private ConnectionConfigInfo connectionConfigInfo;
        private String description;
        private Boolean deleted;
        private String updateUser;
        private OffsetDateTime updateTime;
        private String createUser;
        private OffsetDateTime createTime;

        private Builder() {
        }

        public static Builder aConnectionInfo() {
            return new Builder();
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
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

        public Builder withDeleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder withUpdateUser(String updateUser) {
            this.updateUser = updateUser;
            return this;
        }

        public Builder withUpdateTime(OffsetDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder withCreateUser(String createUser) {
            this.createUser = createUser;
            return this;
        }

        public Builder withCreateTime(OffsetDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public ConnectionInfo build() {
            return new ConnectionInfo(id, datasourceId, name, connScope, connectionConfigInfo, description, deleted, updateUser, updateTime, createUser, createTime);
        }
    }

    @Override
    public String toString() {
        return "ConnectionInfo{" +
                "id=" + id +
                ", datasourceId=" + datasourceId +
                ", name='" + name + '\'' +
                ", connScope=" + connScope +
                ", connectionConfigInfo=" + connectionConfigInfo +
                ", description='" + description + '\'' +
                ", deleted=" + deleted +
                ", updateUser='" + updateUser + '\'' +
                ", updateTime=" + updateTime +
                ", createUser='" + createUser + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
