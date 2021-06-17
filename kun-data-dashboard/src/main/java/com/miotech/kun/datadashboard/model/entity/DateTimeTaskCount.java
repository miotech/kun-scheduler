package com.miotech.kun.datadashboard.model.entity;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/9/20
 */
@Data
public class DateTimeTaskCount {

    /* Epoch Unix timestamp in milliseconds */
    Double time;

    Integer taskCount;
}
