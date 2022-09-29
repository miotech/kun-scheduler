package com.miotech.kun.openapi.model.response;

import com.miotech.kun.dataplatform.web.model.taskdefview.TaskDefinitionView;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class TaskViewDetailVO {
    private final Long id;
    private final String taskViewName;
    private final List<TaskInViewDetailVO> taskList;

    public static TaskViewDetailVO from(TaskDefinitionView taskDefinitionView) {
        return new TaskViewDetailVO(taskDefinitionView.getId(), taskDefinitionView.getName(),
                taskDefinitionView.getIncludedTaskDefinitions().stream()
                        .map(TaskInViewDetailVO::from)
                        .collect(Collectors.toList()));
    }
}
