package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class Dataset extends DatasetBasic {

    @JsonProperty("low_watermark")
    private Watermark lowWatermark;

    private String database;

    @JsonProperty("row_count")
    private Long rowCount;

    private List<DataTask> flows;
}
