package com.miotech.kun.common.utils;

import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 2020/10/24
 */
public class WorkflowUtils {

    public static String resolveTaskStatus(TaskRunStatus taskRunStatus) {
        if (taskRunStatus.isSuccess()) {
            return TaskRunStatus.SUCCESS.name();
        } else if (taskRunStatus.isFailure()) {
            return TaskRunStatus.FAILED.name();
        } else if (taskRunStatus.isSkipped()) {
            return TaskRunStatus.SKIPPED.name();
        } else {
            return "";
        }
    }

    public static List<String> resolveTaskHistory(List<TaskRun> taskRuns) {
        return taskRuns.stream()
                .map(taskRun -> WorkflowUtils.resolveTaskStatus(taskRun.getStatus()))
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }
}
