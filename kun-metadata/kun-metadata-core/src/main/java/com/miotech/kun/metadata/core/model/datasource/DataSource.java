package com.miotech.kun.metadata.core.model.datasource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.OffsetDateTime;
import java.util.List;

@JsonDeserialize(builder = DataSource.Builder.class)
public class DataSource {

    private final Long id;
    private final String name;
    private final ConnectionInfo connectionInfo;
    private final Long typeId;
    private final List<String> tags;
    private final String createUser;
    private final OffsetDateTime createTime;
    private final String updateUser;
    private final OffsetDateTime updateTime;

    public DataSource(Long id, String name, ConnectionInfo connectionInfo, Long typeId, List<String> tags, String createUser,
                      OffsetDateTime createTime, String updateUser, OffsetDateTime updateTime) {
        this.id = id;
        this.name = name;
        this.connectionInfo = connectionInfo;
        this.typeId = typeId;
        this.tags = tags;
        this.createUser = createUser;
        this.createTime = createTime;
        this.updateUser = updateUser;
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public Long getTypeId() {
        return typeId;
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

    public Builder cloneBuilder() {
        return DataSource.newBuilder()
                .withId(id)
                .withName(name)
                .withConnectionInfo(connectionInfo)
                .withTypeId(typeId)
                .withTags(tags)
                .withCreateUser(createUser)
                .withCreateTime(createTime)
                .withUpdateUser(updateUser)
                .withUpdateTime(updateTime)
                ;
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private Long id;
        private String name;
        private ConnectionInfo connectionInfo;
        private Long typeId;
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

        public Builder withConnectionInfo(ConnectionInfo connectionInfo) {
            this.connectionInfo = connectionInfo;
            return this;
        }

        public Builder withTypeId(Long typeId) {
            this.typeId = typeId;
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
            return new DataSource(id, name, connectionInfo, typeId, tags, createUser, createTime, updateUser, updateTime);
        }
    }
}
