package com.miotech.kun.dataplatform.common.taskdefview.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.kun.workflow.utils.JsonLongListFieldSerializer;
import lombok.Value;

import java.util.List;

@Value
public class UpdateTaskDefViewRequest {
    public String name;

    @JsonSerialize(using = JsonLongListFieldSerializer.class)
    public List<Long> taskDefinitionIds;
}
