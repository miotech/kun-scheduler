package com.miotech.kun.operationrecord.server.factory;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.operationrecord.common.model.OperationStatus;
import com.miotech.kun.operationrecord.common.model.OperationType;
import com.miotech.kun.operationrecord.server.event.TestOperationEvent;

public class MockTestOperationEventFactory {

    private MockTestOperationEventFactory() {
    }

    public static TestOperationEvent create() {
        Long id = IdGenerator.getInstance().nextId();
        return create(id);
    }

    public static TestOperationEvent create(Long id) {
        String operator = "admin";
        TestOperationEvent testOperationEvent = new TestOperationEvent(operator, OperationType.TASK_DEFINITION_EDIT, id);
        testOperationEvent.setStatus(OperationStatus.SUCCESS.name());
        return testOperationEvent;
    }

}
