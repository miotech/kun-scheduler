package com.miotech.kun.workflow.testing.factory;

import com.miotech.kun.commons.testing.Unsafe;
import com.miotech.kun.workflow.common.task.dependency.TaskDependencyFunctionProvider;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.task.*;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.testing.factory.MockFactoryUtils.selectItems;

public class MockTaskFactory {
    public static TaskPropsVO createTaskPropsVO() {
        Long mockOperatorId = WorkflowIdGenerator.nextOperatorId();
        return createTaskPropsVOWithOperator(mockOperatorId);
    }

    public static TaskPropsVO createTaskPropsVOWithOperator(long operatorId) {
        Long mockId = WorkflowIdGenerator.nextTaskId();
        return TaskPropsVO.newBuilder()
                .withName("task_" + mockId)
                .withDescription("task_description_" + mockId)
                .withOperatorId(operatorId)
                .withConfig(Config.EMPTY)
                .withScheduleConf(new ScheduleConf(ScheduleType.SCHEDULED, "0 0 0 * * ?", ZoneOffset.UTC.getId()))
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .withQueueName("default")
                .withPriority(0)
                .build();
    }


    public static Task createTaskWithUpstreams(List<Long> upIds, ScheduleConf conf) {
        long taskId = WorkflowIdGenerator.nextTaskId();
        TaskDependencyFunctionProvider depFuncProvider =
                Unsafe.getInjector().getInstance(TaskDependencyFunctionProvider.class);
        List<TaskDependency> dependencies = upIds.stream().map(
                upId -> new TaskDependency(upId, taskId, depFuncProvider.
                        from("latestTaskRun"))).collect(Collectors.toList());
        return Task.newBuilder()
                .withId(taskId)
                .withName("task_" + taskId)
                .withDescription("task_description_" + taskId)
                .withConfig(Config.EMPTY)
                .withOperatorId(WorkflowIdGenerator.nextOperatorId())
                .withScheduleConf(conf)
                .withDependencies(dependencies)
                .withTags(new ArrayList<>())
                .withQueueName("default")
                .withPriority(0)
                .withRecoverTimes(0)
                .withRetryDelay(1)
                .withCheckType(CheckType.SKIP)
                .build();
    }

    public static Task createTask() {
        return createTasks(1).get(0);
    }

    public static Task createTask(String queueName) {
        return createTasks(1, WorkflowIdGenerator.nextOperatorId(), queueName, 0, 1,CheckType.SKIP).get(0);
    }

    public static Task createTaskWithCheckType(CheckType checkType){
        return createTasks(1, WorkflowIdGenerator.nextOperatorId(), "default", 0, 1,checkType).get(0);
    }

    public static Task createTaskWithRetry(Integer retries, Integer retryDelay) {
        return createTasks(1, WorkflowIdGenerator.nextOperatorId(), "default", retries, retryDelay,CheckType.SKIP).get(0);
    }

    public static Task createTask(Long operatorId) {
        return createTasks(1, operatorId, "default", 0, 1,CheckType.SKIP).get(0);
    }

    public static List<Task> createTasks(int num) {
        return createTasks(num, WorkflowIdGenerator.nextOperatorId(), "default", 0, 1,CheckType.SKIP);
    }

    public static List<Task> createTasks(int num, Long operatorId) {
        return createTasks(num, operatorId, "default", 0, 1,CheckType.SKIP);
    }

    public static List<Task> createTasks(int num, Long operatorId, String queueName, Integer retries, Integer retryDelay,CheckType checkType) {
        List<Task> tasks = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            long taskId = WorkflowIdGenerator.nextTaskId();
            tasks.add(Task.newBuilder()
                    .withId(taskId)
                    .withName("task_" + taskId)
                    .withDescription("task_description_" + taskId)
                    .withConfig(Config.EMPTY)
                    .withOperatorId(operatorId)
                    .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                    .withDependencies(new ArrayList<>())
                    .withTags(new ArrayList<>())
                    .withQueueName(queueName)
                    .withPriority(0)
                    .withRetries(retries)
                    .withRetryDelay(retryDelay)
                    .withCheckType(checkType)
                    .build());
        }
        return tasks;
    }

    /**
     * 创建n个相互依赖的任务。例如"0>>1"表示创建的第1个任务依赖第0个任务（即0是上游，1是下游）。支持"0>>1;2>>1;"表示多个依赖。
     *
     * @param num
     * @param relations
     * @return
     */
    public static List<Task> createTasksWithRelations(int num, String relations) {
        return createTasksWithRelations(num, WorkflowIdGenerator.nextOperatorId(), relations);
    }

    /**
     * 创建n个相互依赖的任务。例如"0>>1"表示创建的第1个任务依赖第0个任务（即0是上游，1是下游）。支持"0>>1;2>>1;"表示多个依赖。
     *
     * @param num
     * @param operatorId
     * @param relations
     * @return
     */
    public static List<Task> createTasksWithRelations(int num, Long operatorId, String relations) {
        ScheduleConf scheduleConf = new ScheduleConf(ScheduleType.NONE, null);
        return createTasksWithRelations(num, operatorId, relations, scheduleConf);
    }

    public static List<Task> createTasksWithRelations(int num, Long operatorId, String relations, ScheduleConf scheduleConf) {
        Map<Integer, List<Integer>> parsed = MockFactoryUtils.parseRelations(relations);

        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            ids.add(WorkflowIdGenerator.nextTaskId());
        }

        TaskDependencyFunctionProvider depFuncProvider =
                Unsafe.getInjector().getInstance(TaskDependencyFunctionProvider.class);

        List<Task> tasks = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            long taskId = ids.get(i);
            tasks.add(Task.newBuilder()
                    .withId(taskId)
                    .withName("task_" + taskId)
                    .withDescription("task_description_" + taskId)
                    .withConfig(Config.EMPTY)
                    .withOperatorId(operatorId)
                    .withScheduleConf(scheduleConf)
                    .withQueueName("default")
                    .withPriority(0)
                    .withDependencies(
                            selectItems(ids, parsed.get(i)).stream()
                                    .map(upId -> new TaskDependency(upId, taskId, depFuncProvider.from("latestTaskRun")))
                                    .collect(Collectors.toList())
                    )
                    .withTags(new ArrayList<>())
                    .withRetries(0)
                    .withRetryDelay(30)
                    .withCheckType(CheckType.SKIP)
                    .build());
        }
        return tasks;
    }
}
