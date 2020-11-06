package com.miotech.kun.dataquality.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/11/4
 */
@Data
public class DataQualityHistory {

    String status;

    String errorReason;

    Long updateTime;

    Long continuousFailingCount;

    List<DataQualityRule> ruleRecords;
}
