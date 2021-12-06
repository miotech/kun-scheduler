package com.miotech.kun.datadashboard.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AbnormalDataset {

    @JsonSerialize(using= ToStringSerializer.class)
    Long datasetGid;

    String datasetName;

    String databaseName;

    String datasourceName;

    Integer failedCaseCount;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    OffsetDateTime updateTime;

    List<AbnormalTask> tasks;

    List<AbnormalCase> cases;

}
