package com.miotech.kun.metadata.core.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.model.constant.ResourceType;

import java.io.Serializable;

/**
 * @program: kun
 * @description: Resource Attribute
 * @author: zemin  huang
 * @create: 2022-03-08 10:10
 **/
public class ResourceAttribute  implements Serializable {

    private String owners;

    @JsonCreator
    public ResourceAttribute( @JsonProperty("owners") String owners) {
        this.owners = owners;
    }


    public String getOwners() {
        return owners;
    }


    public static final class ResourceAttributeBuilder {
        private ResourceType resourceType;
        private String owners;

        private ResourceAttributeBuilder() {
        }

        public static ResourceAttributeBuilder aResourceAttribute() {
            return new ResourceAttributeBuilder();
        }

        public ResourceAttributeBuilder withOwners(String owners) {
            this.owners = owners;
            return this;
        }

        public ResourceAttribute build() {
            return new ResourceAttribute(owners);
        }
    }
}
