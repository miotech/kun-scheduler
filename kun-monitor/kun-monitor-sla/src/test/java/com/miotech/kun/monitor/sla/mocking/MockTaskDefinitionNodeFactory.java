package com.miotech.kun.monitor.sla.mocking;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.monitor.facade.model.sla.TaskDefinitionNode;

public class MockTaskDefinitionNodeFactory {

    private MockTaskDefinitionNodeFactory() {
    }

    public static TaskDefinitionNode create() {
        long taskDefId = IdGenerator.getInstance().nextId();
        long workflowTaskId = IdGenerator.getInstance().nextId();
        return TaskDefinitionNode.from(taskDefId, "test-node", 1, 60, workflowTaskId, 30);
    }

}
