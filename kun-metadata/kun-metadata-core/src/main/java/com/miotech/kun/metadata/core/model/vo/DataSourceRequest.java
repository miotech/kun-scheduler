package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.metadata.core.model.connection.DatasourceConnection;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;

import java.util.List;
import java.util.Map;

@JsonDeserialize(builder = DataSourceRequest.Builder.class)
public class DataSourceRequest {

    private final String name;

    private final DatasourceType datasourceType;

    private final DatasourceConnection datasourceConnection;

    private final Map<String, Object> datasourceConfigInfo;

    private final List<String> tags;

    private final String createUser;

    private final String updateUser;

    public DataSourceRequest(String name, DatasourceType datasourceType, DatasourceConnection datasourceConnection, Map<String, Object> datasourceConfigInfo, List<String> tags, String createUser, String updateUser) {
        this.name = name;
        this.datasourceType = datasourceType;
        this.datasourceConnection = datasourceConnection;
        this.datasourceConfigInfo = datasourceConfigInfo;
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

    public DatasourceConnection getConnectionConfig() {
        return datasourceConnection;
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

    public DatasourceConnection getDatasourceConnection() {
        return datasourceConnection;
    }

    public Map<String, Object> getDatasourceConfigInfo() {
        return datasourceConfigInfo;
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private String name;
        private DatasourceType datasourceType;
        private DatasourceConnection datasourceConnection;
        private Map<String, Object> datasourceConfigInfo;
        private List<String> tags;
        private String createUser;
        private String updateUser;

        private Builder() {
        }

        public static Builder aDataSourceRequest() {
            return new Builder();
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDatasourceType(DatasourceType datasourceType) {
            this.datasourceType = datasourceType;
            return this;
        }

        public Builder withDatasourceConnection(DatasourceConnection datasourceConnection) {
            this.datasourceConnection = datasourceConnection;
            return this;
        }

        public Builder withDatasourceConfigInfo(Map<String, Object> datasourceConfigInfo) {
            this.datasourceConfigInfo = datasourceConfigInfo;
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
            return new DataSourceRequest(name, datasourceType, datasourceConnection, datasourceConfigInfo, tags, createUser, updateUser);
        }
    }
}
