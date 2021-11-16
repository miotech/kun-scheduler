package com.miotech.kun.dataquality.web.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/11/4
 */
@Data
public class DataQualityHistory {

    String status;

    String errorReason;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    OffsetDateTime updateTime;

    Long continuousFailingCount;

    List<DataQualityRule> ruleRecords;
}
