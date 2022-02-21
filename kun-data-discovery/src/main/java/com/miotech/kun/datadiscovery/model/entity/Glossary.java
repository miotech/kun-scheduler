package com.miotech.kun.datadiscovery.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/17
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class Glossary extends GlossaryBasicInfo {

    private GlossaryBasicInfo parent;

    private List<Asset> assets;

    private List<GlossaryBasicInfo> ancestryGlossaryList;

    private List<GlossaryBasicInfoWithCount> glossaryCountList;

    private Integer  childrenCount;


}
