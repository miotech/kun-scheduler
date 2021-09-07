package com.miotech.kun.dataplatform.mocking;

import com.miotech.kun.dataplatform.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.dataplatform.model.taskdefinition.*;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.json.simple.JSONObject;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.time.ZoneOffset;
import java.util.*;

import static com.miotech.kun.dataplatform.common.tasktemplate.dao.TaskTemplateDaoTest.TEST_TEMPLATE;

public class MockTaskDefinitionFactory {


    private MockTaskDefinitionFactory() {}

    public static TaskDefinition createTaskDefinition() {
        return createTaskDefinitions(1).get(0);
    }

    public static List<TaskDefinition> createTaskDefinitions(int num) {
        return createTaskDefinitions(num, ImmutableList.of());
    }

    public static List<TaskDefinition> createTaskDefinitions(int num, List<Long> dependencies) {
        List<TaskDefinition> tasksDefs = new ArrayList<>();
        Map<String, Object> taskConfig = new HashMap<>();

        for (int i = 0; i < num; i++) {
            long taskId = DataPlatformIdGenerator.nextTaskDefinitionId();
            long definitionId = DataPlatformIdGenerator.nextDefinitionId();
            taskConfig.put("sparkSQL", "SELECT 1 AS T");
            List<TaskDatasetProps> outputDatasets = Collections.singletonList(new TaskDatasetProps(
                    definitionId,
                    1L,
                    "TEST.test_table1"
            ));
            TaskPayload taskPayload = TaskPayload.newBuilder()
                    .withTaskConfig(taskConfig)
                    .withScheduleConfig(
                            ScheduleConfig
                                    .newBuilder()
                                    .withCronExpr("0 0 10 * * ?")
                                    .withInputNodes(dependencies)
                                    .withOutputDatasets(outputDatasets)
                                    .withType(ScheduleType.SCHEDULED.toString())
                                    .withTimeZone(ZoneOffset.UTC.getId())
                                    .withRetries(1)
                                    .withRetryDelay(30)
                                    .build())
                    .withNotifyConfig(TaskDefNotifyConfig.DEFAULT_TASK_NOTIFY_CONFIG)
                    .build();
            tasksDefs.add(TaskDefinition.newBuilder()
                    .withId(taskId)
                    .withName("taskdef_" + taskId)
                    .withDefinitionId(definitionId)
                    .withTaskTemplateName(TEST_TEMPLATE)
                    .withTaskPayload(taskPayload)
                    .withCreator(1L)
                    .withOwner(1L)
                    .withArchived(false)
                    .withLastModifier(1L)
                    .withCreateTime(DateTimeUtils.now())
                    .withUpdateTime(DateTimeUtils.now())
                    .build());
        }
        return tasksDefs;
    }

    public static TaskTry createTaskTry() {
        return createTaskTry(WorkflowIdGenerator.nextTaskId());
    }

    public static TaskTry createTaskTry(long taskRunId) {
        long taskTryId = DataPlatformIdGenerator.nextTaskTryId();

        return TaskTry.newBuilder()
                .withId(taskTryId)
                .withWorkflowTaskId(WorkflowIdGenerator.nextTaskId())
                .withWorkflowTaskRunId(taskRunId)
                .withDefinitionId(DataPlatformIdGenerator.nextDefinitionId())
                .withTaskConfig(new JSONObject())
                .withCreator(1L)
                .build();
    }
}
