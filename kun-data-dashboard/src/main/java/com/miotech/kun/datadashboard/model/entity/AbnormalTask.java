package com.miotech.kun.datadashboard.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AbnormalTask {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long taskId;

    private String taskName;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long taskRunId;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime updateTime;

}
