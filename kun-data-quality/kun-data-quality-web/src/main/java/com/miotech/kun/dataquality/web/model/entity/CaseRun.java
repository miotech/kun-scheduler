package com.miotech.kun.dataquality.web.model.entity;

import com.miotech.kun.dataquality.web.model.DataQualityStatus;
import lombok.Data;


@Data
public class CaseRun {

    private Long id;

    private Long caseRunId;

    private Long taskRunId;

    private Long caseId;

    private DataQualityStatus status;

    private Long taskAttemptId;

    private Long validateDatasetId;

    private String validateVersion;
}
