package com.miotech.kun.datadiscover.constant;

/**
 * @author: Melo
 * @created: 5/26/20
 */
public enum ErrorCode {

    SUCCESS(0, "Operation Successful"),
    FAILED(1, "Operation Failed");

    private final int code;
    private final String note;
    ErrorCode(int code, String note) {
        this.code = code;
        this.note = note;
    }

    public int getCode() {
        return code;
    }

    public String getNote() {
        return note;
    }
}
