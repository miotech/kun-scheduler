package com.miotech.kun.datadiscovery.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.client.CustomDateTimeDeserializer;
import com.miotech.kun.workflow.client.CustomDateTimeSerializer;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PullProcessVO {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    Long processId;

    String processType;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    OffsetDateTime createdAt;

    PullProcessTaskRunVO latestMCETaskRun;

    PullProcessTaskRunVO latestMSETaskRun;
}
