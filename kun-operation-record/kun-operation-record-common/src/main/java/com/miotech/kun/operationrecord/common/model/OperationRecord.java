package com.miotech.kun.operationrecord.common.model;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class OperationRecord {

    private Long id;

    private String operator;

    private String type;

    private String event;

    private String status;

    private OffsetDateTime createTime;

    private OffsetDateTime updateTime;

}
