package com.miotech.kun.datadashboard.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/9/21
 */
@Data
public class DataDevelopmentTask {

    @JsonSerialize(using= ToStringSerializer.class)
    Long taskId;

    String taskName;

    String taskStatus;

    String errorMessage;

    Long startTime;

    Long endTime;

    Long duration;
}
