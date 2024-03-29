package com.miotech.kun.operationrecord.server.factory;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.operationrecord.common.event.OperationRecordEvent;
import com.miotech.kun.operationrecord.common.model.OperationRecordStatus;
import com.miotech.kun.operationrecord.common.model.OperationRecordType;

public class MockTestOperationEventFactory {

    private MockTestOperationEventFactory() {
    }

    public static OperationRecordEvent create() {
        Long id = IdGenerator.getInstance().nextId();
        return create(id);
    }

    public static OperationRecordEvent create(Long id) {
        String operator = "admin";
        OperationRecordEvent<Long> testOperationEvent = new OperationRecordEvent(operator, OperationRecordType.TASK_DEFINITION_UPDATE, id);
        testOperationEvent.setStatus(OperationRecordStatus.SUCCESS.name());
        return testOperationEvent;
    }

}
