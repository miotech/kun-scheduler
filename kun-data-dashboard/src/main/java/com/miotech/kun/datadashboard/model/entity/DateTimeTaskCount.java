package com.miotech.kun.datadashboard.model.entity;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author: Jie Chen
 * @created: 2020/9/20
 */
@Data
public class DateTimeTaskCount {

    /* Epoch Unix timestamp in milliseconds */
    OffsetDateTime time;

    Integer taskCount;
}
