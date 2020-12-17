package com.miotech.kun.dataquality.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/16
 */
@Data
public class DataQualityCaseBasic {

    @JsonSerialize(using= ToStringSerializer.class)
    Long id;

    String name;

    List<String> types;

    String updater;

    @JsonSerialize(using= ToStringSerializer.class)
    Long taskId;

    Long createTime;

    Long updateTime;

    Boolean isPrimary;
}
