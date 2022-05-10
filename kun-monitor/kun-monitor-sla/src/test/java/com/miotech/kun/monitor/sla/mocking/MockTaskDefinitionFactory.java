package com.miotech.kun.monitor.sla.mocking;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.*;
import com.miotech.kun.dataplatform.web.common.utils.DataPlatformIdGenerator;
import com.miotech.kun.monitor.facade.model.alert.TaskDefNotifyConfig;
import com.miotech.kun.monitor.facade.model.sla.SlaConfig;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;
import org.apache.commons.compress.utils.Lists;
import org.json.simple.JSONObject;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.time.ZoneOffset;
import java.util.*;

public class MockTaskDefinitionFactory {
    public static final String TEST_TEMPLATE = "SparkSQL";
    private MockTaskDefinitionFactory() {}

    public static TaskDefinition createTaskDefinition() {
        return createTaskDefinitions(1).get(0);
    }

    public static List<TaskDefinition> createTaskDefinitions(int num) {
        return createTaskDefinitions(num, ImmutableList.of());
    }

    public static TaskDefinition createTaskDefinition(Long definitionId) {
        return createTaskDefinition(definitionId, null);
    }

    public static TaskDefinition createTaskDefinition(Long definitionId, SlaConfig slaConfig) {
        long id = IdGenerator.getInstance().nextId();
        return TaskDefinition.newBuilder()
                .withId(id)
                .withName("taskdef_" + id)
                .withDefinitionId(definitionId)
                .withTaskTemplateName(TEST_TEMPLATE)
                .withTaskPayload(createTaskPayload(definitionId, slaConfig))
                .withCreator("admin")
                .withOwner("admin")
                .withArchived(false)
                .withLastModifier("admin")
                .withCreateTime(DateTimeUtils.now())
                .withUpdateTime(DateTimeUtils.now())
                .build();
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
                                    .withSlaConfig(new SlaConfig(0, 12, 0, "0 0 12 * * ?"))
                                    .build())
                    .withNotifyConfig(TaskDefNotifyConfig.DEFAULT_TASK_NOTIFY_CONFIG)
                    .build();
            tasksDefs.add(TaskDefinition.newBuilder()
                    .withId(taskId)
                    .withName("taskdef_" + taskId)
                    .withDefinitionId(definitionId)
                    .withTaskTemplateName(TEST_TEMPLATE)
                    .withTaskPayload(taskPayload)
                    .withCreator("admin")
                    .withOwner("admin")
                    .withArchived(false)
                    .withLastModifier("admin")
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
                .withCreator("admin")
                .build();
    }

    private static TaskPayload createTaskPayload(Long definitionId) {
        return createTaskPayload(definitionId, null);
    }

    private static TaskPayload createTaskPayload(Long definitionId, SlaConfig slaConfig) {
        Map<String, Object> taskConfig = new HashMap<>();
        taskConfig.put("sparkSQL", "SELECT 1 AS T");

        List<TaskDatasetProps> outputDatasets = Collections.singletonList(new TaskDatasetProps(
                definitionId,
                1L,
                "TEST.test_table1"
        ));

        return TaskPayload.newBuilder()
                .withTaskConfig(taskConfig)
                .withScheduleConfig(
                        ScheduleConfig
                                .newBuilder()
                                .withCronExpr("0 0 10 * * ?")
                                .withInputNodes(Lists.newArrayList())
                                .withOutputDatasets(outputDatasets)
                                .withType(ScheduleType.SCHEDULED.toString())
                                .withTimeZone(ZoneOffset.UTC.getId())
                                .withRetries(1)
                                .withRetryDelay(30)
                                .withSlaConfig(slaConfig)
                                .build())
                .withNotifyConfig(TaskDefNotifyConfig.DEFAULT_TASK_NOTIFY_CONFIG)
                .build();
    }

}
