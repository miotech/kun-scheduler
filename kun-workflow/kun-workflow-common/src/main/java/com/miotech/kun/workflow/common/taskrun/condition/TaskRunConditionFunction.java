package com.miotech.kun.workflow.common.taskrun.condition;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.model.common.Condition;
import com.miotech.kun.workflow.core.model.task.BlockType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.ConditionType;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunCondition;

import java.util.*;

@Singleton
public class TaskRunConditionFunction {

    @Inject
    private final TaskRunDao taskRunDao;

    @Inject
    public TaskRunConditionFunction(TaskRunDao taskRunDao) {
        this.taskRunDao = taskRunDao;
    }

    public List<TaskRunCondition> resolveTaskRunConditionForPredecessor(Task task) {
        BlockType blockType = task.getScheduleConf().getBlockType();
        if (blockType.equals(BlockType.NONE)) {
            return new ArrayList<>();
        }
        TaskRun predecessorTaskRun = taskRunDao.fetchLatestTaskRun(task.getId());
        if (predecessorTaskRun == null) {
            return new ArrayList<>();
        }
        if (blockType.equals(BlockType.WAIT_PREDECESSOR)) {
            TaskRunCondition taskRunCondition = TaskRunCondition.newBuilder()
                    .withCondition(new Condition(Collections.singletonMap("taskRunId", predecessorTaskRun.getId().toString())))
                    .withResult(predecessorTaskRun.getStatus().isTermState())
                    .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH)
                    .build();
            return new ArrayList<>(Collections.singletonList(taskRunCondition));
        } else if (blockType.equals(BlockType.WAIT_PREDECESSOR_DOWNSTREAM)) {
            List<TaskRun> predecessorWithDownstreamTaskRuns =
                    taskRunDao.fetchDownstreamTaskRunsById(predecessorTaskRun.getId(), 1, true);
            List<TaskRunCondition> taskRunConditions = new ArrayList<>();
            for (TaskRun taskRun : predecessorWithDownstreamTaskRuns) {
                TaskRunCondition taskRunCondition = TaskRunCondition.newBuilder()
                        .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun.getId().toString())))
                        .withResult(taskRun.getStatus().isTermState())
                        .withType(ConditionType.TASKRUN_PREDECESSOR_FINISH)
                        .build();
                taskRunConditions.add(taskRunCondition);
            }
            return taskRunConditions;
        } else {
            return new ArrayList<>();
        }
    }

    public List<TaskRunCondition> resolveTaskRunConditionForDependency(List<TaskRun> taskRuns) {
        if (taskRuns.isEmpty()) {
            return Collections.emptyList();
        }
        List<TaskRunCondition> taskRunConditions = new ArrayList<>();
        for (TaskRun taskRun : taskRuns) {
            TaskRunCondition taskRunCondition = TaskRunCondition.newBuilder()
                    .withCondition(new Condition(Collections.singletonMap("taskRunId", taskRun.getId().toString())))
                    .withResult(taskRun.getStatus().isSuccess())
                    .withType(ConditionType.TASKRUN_DEPENDENCY_SUCCESS)
                    .build();
            taskRunConditions.add(taskRunCondition);
        }
        return taskRunConditions;
    }
}
