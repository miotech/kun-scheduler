package com.miotech.kun.datadashboard.model.entity;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/9/20
 */
@Data
public class DataDevelopmentMetrics {

    Integer successTaskCount;

    Integer failedTaskCount;

    Integer runningTaskCount;

    Integer startedTaskCount;

    Integer pendingTaskCount;

    Integer totalTaskCount;
}
