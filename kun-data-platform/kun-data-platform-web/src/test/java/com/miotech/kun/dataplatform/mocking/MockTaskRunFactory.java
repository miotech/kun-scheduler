package com.miotech.kun.dataplatform.mocking;

import com.google.common.collect.Lists;
import com.miotech.kun.workflow.client.model.Task;
import com.miotech.kun.workflow.client.model.TaskDependency;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class MockTaskRunFactory {
    private MockTaskRunFactory() {}

    public static TaskRun createTaskRun(Task task) {
        return createTaskRuns(Collections.singletonList(task)).get(task.getId());
    }

    public static Map<Long, TaskRun> createTaskRuns(List<Task> tasks) {
        Map<Long, TaskRun> taskIdToTaskRunMap = new HashMap<>();

        List<Long> ids = Lists.newArrayList();
        for (int i = 0; i < tasks.size(); i++) {
            ids.add(WorkflowIdGenerator.nextTaskRunId());
        }

        for (int i = 0; i < tasks.size(); i++) {

            List<Long> dependencyTaskRunIds = tasks.get(i).getDependencies().stream()
                    .map(TaskDependency::getUpstreamTaskId)
                    .map(x -> taskIdToTaskRunMap.get(x).getId())
                    .collect(Collectors.toList());

            TaskRun tr = TaskRun.newBuilder()
                    .withId(ids.get(i))
                    .withTask(tasks.get(i))
                    .withConfig(tasks.get(i).getConfig())
                    .withScheduleType(tasks.get(i).getScheduleConf().getType().toString())
                    .withDependencyTaskRunIds(dependencyTaskRunIds)
                    .withStartAt(null)
                    .withEndAt(null)
                    .withQueueName(tasks.get(i).getQueueName())
                    .build();
            taskIdToTaskRunMap.put(tasks.get(i).getId(), tr);
        }

        return taskIdToTaskRunMap;
    }

}

