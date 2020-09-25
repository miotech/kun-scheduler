package com.miotech.kun.datadashboard.model.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/9/20
 */
@Data
public class DateTimeMetrics {

    List<DateTimeTaskCount> taskCountList = new ArrayList<>();

    public void add(DateTimeTaskCount dateTimeTaskCount) {
        taskCountList.add(dateTimeTaskCount);
    }
}
