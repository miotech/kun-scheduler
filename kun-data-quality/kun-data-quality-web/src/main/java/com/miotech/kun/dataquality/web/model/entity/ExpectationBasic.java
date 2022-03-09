package com.miotech.kun.dataquality.web.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.commons.utils.CustomDateTimeDeserializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ExpectationBasic {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private List<String> types;

    private String description;

    private String updater;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long taskId;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private OffsetDateTime createTime;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private OffsetDateTime updateTime;

    private Boolean isPrimary;

    private Boolean isBlocking;

}
