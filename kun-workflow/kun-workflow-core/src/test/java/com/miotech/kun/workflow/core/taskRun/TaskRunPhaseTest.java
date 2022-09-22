package com.miotech.kun.workflow.core.taskRun;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunPhase;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TaskRunPhaseTest {

    @ParameterizedTest
    @MethodSource("taskRunPhaseData")
    public void testToStatus(Integer taskRunPhase, TaskRunStatus expectedStatus){
       TaskRunStatus taskRunStatus = TaskRunPhase.toStatus(taskRunPhase);
       assertThat(taskRunStatus,is(expectedStatus));
    }

    public static Stream<Arguments> taskRunPhaseData(){
        return Stream.of(Arguments.of(TaskRunPhase.CREATED,TaskRunStatus.CREATED),
                Arguments.of(TaskRunPhase.CREATED | TaskRunPhase.WAITING,TaskRunStatus.CREATED),
                Arguments.of(TaskRunPhase.CREATED | TaskRunPhase.WAITING | TaskRunPhase.QUEUED,TaskRunStatus.QUEUED),
                Arguments.of(TaskRunPhase.CREATED | TaskRunPhase.WAITING | TaskRunPhase.QUEUED | TaskRunPhase.RUNNING,TaskRunStatus.RUNNING),
                Arguments.of(TaskRunPhase.SUCCESS,TaskRunStatus.SUCCESS),
                Arguments.of(TaskRunPhase.FAILED,TaskRunStatus.FAILED),
                Arguments.of(TaskRunPhase.ABORTED,TaskRunStatus.ABORTED),
                Arguments.of(TaskRunPhase.CHECK_FAILED,TaskRunStatus.CHECK_FAILED),
                Arguments.of(TaskRunPhase.ERROR,TaskRunStatus.ERROR),
                Arguments.of(TaskRunPhase.FAILED | TaskRunPhase.SKIP,TaskRunStatus.SKIPPED),
                Arguments.of(TaskRunPhase.ABORTED | TaskRunPhase.SKIP,TaskRunStatus.SKIPPED),
                Arguments.of(TaskRunPhase.CHECK_FAILED | TaskRunPhase.SKIP,TaskRunStatus.SKIPPED),
                Arguments.of(TaskRunPhase.SUCCESS | TaskRunPhase.SKIP,TaskRunStatus.SKIPPED),
                Arguments.of(TaskRunPhase.UPSTREAM_FAILED,TaskRunStatus.UPSTREAM_FAILED),
                Arguments.of(TaskRunPhase.BLOCKED,TaskRunStatus.BLOCKED));
    }



}
