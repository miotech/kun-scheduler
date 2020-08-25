package com.miotech.kun.dataplatform.common.tasktemplate.vo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.miotech.kun.dataplatform.model.tasktemplate.ParameterDefinition;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TaskTemplateReqeustVO {

    private final String name;

    private final String templateType;

    private final String templateGroup;

    private final Long operatorId;

    private final List<ParameterDefinition> displayParameters;

    private final Map<String, Object> defaultValues;

    private final String renderClassName;

    public TaskTemplateReqeustVO(String name,
                                 String templateType,
                                 String templateGroup,
                                 Long operatorId,
                                 List<ParameterDefinition> displayParameters,
                                 Map<String, Object> defaultValues,
                                 String renderClassName) {
        this.name = name;
        this.templateType = templateType;
        this.templateGroup = templateGroup;
        this.operatorId = operatorId;
        this.displayParameters = displayParameters == null ? ImmutableList.of() : displayParameters;
        this.defaultValues = defaultValues == null ? ImmutableMap.of() : defaultValues;
        this.renderClassName = renderClassName;
    }
}