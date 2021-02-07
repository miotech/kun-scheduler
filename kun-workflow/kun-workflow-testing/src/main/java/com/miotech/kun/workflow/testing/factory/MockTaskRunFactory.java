package com.miotech.kun.workflow.testing.factory;

import com.google.common.collect.Lists;
import com.miotech.kun.workflow.core.execution.Config;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.miotech.kun.workflow.testing.factory.MockFactoryUtils.parseRelations;
import static com.miotech.kun.workflow.testing.factory.MockFactoryUtils.selectItems;

public class MockTaskRunFactory {
    private static final Tick tick = new Tick(OffsetDateTime.of(
            2020, 5, 1, 0, 0, 0, 0, ZoneOffset.of("+08:00")
    ));

    public static TaskRun createTaskRun() {
        return createTaskRun(MockTaskFactory.createTask());
    }

    public static TaskRun createTaskRun(Task task) {
        return createTaskRuns(new Task[]{task}).get(0);
    }

    public static TaskRun createTaskRunWithTick(Task task, Tick tick) {
        return TaskRun.newBuilder()
                .withId(WorkflowIdGenerator.nextTaskRunId())
                .withTask(task)
                .withInlets(new ArrayList<>())
                .withOutlets(new ArrayList<>())
                .withScheduleType(task.getScheduleConf().getType())
                .withDependentTaskRunIds(new ArrayList<>())
                .withScheduledTick(tick)
                .withStartAt(DateTimeUtils.now())
                .withEndAt(DateTimeUtils.now())
                .withStatus(TaskRunStatus.CREATED)
                .withConfig(Config.EMPTY)
                .build();
    }

    public static List<TaskRun> createTaskRuns(Task... tasks) {
        return createTaskRuns(Arrays.asList(tasks));
    }

    public static List<TaskRun> createTaskRuns(List<Task> tasks) {
        return createTaskRunsWithRelations(tasks, "");
    }

    public static List<TaskRun> createTaskRunsWithRelations(List<Task> tasks, String relations) {
        return createTaskRunsWithRelationsAndTick(tasks, relations, tick);
    }


    public static List<TaskRun> createTaskRunsWithRelationsAndTick(List<Task> tasks, String relations, Tick tick) {
        List<TaskRun> taskRuns = new ArrayList<>();

        List<Long> ids = Lists.newArrayList();
        for (int i = 0; i < tasks.size(); i++) {
            ids.add(WorkflowIdGenerator.nextTaskRunId());
        }

        Map<Integer, List<Integer>> edges = parseRelations(relations);

        for (int i = 0; i < tasks.size(); i++) {
            TaskRun tr = TaskRun.newBuilder()
                    .withId(ids.get(i))
                    .withTask(tasks.get(i))
                    .withConfig(Config.EMPTY)
                    .withScheduleType(tasks.get(i).getScheduleConf().getType())
                    .withDependentTaskRunIds(selectItems(ids, edges.get(i)))
                    .withInlets(Collections.emptyList())
                    .withOutlets(Collections.emptyList())
                    .withStatus(null)
                    .withScheduledTick(tick)
                    .withStartAt(null)
                    .withEndAt(null)
                    .build();
            taskRuns.add(tr);
        }

        return taskRuns;
    }

    public static TaskRun createTaskRun(Long id, Task task) {
        return TaskRun.newBuilder()
                .withId(id)
                .withTask(task)
                .withInlets(new ArrayList<>())
                .withOutlets(new ArrayList<>())
                .withScheduleType(task.getScheduleConf().getType())
                .withDependentTaskRunIds(new ArrayList<>())
                .withScheduledTick(new Tick(DateTimeUtils.now()))
                .withStartAt(DateTimeUtils.now())
                .withEndAt(DateTimeUtils.now())
                .withStatus(TaskRunStatus.QUEUED)
                .withConfig(Config.EMPTY)
                .build();
    }

    public static TaskAttempt createTaskAttempt(Long id, TaskRun taskRun, int attempt) {
        return TaskAttempt.newBuilder()
                .withId(id)
                .withTaskRun(taskRun)
                .withAttempt(attempt)
                .withStartAt(DateTimeUtils.now())
                .withEndAt(DateTimeUtils.now().plusHours(1))
                .withLogPath("/var/log_" + taskRun + "_" + attempt + ".log")
                .withStatus(TaskRunStatus.RUNNING)
                .build();
    }
}
