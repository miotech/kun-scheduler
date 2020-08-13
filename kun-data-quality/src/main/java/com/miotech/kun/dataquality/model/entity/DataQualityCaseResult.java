package com.miotech.kun.dataquality.model.entity;

import lombok.Data;

import java.util.List;

/**
 * @author: Melo
 * @created: 8/13/20
 */
@Data
public class DataQualityCaseResult {

    long taskId;

    long taskRunId;

    long caseId;

    String caseStatus;

    List<String> errorReason;

    long startTime;

    long endTime;

}
