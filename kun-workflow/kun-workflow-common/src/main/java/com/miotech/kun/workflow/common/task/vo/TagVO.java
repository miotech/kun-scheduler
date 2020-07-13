package com.miotech.kun.workflow.common.task.vo;

import com.miotech.kun.workflow.core.model.common.Tag;

import java.util.Objects;

public class TagVO extends Tag {
    private Long taskId;

    public TagVO(Long taskId, String key, String value) {
        super(key, value);
        this.taskId = taskId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TagVO taskTagVO = (TagVO) o;
        return Objects.equals(taskId, taskTagVO.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), taskId);
    }
}
