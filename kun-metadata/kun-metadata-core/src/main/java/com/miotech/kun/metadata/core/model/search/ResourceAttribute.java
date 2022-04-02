package com.miotech.kun.metadata.core.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.miotech.kun.metadata.core.model.constant.ResourceType;

import java.io.Serializable;

/**
 * @program: kun
 * @description: Resource Attribute
 * @author: zemin  huang
 * @create: 2022-03-08 10:10
 **/
@JsonSubTypes({@JsonSubTypes.Type(value = DataSetResourceAttribute.class)
        , @JsonSubTypes.Type(value = GlossaryResourceAttribute.class)})
public class ResourceAttribute implements Serializable {

    private String owners;

    @JsonCreator
    public ResourceAttribute(@JsonProperty("owners") String owners) {
        this.owners = owners;
    }

    public String getOwners() {
        return owners;
    }


}
