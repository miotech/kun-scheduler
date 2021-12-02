package com.miotech.kun.datadashboard.model.entity.datadevelopment;

import lombok.Data;

import java.util.List;

@Data
public class StatisticChartResult {
    List<DailyStatistic> dailyStatisticList;

    public StatisticChartResult(List<DailyStatistic> dailyStatisticList) {
        this.dailyStatisticList = dailyStatisticList;
    }
}
