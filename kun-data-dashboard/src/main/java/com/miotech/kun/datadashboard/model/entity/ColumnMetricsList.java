package com.miotech.kun.datadashboard.model.entity;

import com.miotech.kun.common.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/9/19
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ColumnMetricsList extends PageInfo {

    List<ColumnMetrics> columnMetricsList = new ArrayList<>();

    public void add(ColumnMetrics columnMetrics) {
        columnMetricsList.add(columnMetrics);
    }
}
