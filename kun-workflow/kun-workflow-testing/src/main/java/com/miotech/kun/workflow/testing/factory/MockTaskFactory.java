package com.miotech.kun.workflow.testing.factory;

import com.miotech.kun.workflow.common.task.vo.TaskPropsVO;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.ScheduleConf;
import com.miotech.kun.workflow.core.model.task.ScheduleType;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.utils.WorkflowIdGenerator;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;

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
                .build();
    }

    public static Task createTask() {
        Long mockId = WorkflowIdGenerator.nextTaskId();
        Long mockOperatorId = WorkflowIdGenerator.nextOperatorId();
        return Task.newBuilder()
                .withId(mockId)
                .withName("task_" + mockId)
                .withDescription("task_description_" + mockId)
                .withVariableDefs(new ArrayList<>())
                .withArguments(new ArrayList<>())
                .withOperatorId(mockOperatorId)
                .withScheduleConf(new ScheduleConf(ScheduleType.NONE, null))
                .withDependencies(new ArrayList<>())
                .build();
    }
}
