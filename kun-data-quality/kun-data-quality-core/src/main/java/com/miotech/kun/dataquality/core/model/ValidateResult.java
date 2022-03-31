package com.miotech.kun.dataquality.core.model;

public enum ValidateResult {
    RUNNING,
    BLOCK_CASE_FAILED,
    FINAL_SUCCESS_FAILED,
    SUCCESS;

    public boolean isFinished(){
        return this == BLOCK_CASE_FAILED || this == FINAL_SUCCESS_FAILED || this == SUCCESS;
    }
}
