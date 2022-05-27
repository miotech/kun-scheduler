package com.miotech.kun.operationrecord.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.commons.pubsub.event.PublicEvent;
import com.miotech.kun.operationrecord.common.model.OperationRecordType;

public class OperationRecordEvent<E> extends PublicEvent {

    private final String operator;

    private final OperationRecordType operationRecordType;

    private final E event;

    private String status;

    @JsonCreator
    public OperationRecordEvent(@JsonProperty("operator") String operator,
                                @JsonProperty("operationRecordType") OperationRecordType operationRecordType,
                                @JsonProperty("event") E event) {
        this.operator = operator;
        this.operationRecordType = operationRecordType;
        this.event = event;
    }

    public String getOperator() {
        return operator;
    }

    public OperationRecordType getOperationRecordType() {
        return operationRecordType;
    }

    public E getEvent() {
        return event;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
