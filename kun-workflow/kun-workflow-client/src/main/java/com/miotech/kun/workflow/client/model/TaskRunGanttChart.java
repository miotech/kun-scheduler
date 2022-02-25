package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.workflow.core.model.common.GanttChartTaskRunInfo;

import java.util.List;

public class TaskRunGanttChart {

    private final List<GanttChartTaskRunInfo> infoList;

    @JsonCreator
    public TaskRunGanttChart(@JsonProperty("infoList") List<GanttChartTaskRunInfo> infoList) {
        this.infoList = infoList;
    }

    public List<GanttChartTaskRunInfo> getInfoList() {
        return infoList;
    }


    @Override
    public String toString() {
        return "TaskRunGanttChart{" +
                "infoList=" + infoList +
                '}';
    }
}
