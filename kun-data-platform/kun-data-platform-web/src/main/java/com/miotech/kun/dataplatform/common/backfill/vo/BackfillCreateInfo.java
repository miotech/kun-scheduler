package com.miotech.kun.dataplatform.common.backfill.vo;

import lombok.Data;

import java.util.List;

@Data
public class BackfillCreateInfo {
    private final String name;

    private final List<Long> workflowTaskIds;

    private final List<Long> taskDefinitionIds;
}
