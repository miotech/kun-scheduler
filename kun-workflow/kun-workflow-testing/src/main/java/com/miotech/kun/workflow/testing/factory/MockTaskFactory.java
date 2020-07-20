package com.miotech.kun.workflow.testing.factory;

import com.miotech.kun.commons.testing.Unsafe;
import com.miotech.kun.workflow.common.task.dependency.TaskDependencyFunctionProvider;
import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskDependency;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.testing.factory.MockFactoryUtils.selectItems;

public class MockTaskFactory {
    public static TaskPropsVO createTaskPropsVO() {
        Long mockId = WorkflowIdGenerator.nextTaskId();
        Long mockOperatorId = WorkflowIdGenerator.nextOperatorId();
        return TaskPropsVO.newBuilder()
                .withName("task_" + mockId)
                .withDescription("task_description_" + mockId)
                .withVariableDefs(new ArrayList<>())
                .withArguments(new ArrayList<>())
                .withOperatorId(mockOperatorId)
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .withDependencies(new ArrayList<>())
                .withTags(new ArrayList<>())
                .build();
    }

    public static Task createTask() {
        return createTasks(1).get(0);
    }

    public static List<Task> createTasks(int num) {
        List<Task> tasks = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            long taskId = WorkflowIdGenerator.nextTaskId();
            tasks.add(Task.newBuilder()
                    .withId(taskId)
                    .withName("task_" + taskId)
                    .withDescription("task_description_" + taskId)
                    .withVariableDefs(new ArrayList<>())
                    .withArguments(new ArrayList<>())
                    .withOperatorId(WorkflowIdGenerator.nextOperatorId())
                    .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                    .withDependencies(new ArrayList<>())
                    .withTags(new ArrayList<>())
                    .build());
        }
        return tasks;
    }

    /**
     * 创建n个相互依赖的任务。例如"0>>1"表示创建的第1个任务依赖第0个任务（即0是上游，1是下游）。支持"0>>1;2>>1;"表示多个依赖。
     * @param num
     * @param relations
     * @return
     */
    public static List<Task> createTasksWithRelations(int num, String relations) {
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
                    .withVariableDefs(new ArrayList<>())
                    .withArguments(new ArrayList<>())
                    .withOperatorId(WorkflowIdGenerator.nextOperatorId())
                    .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                    .withDependencies(
                            selectItems(ids, parsed.get(i)).stream()
                                    .map(upId -> new TaskDependency(upId, taskId, depFuncProvider.from("latestTaskRun")))
                                    .collect(Collectors.toList())
                    )
                    .withTags(new ArrayList<>())
                    .build());
        }
        return tasks;
    }
}
