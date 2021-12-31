package com.miotech.kun.dataquality.web.model.bo;

import com.miotech.kun.dataquality.web.model.entity.DataQualityRule;
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

    private Long primaryDatasetGid;

    private Long taskId;

    private String createUser;

    private Long createTime;

    private String updateUser;

    private Long updateTime;

    private Boolean isBlocking;

    public ExpectationBO convertTo() {
        return ExpectationBO.builder()
                .name(this.name)
                .types(this.types)
                .description(this.description)
                .sql((String) this.dimensionConfig.get("sql"))
                .validateRules(this.validateRules)
                .relatedTableIds(this.relatedTableIds)
                .datasetGid(this.primaryDatasetGid)
                .taskId(this.taskId)
                .isBlocking(this.isBlocking)
                .build();
    }

}
