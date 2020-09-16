package com.miotech.kun.datadashboard.model.entity;

import com.miotech.kun.common.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/9/21
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DataDevelopmentTasks extends PageInfo {

    List<DataDevelopmentTask> tasks = new ArrayList<>();

    public void add(DataDevelopmentTask task) {
        tasks.add(task);
    }

}
