package com.miotech.kun.dataplatform.common.taskdefinition.vo;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = false)
@Data
public class TaskDefinitionSearchRequest extends PageRequest {

    private final String name;

    private final String taskTemplateName;


    private final List<Long> definitionIds;

    private final List<Long> creatorIds;

    private final Optional<Boolean> archived;

    private final List<Long> viewIds;

    public TaskDefinitionSearchRequest(Integer pageSize,
                                       Integer pageNum,
                                       String name,
                                       String taskTemplateName,
                                       List<Long> definitionIds,
                                       List<Long> creatorIds,
                                       Optional<Boolean> archived,
                                       List<Long> viewIds
    ) {
        super(pageSize, pageNum);
        this.name = name;
        this.taskTemplateName = taskTemplateName;
        this.definitionIds = definitionIds != null ? definitionIds : ImmutableList.of();
        this.creatorIds = creatorIds != null ? creatorIds : ImmutableList.of();
        this.archived = archived;
        this.viewIds = viewIds;
    }
}
