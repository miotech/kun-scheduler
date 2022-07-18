package com.miotech.kun.monitor.sla.model;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class BacktrackingTaskDefinition {

    private Long definitionId;

    private String definitionName;

    private OffsetDateTime deadline;

    private Integer priority;

}
