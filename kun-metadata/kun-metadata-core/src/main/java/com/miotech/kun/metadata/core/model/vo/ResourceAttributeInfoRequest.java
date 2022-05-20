package com.miotech.kun.metadata.core.model.vo;

import com.miotech.kun.metadata.core.model.constant.ResourceType;

import java.io.Serializable;
import java.util.Map;

/**
 * @program: kun
 * @description: search Dao
 * @author: zemin  huang
 * @create: 2022-03-08 10:16
 **/

public class ResourceAttributeInfoRequest implements Serializable {
    private ResourceType resourceType;
    private String resourceAttributeName;
    private Map<String, Object> resourceAttributeMap;
    private Boolean showDeleted;

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceAttributeName() {
        return resourceAttributeName;
    }

    public void setResourceAttributeName(String resourceAttributeName) {
        this.resourceAttributeName = resourceAttributeName;
    }

    public Map<String, Object> getResourceAttributeMap() {
        return resourceAttributeMap;
    }

    public void setResourceAttributeMap(Map<String, Object> resourceAttributeMap) {
        this.resourceAttributeMap = resourceAttributeMap;
    }

    public Boolean getShowDeleted() {
        return showDeleted;
    }

    public void setShowDeleted(Boolean showDeleted) {
        this.showDeleted = showDeleted;
    }

    @Override
    public String toString() {
        return "ResourceAttributeInfoRequest{" +
                "resourceType=" + resourceType +
                ", resourceAttributeName='" + resourceAttributeName + '\'' +
                ", resourceAttributeMap=" + resourceAttributeMap +
                ", showDeleted=" + showDeleted +
                '}';
    }
}
