package com.miotech.kun.workflow.client.mock;

import com.miotech.kun.workflow.client.model.Operator;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.client.model.VariableVO;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.variable.Variable;
import com.miotech.kun.workflow.testing.operator.NopOperator;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

import java.util.ArrayList;

public class MockingFactory {
    private MockingFactory() {}

    public static final String nopOperatorClassName = NopOperator.class.getSimpleName();
    public static final String nopOperatorPath = OperatorCompiler.compileJar(NopOperator.class, nopOperatorClassName);

    public static Operator mockOperator() {
        return Operator.newBuilder()
                .withId(1L)
                .withName("test")
                .withDescription("test")
                // WARN: Using simple name instead of full qualified name to force loading from jar (not classpath)
                .withClassName(nopOperatorClassName)
                .build();
    }

    public static  Task mockTask() {
        return mockTask(1L);
    }

    public static  Task mockTask(Long operatorId) {
        return Task.newBuilder()
                .withId(1L)
                .withName("test" + WorkflowIdGenerator.nextTaskId())
                .withDescription("")
                .withConfig(Config.EMPTY)
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 15 10 * * ?"))
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .withQueueName("default")
                .withOperatorId(operatorId)
                .build();
    }

    public static  TaskRun mockTaskRun() {
        return TaskRun.newBuilder()
                .withId(1L)
                .withTask(mockTask())
                .withQueueName("default")
                .withStartAt(DateTimeUtils.now())
                .withEndAt(DateTimeUtils.now())
                .build();
    }

    public static Variable mockVariable() {
        return Variable.newBuilder()
                .withKey("test-key")
                .withNamespace("test-namespace")
                .withValue("test-value")
                .withEncrypted(false)
                .build();
    }

    public static VariableVO mockVariableVO() {
        Variable variable = mockVariable();
        return VariableVO.newBuilder()
                .withNamespace(variable.getNamespace())
                .withKey(variable.getKey())
                .withValue(variable.getValue())
                .withEncrypted(variable.isEncrypted())
                .build();
    }

    public static  String mockDeleteResponse() {
        return "{\"msg\": \"delete ok\"}";
    }
}
