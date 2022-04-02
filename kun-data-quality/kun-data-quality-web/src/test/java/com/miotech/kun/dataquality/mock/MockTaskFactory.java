package com.miotech.kun.dataquality.mock;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

import java.time.ZoneOffset;
import java.util.ArrayList;

public class MockTaskFactory {

    private MockTaskFactory() {
    }

    public static Task create() {
        long operatorId = IdGenerator.getInstance().nextId();
        return Task.newBuilder()
                .withId(1L)
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
