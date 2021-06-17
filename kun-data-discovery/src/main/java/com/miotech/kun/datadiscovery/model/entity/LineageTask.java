package com.miotech.kun.datadiscovery.model.entity;

import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/10/20
 */
@Data
public class LineageTask {
    @JsonSerialize(using = ToStringSerializer.class)
    Long taskId;

    String taskName;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    OffsetDateTime lastExecutedTime;

    List<String> historyList;
}
