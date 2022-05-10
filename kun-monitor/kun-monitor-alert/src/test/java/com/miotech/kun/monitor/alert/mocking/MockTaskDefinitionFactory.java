package com.miotech.kun.monitor.alert.mocking;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskTry;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.json.simple.JSONObject;


public class MockTaskDefinitionFactory {

    private MockTaskDefinitionFactory() {}

    public static TaskTry createTaskTry(long taskRunId) {
        long taskTryId = IdGenerator.getInstance().nextId();

        return TaskTry.newBuilder()
                .withId(taskTryId)
                .withWorkflowTaskId(WorkflowIdGenerator.nextTaskId())
                .withWorkflowTaskRunId(taskRunId)
                .withDefinitionId(IdGenerator.getInstance().nextId())
                .withTaskConfig(new JSONObject())
                .withCreator("admin")
                .build();
    }
}
