package com.miotech.kun.openapi.model.request;

import com.miotech.kun.common.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class TaskSearchRequest extends PageRequest {
    private String taskName;

    private String taskTemplateName;

    private String ownerId;

    private Long taskViewId;

    public TaskSearchRequest(Integer pageSize,
                             Integer pageNum,
                             String taskName,
                             String taskTemplateName,
                             String ownerId,
                             Long taskViewId) {
        super(pageSize, pageNum);
        this.taskName = taskName;
        this.taskTemplateName = taskTemplateName;
        this.ownerId = ownerId;
        this.taskViewId = taskViewId;
    }
}
