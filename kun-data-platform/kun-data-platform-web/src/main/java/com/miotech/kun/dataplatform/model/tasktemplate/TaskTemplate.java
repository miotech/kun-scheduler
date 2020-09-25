package com.miotech.kun.dataplatform.model.tasktemplate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.miotech.kun.workflow.client.model.Operator;

import java.util.List;
import java.util.Map;

@JsonDeserialize(builder = TaskTemplate.Builder.class)
public class TaskTemplate {

    private final String name;

    private final String templateType;

    private final String templateGroup;

    private final Operator operator;

    private final String jarPath;

    private final Map<String, Object> defaultValues;

    private final List<ParameterDefinition> displayParameters;

    private final String renderClassName;

    public TaskTemplate(String name, String templateType, String templateGroup, Operator operator,
                        String jarPath, Map<String, Object> defaultValues,
                        List<ParameterDefinition> displayParameters,
                        String renderClassName) {
        this.name = name;
        this.templateType = templateType;
        this.templateGroup = templateGroup;
        this.operator = operator;
        this.jarPath = jarPath;
        this.defaultValues = defaultValues;
        this.displayParameters = displayParameters;
        this.renderClassName = renderClassName;
    }

    public String getName() {
        return name;
    }

    public String getTemplateType() {
        return templateType;
    }

    public String getTemplateGroup() {
        return templateGroup;
    }

    public String getJarPath() { return jarPath; }

    public Operator getOperator() {
        return operator;
    }

    public Map<String, Object> getDefaultValues() {
        return defaultValues;
    }

    public List<ParameterDefinition> getDisplayParameters() {
        return displayParameters;
    }

    public String getRenderClassName() {
        return renderClassName;
    }

    public static Builder newBuilder() { return new Builder(); }

    public Builder cloneBuilder() {
        return newBuilder()
                .withName(name)
                .withTemplateType(templateType)
                .withTemplateGroup(templateGroup)
                .withOperator(operator)
                .withDefaultValues(defaultValues)
                .withDisplayParameters(displayParameters)
                .withRenderClassName(renderClassName);
    }

    public static final class Builder {
        private String name;
        private String templateType;
        private String templateGroup;
        private String jarPath;
        private Operator operator;
        private Map<String, Object> defaultValues;
        private List<ParameterDefinition> displayParameters;
        private String renderClassName;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withTemplateType(String templateType) {
            this.templateType = templateType;
            return this;
        }

        public Builder withTemplateGroup(String templateGroup) {
            this.templateGroup = templateGroup;
            return this;
        }

        public Builder withJarPath(String jarPath) {
            this.jarPath = jarPath;
            return this;
        }

        public Builder withOperator(Operator operator) {
            this.operator = operator;
            return this;
        }

        public Builder withDefaultValues(Map<String, Object> defaultValues) {
            this.defaultValues = defaultValues;
            return this;
        }

        public Builder withDisplayParameters(List<ParameterDefinition> displayParameters) {
            this.displayParameters = displayParameters;
            return this;
        }

        public Builder withRenderClassName(String renderClassName) {
            this.renderClassName = renderClassName;
            return this;
        }

        public TaskTemplate build() {
            return new TaskTemplate(name, templateType, templateGroup, operator, jarPath, defaultValues, displayParameters, renderClassName);
        }
    }
}
