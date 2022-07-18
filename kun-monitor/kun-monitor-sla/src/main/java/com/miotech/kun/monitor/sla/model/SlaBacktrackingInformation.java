package com.miotech.kun.monitor.sla.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SlaBacktrackingInformation {

    private Integer avgTaskRunTimeLastSevenTimes;

    private BacktrackingTaskDefinition backtrackingTaskDefinition;

}
