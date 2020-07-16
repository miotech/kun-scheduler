package com.miotech.kun.workflow.client.mock;

import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;

import java.util.ArrayList;

public class MockingFactory {
    private MockingFactory() {}

    private static final String nopOperatorClassName = NopOperator.class.getSimpleName();
    private static final String nopOperatorPath = OperatorCompiler.compileJar(NopOperator.class, nopOperatorClassName);

    public static Operator mockOperator() {
        return Operator.newBuilder()
                .withId(1L)
                .withName("test")
                .withDescription("test")
                .withClassName(nopOperatorClassName)
                .withPackagePath(nopOperatorPath)
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
                .withConfig(Config.EMPTY)
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
