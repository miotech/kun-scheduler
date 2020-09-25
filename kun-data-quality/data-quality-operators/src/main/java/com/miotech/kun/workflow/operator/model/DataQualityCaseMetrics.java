package com.miotech.kun.workflow.operator.model;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/9/27
 */
@Data
public class DataQualityCaseMetrics {

    Long caseId;

    String errorReason;

    CaseStatus caseStatus;
}
