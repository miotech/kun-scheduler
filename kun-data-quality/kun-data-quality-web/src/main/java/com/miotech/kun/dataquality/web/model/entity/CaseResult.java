package com.miotech.kun.dataquality.web.model.entity;

import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.web.model.DataQualityStatus;

public class CaseResult {

    private final DataQualityStatus dataQualityStatus;
    private final CaseType caseType;

    public CaseResult(DataQualityStatus dataQualityStatus, CaseType caseType) {
        this.dataQualityStatus = dataQualityStatus;
        this.caseType = caseType;
    }

    public DataQualityStatus getDataQualityStatus() {
        return dataQualityStatus;
    }

    public CaseType getCaseType() {
        return caseType;
    }
}
