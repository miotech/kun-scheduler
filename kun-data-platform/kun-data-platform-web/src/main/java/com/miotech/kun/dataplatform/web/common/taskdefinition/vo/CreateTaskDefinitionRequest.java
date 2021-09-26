package com.miotech.kun.dataplatform.web.common.taskdefinition.vo;

import lombok.Data;

@Data
public class CreateTaskDefinitionRequest {

    private final String name;

    private final String taskTemplateName;
}
