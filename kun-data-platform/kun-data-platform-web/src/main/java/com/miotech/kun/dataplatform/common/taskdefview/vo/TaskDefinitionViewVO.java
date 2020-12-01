package com.miotech.kun.dataplatform.common.taskdefview.vo;

import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TaskDefinitionViewVO {
    public static TaskDefinitionViewVO from(TaskDefinitionView view) {
        return new TaskDefinitionViewVO(
                view.getId(),
                view.getName(),
                view.getCreator(),
                view.getLastModifier(),
                view.getCreateTime(),
                view.getUpdateTime(),
                view.getIncludedTaskDefinitions().stream()
                        .map(TaskDefinition::getDefinitionId)
                        .collect(Collectors.toList())
        );
    }

    private final Long id;

    private final String name;

    private final Long creator;

    private final Long lastModifier;

    private final OffsetDateTime createTime;

    private final OffsetDateTime updateTime;

    private final List<Long> includedTaskDefinitionIds;
}
