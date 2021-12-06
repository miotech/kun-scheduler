package com.miotech.kun.datadashboard.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AbnormalCase {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long caseId;

    private String caseName;

    private String status;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime updateTime;

    private Long continuousFailingCount;

    private String caseOwner;

    private List<DataQualityRule> ruleRecords;

}
