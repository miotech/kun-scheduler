package com.miotech.kun.dataplatform.common.taskdefview.vo;

import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class TaskDefinitionCreateInfoVO {
    public static TaskDefinitionCreateInfoVO from(TaskDefinitionView view) {
        return new TaskDefinitionCreateInfoVO(
                view.getName(),
                view.getCreator(),
                view.getIncludedTaskDefinitions().stream()
                        .map(TaskDefinition::getDefinitionId)
                        .collect(Collectors.toList())
        );
    }

    private final String name;

    private final Long creator;

    private final List<Long> includedTaskDefinitionIds;
}
