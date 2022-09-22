package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;

import java.util.List;
import java.util.Map;

@JsonDeserialize(builder = DataSourceBasicInfoRequest.Builder.class)
public class DataSourceBasicInfoRequest {

    private final String name;

    private final DatasourceType datasourceType;

    private final Map<String, Object> datasourceConfigInfo;

    private final List<String> tags;

    private final String createUser;

    private final String updateUser;

    public DataSourceBasicInfoRequest(String name, DatasourceType datasourceType, Map<String, Object> datasourceConfigInfo, List<String> tags, String createUser, String updateUser) {
        this.name = name;
        this.datasourceType = datasourceType;
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


    public Map<String, Object> getDatasourceConfigInfo() {
        return datasourceConfigInfo;
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private String name;
        private DatasourceType datasourceType;
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

        public DataSourceBasicInfoRequest build() {
            return new DataSourceBasicInfoRequest(name, datasourceType, datasourceConfigInfo, tags, createUser, updateUser);
        }
    }
}
