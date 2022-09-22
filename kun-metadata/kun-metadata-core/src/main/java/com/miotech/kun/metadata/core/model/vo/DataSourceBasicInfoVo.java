package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@JsonDeserialize(builder = DataSourceBasicInfoVo.Builder.class)
public class DataSourceBasicInfoVo {

    private final Long id;
    private final DatasourceType datasourceType;
    private final Map<String, Object> datasourceConfigInfo;
    private final String name;
    private final List<String> tags;
    private final String createUser;
    private final OffsetDateTime createTime;
    private final String updateUser;
    private final OffsetDateTime updateTime;

    public DataSourceBasicInfoVo(Long id, DatasourceType datasourceType, Map<String, Object> datasourceConfigInfo,
                                 String name, List<String> tags, String createUser, OffsetDateTime createTime, String updateUser, OffsetDateTime updateTime) {
        this.id = id;
        this.datasourceType = datasourceType;
        this.datasourceConfigInfo = datasourceConfigInfo;
        this.name = name;
        this.tags = tags;
        this.createUser = createUser;
        this.createTime = createTime;
        this.updateUser = updateUser;
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public DatasourceType getDatasourceType() {
        return datasourceType;
    }

    public Map<String, Object> getDatasourceConfigInfo() {
        return datasourceConfigInfo;
    }

    public String getName() {
        return name;
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

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private DatasourceType datasourceType;
        private Map<String, Object> datasourceConfigInfo;
        private String name;
        private List<String> tags;
        private String createUser;
        private OffsetDateTime createTime;
        private String updateUser;
        private OffsetDateTime updateTime;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withId(Long id) {
            this.id = id;
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

        public Builder withName(String name) {
            this.name = name;
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

        public DataSourceBasicInfoVo build() {
            return new DataSourceBasicInfoVo(id, datasourceType, datasourceConfigInfo, name, tags, createUser, createTime, updateUser, updateTime);
        }
    }
}
