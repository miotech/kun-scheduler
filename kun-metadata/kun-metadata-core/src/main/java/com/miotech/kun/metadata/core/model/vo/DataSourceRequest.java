package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfig;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;

import java.util.List;

@JsonDeserialize(builder = DataSourceRequest.Builder.class)
public class DataSourceRequest {

    private final String name;

    private final DatasourceType datasourceType;

    private final ConnectionConfig connectionConfig;

    private final List<String> tags;

    private final String createUser;

    private final String updateUser;

    public DataSourceRequest(String name, DatasourceType datasourceType, ConnectionConfig connectionConfig, List<String> tags, String createUser, String updateUser) {
        this.name = name;
        this.datasourceType = datasourceType;
        this.connectionConfig = connectionConfig;
        this.tags = tags;
        this.createUser = createUser;
        this.updateUser = updateUser;
    }

    public String getName() {
        return name;
    }

    public DatasourceType getDatasourceType() {
        return datasourceType;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
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
        private DatasourceType datasourceType;
        private ConnectionConfig connectionConfig;
        private List<String> tags;
        private String createUser;
        private String updateUser;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDatasourceType(DatasourceType datasourceType) {
            this.datasourceType = datasourceType;
            return this;
        }

        public Builder withConnectionConfig(ConnectionConfig connectionConfig) {
            this.connectionConfig = connectionConfig;
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
            return new DataSourceRequest(name, datasourceType, connectionConfig, tags, createUser, updateUser);
        }
    }
}
