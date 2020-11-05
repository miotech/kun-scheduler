package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/10/20
 */
@Data
public class LineageVertex {

    @JsonSerialize(using= ToStringSerializer.class)
    Long vertexId;

    Integer upstreamVertexCount;

    Integer downstreamVertexCount;

    LineageDatasetBasic datasetBasic;

}
