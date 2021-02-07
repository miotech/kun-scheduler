package com.miotech.kun.common.model;

public class AcknowledgementVO {
    private final boolean ack = true;

    private final String message;

    public AcknowledgementVO() {
        message = "";
    }

    public AcknowledgementVO(String message) {
        this.message = message;
    }

    public boolean isAck() {
        return ack;
    }

    public String getMessage() { return message; }
}
