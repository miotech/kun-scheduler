package com.miotech.kun.datadashboard.model.entity;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/9/20
 */
@Data
public class DataDevelopmentMetrics {

    Long successTaskCount;

    Long failedTaskCount;

    Long runningTaskCount;

    Long startedTaskCount;

    Long pendingTaskCount;

    Long totalTaskCount;
}
