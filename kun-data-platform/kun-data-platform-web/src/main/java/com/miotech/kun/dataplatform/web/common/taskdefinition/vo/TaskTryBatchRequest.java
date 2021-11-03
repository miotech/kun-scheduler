package com.miotech.kun.dataplatform.web.common.taskdefinition.vo;

import com.google.common.collect.ImmutableList;
import lombok.Data;

import java.util.List;

@Data
public class TaskTryBatchRequest {

    private List<Long> idList;

    public TaskTryBatchRequest() {

    }

    public TaskTryBatchRequest(List<Long> idList) {
        this.idList = idList != null? idList : ImmutableList.of();
    }
}
