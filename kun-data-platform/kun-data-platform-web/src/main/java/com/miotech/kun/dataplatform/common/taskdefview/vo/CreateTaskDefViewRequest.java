package com.miotech.kun.dataplatform.common.taskdefview.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.kun.workflow.utils.JsonLongListFieldSerializer;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = CreateTaskDefViewRequest.CreateTaskDefViewRequestBuilder.class)
public class CreateTaskDefViewRequest {
    String name;

    @JsonSerialize(using = JsonLongListFieldSerializer.class)
    @Builder.Default
    List<Long> taskDefIds = new ArrayList<>();

    @JsonPOJOBuilder(withPrefix = "")
    public static class CreateTaskDefViewRequestBuilder {
    }
}
