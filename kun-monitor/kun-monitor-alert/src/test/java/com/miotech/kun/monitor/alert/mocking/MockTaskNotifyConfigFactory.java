package com.miotech.kun.monitor.alert.mocking;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.monitor.facade.model.alert.TaskNotifyConfig;
import com.miotech.kun.monitor.facade.model.alert.TaskStatusNotifyTrigger;

import java.util.ArrayList;

public class MockTaskNotifyConfigFactory {

    public static TaskNotifyConfig mockWithId(Long id) {
        return mockWithoutId().cloneBuilder().withId(id).build();
    }

    public static TaskNotifyConfig mockWithoutId() {
        Long workflowTaskId = IdGenerator.getInstance().nextId();
        return TaskNotifyConfig.newBuilder()
                .withWorkflowTaskId(workflowTaskId)
                .withTriggerType(TaskStatusNotifyTrigger.ON_FINISH)
                .withNotifierConfigs(new ArrayList<>())
                .build();
    }
}
