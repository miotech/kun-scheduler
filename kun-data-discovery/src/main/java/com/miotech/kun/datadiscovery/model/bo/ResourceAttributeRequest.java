package com.miotech.kun.datadiscovery.model.bo;

import com.miotech.kun.metadata.core.model.constant.ResourceType;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @program: kun
 * @description: search
 * @author: zemin  huang
 * @create: 2022-03-08 10:16
 **/

@Data
public class ResourceAttributeRequest implements Serializable {
    private String resourceAttributeName;
    private Map<String, Object> resourceAttributeMap;
    private Boolean showDeleted = false;

}
