package com.miotech.kun.dataquality.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/7/16
 */
@Data
public class DataQualityCaseBasic {

    @JsonSerialize(using= ToStringSerializer.class)
    Long id;

    String name;

    String updater;
}
