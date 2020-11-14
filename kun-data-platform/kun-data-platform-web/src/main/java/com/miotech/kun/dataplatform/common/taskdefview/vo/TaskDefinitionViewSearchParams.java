package com.miotech.kun.dataplatform.common.taskdefview.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = TaskDefinitionViewSearchParams.TaskDefinitionViewSearchParamsBuilder.class)
public class TaskDefinitionViewSearchParams {
    String keyword;
    Integer pageNum;
    Integer pageSize;
    Long creator;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TaskDefinitionViewSearchParamsBuilder {}
}
