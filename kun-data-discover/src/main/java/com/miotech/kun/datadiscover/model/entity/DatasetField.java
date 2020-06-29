package com.miotech.kun.datadiscover.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@Data
@NoArgsConstructor
public class DatasetField {

    private Long id;

    private String name;

    private String type;

    @JsonProperty("high_watermark")
    private Watermark highWatermark;

    private String description;

    @JsonProperty("not_null_count")
    private Long notNullCount;

    @JsonProperty("not_null_percentage")
    private Double notNullPercentage;

    @JsonProperty("distinct_count")
    private Long distinctCount;
}
