package com.miotech.kun.dataplatform.common.deploy.vo;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class DeployedTaskSearchRequest extends PageRequest {

    private List<Long> definitionIds;

    private List<Long> ownerIds;

    private String taskTemplateName;

    private String name;

    private List<Long> workflowTaskIds;

    public DeployedTaskSearchRequest(Integer pageSize,
                                     Integer pageNum,
                                     List<Long> definitionIds,
                                     List<Long> ownerIds,
                                     String taskTemplateName,
                                     String name,
                                     List<Long> workflowTaskIds) {
        super(pageSize, pageNum);
        this.definitionIds = definitionIds != null ? definitionIds : ImmutableList.of();
        this.ownerIds = ownerIds != null ? ownerIds : ImmutableList.of();
        this.taskTemplateName = taskTemplateName;
        this.workflowTaskIds = workflowTaskIds;
        this.name = name;
    }
}

