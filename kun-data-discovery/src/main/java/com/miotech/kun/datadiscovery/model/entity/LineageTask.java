package com.miotech.kun.datadiscovery.model.entity;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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

    Long lastExecutedTime;

    List<String> historyList;
}
