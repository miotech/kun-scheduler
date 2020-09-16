package com.miotech.kun.datadashboard.model.entity;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/9/21
 */
@Data
public class DataDevelopmentTask {

    String taskName;

    String taskStatus;

    String errorMessage;

    Long startTime;

    Long endTime;

    Long duration;
}
