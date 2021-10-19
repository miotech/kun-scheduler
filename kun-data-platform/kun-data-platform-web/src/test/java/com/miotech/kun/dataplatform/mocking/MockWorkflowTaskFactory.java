package com.miotech.kun.dataplatform.mocking;

import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

import java.time.ZoneOffset;
import java.util.ArrayList;

public class MockWorkflowTaskFactory {

    private MockWorkflowTaskFactory() {
    }

    public static Task mockTask(Long operatorId) {
        return Task.newBuilder()
                .withId(WorkflowIdGenerator.nextTaskId())
                .withName("test" + WorkflowIdGenerator.nextTaskId())
                .withDescription("")
                .withConfig(Config.EMPTY)
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 15 10 * * ?", ZoneOffset.UTC.getId()))
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .withQueueName("default")
                .withOperatorId(operatorId)
                .build();
    }

}
