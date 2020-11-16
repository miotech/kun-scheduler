package com.miotech.kun.dataplatform.common.taskdefview.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.model.taskdefview.TaskDefinitionView;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = TaskDefinitionViewCreateInfoVO.TaskDefinitionViewCreateInfoVOBuilder.class)
public class TaskDefinitionViewCreateInfoVO {
    public static TaskDefinitionViewCreateInfoVO from(TaskDefinitionView view) {
        return new TaskDefinitionViewCreateInfoVO(
                view.getName(),
                view.getCreator(),
                view.getIncludedTaskDefinitions().stream()
                        .map(TaskDefinition::getDefinitionId)
                        .collect(Collectors.toList())
        );
    }

    String name;

    Long creator;

    List<Long> includedTaskDefinitionIds;

    @JsonPOJOBuilder
    public static class TaskDefinitionViewCreateInfoVOBuilder {}
}
