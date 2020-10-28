package com.miotech.kun.datadiscovery.model.entity;

import com.miotech.kun.common.model.PageInfo;
import com.miotech.kun.workflow.client.model.TaskRun;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/10/20
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class LineageTasks {

    List<LineageTask> tasks = new ArrayList<>();

    public void add(LineageTask lineageTask) {
        tasks.add(lineageTask);
    }
}
