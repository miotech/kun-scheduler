package com.miotech.kun.dataplatform.common.taskdefview.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.kun.workflow.utils.JsonLongListFieldSerializer;
import lombok.Data;

import java.util.List;

@Data
public class UpdateTaskDefViewRelationRequest {
    @JsonSerialize(using = JsonLongListFieldSerializer.class)
    private List<Long> taskDefinitionIds;
}
