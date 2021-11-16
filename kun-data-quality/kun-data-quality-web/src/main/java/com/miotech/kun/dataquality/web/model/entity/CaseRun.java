package com.miotech.kun.dataquality.web.model.entity;

import lombok.Data;


@Data
public class CaseRun {

    private Long id;

    private Long caseRunId;

    private Long taskRunId;

    private Long caseId;

    private String status;
}
