package com.miotech.kun.dataplatform.web.common.backfill.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BackfillCreateInfo {

    private final String name;

    private final List<Long> workflowTaskIds;

    private final List<Long> taskDefinitionIds;

    private String cronExpr;

    private String timeZone;
}
