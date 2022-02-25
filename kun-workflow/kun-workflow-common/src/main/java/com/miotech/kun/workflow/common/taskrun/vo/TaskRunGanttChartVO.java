package com.miotech.kun.workflow.common.taskrun.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.core.model.common.GanttChartTaskRunInfo;

import java.time.OffsetDateTime;
import java.util.List;

public class TaskRunGanttChartVO {

    private final List<GanttChartTaskRunInfo> infoList;

    @JsonSerialize(using= ToStringSerializer.class)
    private final OffsetDateTime earliestTime;

    @JsonSerialize(using= ToStringSerializer.class)
    private final OffsetDateTime latestTime;

    public TaskRunGanttChartVO(List<GanttChartTaskRunInfo> infoList, OffsetDateTime earliestTime, OffsetDateTime latestTime) {
        this.infoList = infoList;
        this.earliestTime = earliestTime;
        this.latestTime = latestTime;
    }

    public List<GanttChartTaskRunInfo> getInfoList() {
        return infoList;
    }

    public OffsetDateTime getEarliestTime() {
        return earliestTime;
    }

    public OffsetDateTime getLatestTime() {
        return latestTime;
    }
}
