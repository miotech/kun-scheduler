package com.miotech.kun.dataplatform.common.tasktemplate.vo;

import com.miotech.kun.dataplatform.model.tasktemplate.ParameterDefinition;
import lombok.Data;

import java.util.List;

@Data
public class TaskTemplateVO {

    private final String name;

    private final String templateType;

    private final String templateGroup;

    private final List<ParameterDefinition> displayParameters;
}
