package com.miotech.kun.workflow.client.mock;

import com.miotech.kun.workflow.client.model.*;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.executor.ExecutorInfo;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.variable.Variable;
import com.miotech.kun.workflow.testing.factory.MockResourceQueueFactory;
import com.miotech.kun.workflow.testing.operator.NopOperator;
import com.miotech.kun.workflow.testing.operator.OperatorCompiler;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

import java.time.ZoneOffset;
import java.util.*;

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
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 15 10 * * ?", ZoneOffset.UTC.getId()))
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .withQueueName("default")
                .withOperatorId(operatorId)
                .build();
    }

    public static  Task mockTask(Long taskId, Long operatorId) {
        return Task.newBuilder()
                .withId(taskId)
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

    public static  TaskRun mockTaskRun() {
        return TaskRun.newBuilder()
                .withId(1L)
                .withTask(mockTask())
                .withQueueName("default")
                .withStartAt(DateTimeUtils.now())
                .withEndAt(DateTimeUtils.now())
                .build();
    }

    public static  TaskRun mockTaskRun(Long taskRunId) {
        return TaskRun.newBuilder()
                .withId(taskRunId)
                .withTask(mockTask(taskRunId, 1L))
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

    public static ExecutorInfo mockExecutorInfo(String name) {
        ResourceQueue resourceQueue = MockResourceQueueFactory.createResourceQueue();
        return ExecutorInfo.newBuilder()
                .withKind(name)
                .withName(name)
                .withLabels(Arrays.asList("label1", "label2"))
                .withResourceQueues(Collections.singletonList(resourceQueue))
                .build();
    }

    public static ExecutorInfo mockDispatchExecutorInfo(ExecutorInfo... executorInfos) {
        Map<String, Object> extraInfo = new HashMap<>();
        for (ExecutorInfo executorInfo : executorInfos) {
            extraInfo.put(executorInfo.getName(), executorInfo);
        }
        return ExecutorInfo.newBuilder()
                .withKind("dispatch")
                .withName("dispatch")
                .withExtraInfo(extraInfo)
                .build();
    }

    public static  String mockDeleteResponse() {
        return "{\"msg\": \"delete ok\"}";
    }
}
