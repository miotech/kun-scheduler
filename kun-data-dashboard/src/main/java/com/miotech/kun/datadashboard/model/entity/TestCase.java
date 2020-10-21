package com.miotech.kun.datadashboard.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/9/17
 */
@Data
public class TestCase {

    @JsonSerialize(using= ToStringSerializer.class)
    Long datasetGid;

    String datasetName;

    String caseName;

    String result;

    String errorReason;

    Long updateTime;

    Long continuousFailingCount;

    String caseOwner;
}
