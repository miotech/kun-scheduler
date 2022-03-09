package com.miotech.kun.metadata.core.model.constant;

import com.miotech.kun.metadata.core.model.search.DataSetResourceAttribute;
import com.miotech.kun.metadata.core.model.search.GlossaryResourceAttribute;
import com.miotech.kun.metadata.core.model.search.ResourceAttribute;

/**
 * @program: kun
 * @description: resource type
 * @author: zemin  huang
 * @create: 2022-03-08 10:07
 **/
public enum ResourceType {
    DATASET( DataSetResourceAttribute.class),
    GLOSSARY( GlossaryResourceAttribute.class);

    private final Class<? extends ResourceAttribute> resourceAttributeClass;

    ResourceType( Class<? extends ResourceAttribute> resourceAttributeClass) {
        this.resourceAttributeClass = resourceAttributeClass;
    }

    public Class<? extends ResourceAttribute> getResourceAttributeClass() {
        return resourceAttributeClass;
    }


}
