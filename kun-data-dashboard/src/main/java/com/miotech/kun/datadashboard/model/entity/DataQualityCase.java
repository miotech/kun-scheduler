package com.miotech.kun.datadashboard.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/9/17
 */
@Data
public class DataQualityCase {

    @JsonSerialize(using= ToStringSerializer.class)
    Long datasetGid;

    String datasetName;

    @JsonSerialize(using= ToStringSerializer.class)
    Long caseId;

    String caseName;

    String status;

    String errorReason;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    OffsetDateTime updateTime;

    Long continuousFailingCount;

    String caseOwner;

    List<DataQualityRule> ruleRecords;
}
