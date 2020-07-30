package com.miotech.kun.workflow.common.task.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.core.execution.ConfigDef;

public class OperatorVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String name;

    private String description;

    private String className;

    private ConfigDef configDef;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public ConfigDef getConfigDef() {
        return configDef;
    }

    public void setConfigDef(ConfigDef configDef) {
        this.configDef = configDef;
    }
}
