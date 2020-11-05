package com.miotech.kun.datadiscovery.model.entity;

import lombok.Data;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/10/20
 */
@Data
public class LineageTask {

    Long taskId;

    String taskName;

    Long lastExecutedTime;

    List<String> historyList;
}
