package com.miotech.kun.metadata.core.model.datasource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.metadata.core.model.connection.DatasourceConnection;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@JsonDeserialize(builder = DataSource.Builder.class)
public class DataSource {

    private final Long id;
    private final String name;
    private final DatasourceConnection datasourceConnection;
    private final Map<String, Object> datasourceConfigInfo;
    private final DatasourceType datasourceType;
    private final List<String> tags;
    private final String createUser;
    private final OffsetDateTime createTime;
    private final String updateUser;
    private final OffsetDateTime updateTime;

    public DataSource(Long id, String name, DatasourceConnection datasourceConnection, Map<String, Object> datasourceConfigInfo, DatasourceType datasourceType,
                      List<String> tags, String createUser, OffsetDateTime createTime, String updateUser, OffsetDateTime updateTime) {
        this.id = id;
        this.name = name;
        this.datasourceConnection = datasourceConnection;
        this.datasourceConfigInfo = datasourceConfigInfo;
        this.datasourceType = datasourceType;
        this.tags = tags;
        this.createUser = createUser;
        this.createTime = createTime;
        this.updateUser = updateUser;
        this.updateTime = updateTime;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public DatasourceConnection getDatasourceConnection() {
        return datasourceConnection;
    }

    public Map<String, Object> getDatasourceConfigInfo() {
        return datasourceConfigInfo;
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

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private Long id;
        private String name;
        private DatasourceConnection datasourceConnection;
        private Map<String, Object> datasourceCofigInfo;
        private DatasourceType datasourceType;
        private List<String> tags;
        private String createUser;
        private OffsetDateTime createTime;
        private String updateUser;
        private OffsetDateTime updateTime;

        private Builder() {
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }


        public Builder withDatasourceConnection(DatasourceConnection datasourceConnection) {
            this.datasourceConnection = datasourceConnection;
            return this;
        }

        public Builder withDatasourceConfigInfo(Map<String, Object> datasourceCofigInfo) {
            this.datasourceCofigInfo = datasourceCofigInfo;
            return this;
        }

        public Builder withDatasourceType(DatasourceType datasourceType) {
            this.datasourceType = datasourceType;
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

        public Builder withCreateTime(OffsetDateTime createTime) {
            this.createTime = createTime;
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

        public DataSource build() {
            return new DataSource(id, name, datasourceConnection, datasourceCofigInfo, datasourceType, tags, createUser, createTime, updateUser, updateTime);
        }
    }
}
