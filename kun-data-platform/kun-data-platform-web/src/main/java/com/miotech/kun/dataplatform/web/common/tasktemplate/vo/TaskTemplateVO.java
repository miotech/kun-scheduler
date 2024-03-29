package com.miotech.kun.dataplatform.web.common.tasktemplate.vo;

import com.miotech.kun.dataplatform.web.model.tasktemplate.ParameterDefinition;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TaskTemplateVO {

    private final String name;

    private final String templateType;

    private final String templateGroup;

    private final List<ParameterDefinition> displayParameters;

    private final Map<String, Object> defaultValues;

    private final String renderClassName;
}
