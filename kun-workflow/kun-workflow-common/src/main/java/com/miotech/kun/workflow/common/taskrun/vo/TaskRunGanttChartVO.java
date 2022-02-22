package com.miotech.kun.workflow.common.taskrun.vo;

import com.miotech.kun.workflow.core.model.common.GanttChartTaskRunInfo;

import java.util.List;

public class TaskRunGanttChartVO {

    private final List<GanttChartTaskRunInfo> infoList;

    public TaskRunGanttChartVO(List<GanttChartTaskRunInfo> infoList) {
        this.infoList = infoList;
    }

    public List<GanttChartTaskRunInfo> getInfoList() {
        return infoList;
    }
}
