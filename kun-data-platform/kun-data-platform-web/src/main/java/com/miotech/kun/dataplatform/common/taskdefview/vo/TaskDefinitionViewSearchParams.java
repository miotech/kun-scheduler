package com.miotech.kun.dataplatform.common.taskdefview.vo;

import lombok.Data;

@Data
public class TaskDefinitionViewSearchParams {
    private final String keyword;
    private final Integer pageNum;
    private final Integer pageSize;
}
