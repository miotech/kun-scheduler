package com.miotech.kun.workflow.operator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/8/13
 */
public class DataQualityRecord {

    Long caseId;

    String caseStatus;

    List<String> errorReason = new ArrayList<>();

    Long startTime;

    Long endTime;

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(String caseStatus) {
        this.caseStatus = caseStatus;
    }

    public List<String> getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(List<String> errorReason) {
        this.errorReason = errorReason;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public void appendErrorReason(String errorReason) {
        this.errorReason.add(errorReason);
    }
}
