package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.metadata.core.model.datasource.ConnectionInfo;

import java.util.List;
import java.util.Map;

@JsonDeserialize(builder = DataSourceRequest.Builder.class)
public class DataSourceRequest {

    private final String name;

    private final Long typeId;

    private final ConnectionInfo connectionInfo;

    private final List<String> tags;

    private final String createUser;

    private final String updateUser;

    public DataSourceRequest(String name, Long typeId, ConnectionInfo connectionInfo, List<String> tags, String createUser, String updateUser) {
        this.name = name;
        this.typeId = typeId;
        this.connectionInfo = connectionInfo;
        this.tags = tags;
        this.createUser = createUser;
        this.updateUser = updateUser;
    }

    public String getName() {
        return name;
    }

    public Long getTypeId() {
        return typeId;
    }

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getCreateUser() {
        return createUser;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private String name;
        private Long typeId;
        private ConnectionInfo connectionInfo;
        private List<String> tags;
        private String createUser;
        private String updateUser;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withTypeId(Long typeId) {
            this.typeId = typeId;
            return this;
        }

        public Builder withConnectionInfo(ConnectionInfo connectionInfo) {
            this.connectionInfo = connectionInfo;
            return this;
        }

        public Builder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder withCreateUser(String createUser) {
            this.createUser = createUser;
            return this;
        }

        public Builder withUpdateUser(String updateUser) {
            this.updateUser = updateUser;
            return this;
        }

        public DataSourceRequest build() {
            return new DataSourceRequest(name, typeId, connectionInfo, tags, createUser, updateUser);
        }
    }
}
