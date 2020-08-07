package com.miotech.kun.common.constant;

/**
 * @author: Melo
 * @created: 5/26/20
 */
public enum ErrorCode {

    SUCCESS(0, "Operation Successful"),
    FAILED(1, "Operation Failed"),
    PDF_FILE_NOT_FOUND(2, "PDF file not found");

    private final Integer code;
    private final String note;
    ErrorCode(int code, String note) {
        this.code = code;
        this.note = note;
    }

    public Integer getCode() {
        return code;
    }

    public String getNote() {
        return note;
    }
}
