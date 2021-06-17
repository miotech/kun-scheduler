package com.miotech.kun.datadashboard.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author: Jie Chen
 * @created: 2020/9/21
 */
@Data
public class DataDevelopmentTask {

    @JsonSerialize(using= ToStringSerializer.class)
    Long taskId;

    @JsonSerialize(using = ToStringSerializer.class)
    Long taskRunId;

    String taskName;

    String taskStatus;

    String errorMessage;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    OffsetDateTime startTime;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    OffsetDateTime endTime;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    OffsetDateTime createTime;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    OffsetDateTime updateTime;

    Long duration;
}
