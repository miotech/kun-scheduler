package com.miotech.kun.dataquality.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.json.simple.JSONObject;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/16
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DataQualityCase extends DataQualityCaseBasic {

    private String description;

    private String dimension;

    private JSONObject dimensionConfig;

    private List<DataQualityRule> validateRules;

    private List<DatasetBasic> relatedTables;

}
