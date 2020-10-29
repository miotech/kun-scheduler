package com.miotech.kun.workflow.operator.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/9/27
 */
@Data
public class DataQualityCaseMetrics {

    Long caseId;

    String errorReason;

    CaseStatus caseStatus;

    List<DataQualityRule> ruleRecords = new ArrayList<>();

    public void add(DataQualityRule ruleRecord) {
        ruleRecords.add(ruleRecord);
    }
}
