package com.miotech.kun.dataplatform.web.common.taskdefview.vo;

import com.miotech.kun.common.model.PageResult;

import java.util.List;
import java.util.Objects;

public class TaskDefViewPageResult extends PageResult<TaskDefinitionViewVO> {

    private final Integer allTasksCount;

    private final Integer danglingTasksCount;


    public TaskDefViewPageResult(Integer pageSize, Integer pageNumber, Integer totalCount, List<TaskDefinitionViewVO> records,
                                 Integer allTasksCount, Integer danglingTasksCount) {
        super(pageSize, pageNumber, totalCount, records);
        this.allTasksCount = allTasksCount;
        this.danglingTasksCount = danglingTasksCount;
    }

    public Integer getAllTasksCount() {
        return allTasksCount;
    }

    public Integer getDanglingTasksCount() {
        return danglingTasksCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TaskDefViewPageResult that = (TaskDefViewPageResult) o;
        return allTasksCount.equals(that.allTasksCount) && danglingTasksCount.equals(that.danglingTasksCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), allTasksCount, danglingTasksCount);
    }
}
