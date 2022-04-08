package com.miotech.kun.datadiscovery.model.entity;

import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.search.ResourceAttribute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @program: kun
 * @description: glossary Searched Info
 * @author: zemin  huang
 * @create: 2022-04-07 13:33
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlossarySearchedInfo {
    private Long gid;
    private ResourceType resourceType;
    private String name;
    private String description;
    private ResourceAttribute resourceAttribute;
    private boolean deleted;
    private List<GlossaryBasicInfo> ancestryGlossaryList;

}
