package com.miotech.kun.metadata.core.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.miotech.kun.metadata.core.model.constant.ResourceType;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * @program: kun
 * @description: search Info
 * @author: zemin  huang
 * @create: 2022-03-08 10:07
 **/
public class SearchedInfo implements Serializable {
    private Long gid;
    private ResourceType resourceType;
    private String name;
    private String description;
    private ResourceAttribute resourceAttribute;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
    private boolean deleted;

    public Long getGid() {
        return gid;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ResourceAttribute getResourceAttribute() {
        return resourceAttribute;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    @JsonCreator
    public SearchedInfo(
            @JsonProperty("gid") Long gid,
            @JsonProperty("resourceType") ResourceType resourceType,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "resourceType", visible = true)
            @JsonProperty("resourceAttribute")
                    ResourceAttribute resourceAttribute,
            @JsonProperty("createTime") OffsetDateTime createTime,
            @JsonProperty("updateTime") OffsetDateTime updateTime,
            @JsonProperty("deleted") boolean deleted
    ) {
        this.gid = gid;
        this.resourceType = resourceType;
        this.name = name;
        this.description = description;
        this.resourceAttribute = resourceAttribute;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.deleted = deleted;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public static final class Builder {
        private Long gid;
        private ResourceType resourceType;
        private String name;
        private String description;
        private ResourceAttribute resourceAttribute;
        private OffsetDateTime createTime;
        private OffsetDateTime updateTime;
        private boolean deleted;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withGid(Long gid) {
            this.gid = gid;
            return this;
        }

        public Builder withResourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withResourceAttribute(ResourceAttribute resourceAttribute) {
            this.resourceAttribute = resourceAttribute;
            return this;
        }

        public Builder withCreateTime(OffsetDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder withUpdateTime(OffsetDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder withDeleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public SearchedInfo build() {
            return new SearchedInfo(gid, resourceType, name, description, resourceAttribute, createTime, updateTime, deleted);
        }
    }

    @Override
    public String toString() {
        return "SearchedInfo{" +
                "gid=" + gid +
                ", resourceType=" + resourceType +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", resourceAttribute=" + resourceAttribute +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", deleted=" + deleted +
                '}';
    }
}
