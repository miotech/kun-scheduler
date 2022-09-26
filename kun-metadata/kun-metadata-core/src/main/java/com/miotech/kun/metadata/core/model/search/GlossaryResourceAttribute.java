package com.miotech.kun.metadata.core.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @program: kun
 * @description: Glossary Resource Attribute
 * @author: zemin  huang
 * @create: 2022-03-08 10:12
 **/
public class GlossaryResourceAttribute extends ResourceAttribute {
    @JsonCreator
    public GlossaryResourceAttribute(
            @JsonProperty("owners") String owners) {
        super(owners);
    }

    @Override
    public String toString() {
        return "GlossaryResourceAttribute{} " + super.toString();
    }
}
