package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.miotech.kun.commons.utils.CustomDateTimeDeserializer;
import com.miotech.kun.workflow.core.model.common.GanttChartTaskRunInfo;

import java.time.OffsetDateTime;
import java.util.List;

public class TaskRunGanttChart {

    private final List<GanttChartTaskRunInfo> infoList;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private final OffsetDateTime earliestTime;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private final OffsetDateTime latestTime;

    @JsonCreator
    public TaskRunGanttChart(@JsonProperty("infoList") List<GanttChartTaskRunInfo> infoList,
                             @JsonProperty("earliestTime") OffsetDateTime earliestTime,
                             @JsonProperty("latestTime") OffsetDateTime latestTime) {
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

    @Override
    public String toString() {
        return "TaskRunGanttChart{" +
                "infoList=" + infoList +
                ", earliestTime=" + earliestTime +
                ", latestTime=" + latestTime +
                '}';
    }
}
