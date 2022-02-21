package com.miotech.kun.datadiscovery.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @program: kun
 * @description: glossary basic info with count
 * @author: zemin  huang
 * @create: 2022-02-24 14:29
 **/
@Data
@EqualsAndHashCode(callSuper=true)
public class GlossaryBasicInfoWithCount extends GlossaryBasicInfo {
// Number of child nodes in the first layer
    private Integer childrenCount;
// Number of depth traversal
    private Integer  dataSetCount;
}
