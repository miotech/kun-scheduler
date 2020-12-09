package com.miotech.kun.datadashboard.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/9/17
 */
@Data
public class DatasetBasic {

    @JsonSerialize(using= ToStringSerializer.class)
    Long gid;

    String datasetName;

    String database;

    String dataSource;
}
