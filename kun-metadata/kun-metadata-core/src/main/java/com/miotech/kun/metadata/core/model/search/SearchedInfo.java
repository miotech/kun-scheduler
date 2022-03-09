package com.miotech.kun.metadata.core.model.search;

import com.miotech.kun.metadata.core.model.constant.ResourceType;

/**
 * @program: kun
 * @description: search Info
 * @author: zemin  huang
 * @create: 2022-03-08 10:07
 **/
public class SearchedInfo {
    private Long gid;
    private ResourceType resourceType;
    private String name;
    private String description;
    private ResourceAttribute resourceAttribute;
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

    public SearchedInfo(Long gid, ResourceType resourceType, String name, String description,
                        ResourceAttribute resourceAttribute, boolean deleted) {
        this.gid = gid;
        this.resourceType = resourceType;
        this.name = name;
        this.description = description;
        this.resourceAttribute = resourceAttribute;
        this.deleted = deleted;
    }

    public static final class Builder {
        private Long gid;
        private ResourceType resourceType;
        private String name;
        private String description;
        private ResourceAttribute resourceAttribute;
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

        public Builder withDeleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public SearchedInfo build() {
            return new SearchedInfo(gid, resourceType, name, description, resourceAttribute, deleted);
        }
    }
}
