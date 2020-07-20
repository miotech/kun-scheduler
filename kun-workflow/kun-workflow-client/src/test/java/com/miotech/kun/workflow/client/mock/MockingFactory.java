package com.miotech.kun.workflow.client.mock;

import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.common.Param;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;

import java.util.ArrayList;
import java.util.Collections;

public class MockingFactory {
    private MockingFactory() {}


    public static Operator mockOperator() {
        return Operator.newBuilder()
                .withId(1L)
                .withName("test")
                .withDescription("test")
                .withClassName("test")
                .withPackagePath("file:/test.jar")
                .withParams(Collections.singletonList(
                        Param.newBuilder()
                                .withName("test")
                                .withDescription("test param")
                                .build()
                ))
                .build();
    }

    public static  Task mockTask() {
        return mockTask(1L);
    }

    public static  Task mockTask(Long operatorId) {
        return Task.newBuilder()
                .withId(1L)
                .withName("test")
                .withDescription("")
                .withArguments(new ArrayList<>())
                .withVariableDefs(new ArrayList<>())
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 15 10 * * ?"))
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .withOperatorId(operatorId)
                .build();
    }

    public static  TaskRun mockTaskRun() {
        return TaskRun.newBuilder()
                .withId(1L)
                .withTask(mockTask())
                .build();
    }

    public static  String mockDeleteResponse() {
        return "{\"msg\": \"delete ok\"}";
    }
}
