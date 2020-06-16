package com.miotech.kun.datadiscover.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/6/23
 */
@Data
public class GlossaryId {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long parentId;
}
