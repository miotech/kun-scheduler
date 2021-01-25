package com.miotech.kun.dataplatform.common.backfill.vo;

import lombok.Data;

import java.util.List;

@Data
public class BackfillCreateInfo {
    private final String name;

    private final Long creator;

    private final List<Long> taskRunIds;

    private final List<Long> taskDefinitionIds;
}
