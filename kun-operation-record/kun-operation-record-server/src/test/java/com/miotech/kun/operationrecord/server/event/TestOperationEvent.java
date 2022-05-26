package com.miotech.kun.operationrecord.server.event;

import com.miotech.kun.operationrecord.common.event.BaseOperationEvent;
import com.miotech.kun.operationrecord.common.model.OperationType;

public class TestOperationEvent extends BaseOperationEvent<Long> {

    private final Long id;

    public TestOperationEvent(String operator, OperationType operationType, Long id) {
        super(operator, operationType);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Long getEvent() {
        return getId();
    }
}
