package com.miotech.kun.dataquality.model.bo;

import com.miotech.kun.dataquality.model.entity.DataQualityRule;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/16
 */
@Data
public class DataQualityRequest {

    private String name;

    private List<String> types;

    private String description;

    private String dimension;

    private JSONObject dimensionConfig;

    private List<DataQualityRule> validateRules;

    private List<Long> relatedTableIds;

    private Long taskId;

    private String createUser;

    private Long createTime;

    private String updateUser;

    private Long updateTime;
}
