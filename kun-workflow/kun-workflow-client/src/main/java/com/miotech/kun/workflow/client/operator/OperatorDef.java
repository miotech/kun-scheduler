package com.miotech.kun.workflow.client.operator;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.miotech.kun.workflow.client.model.Operator;

import java.util.Objects;

@JsonSerialize
class OperatorDef {
    private Long id;
    private String name;
    private String className;
    private String description;


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

    public void setClassName(String className) {
        this.className = className;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getClassName() {
        return className;
    }

    public String getOperatorVer() {
        return "0";
    }

    public static OperatorDef fromOperator(Operator operator) {
        OperatorDef operatorDef = new OperatorDef();
        operatorDef.setId(operator.getId());
        operatorDef.setName(operator.getName());
        operatorDef.setClassName(operator.getClassName());
        operatorDef.setDescription(operator.getDescription());
        return operatorDef;
    }

    public Operator toOperator() {
        return Operator.newBuilder()
                .withId(this.id)
                .withName(this.name)
                .withClassName(this.className)
                .withDescription(this.description)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OperatorDef)) return false;
        OperatorDef that = (OperatorDef) o;
        return Objects.equals(getId(), that.getId()) &&
                getName().equals(that.getName()) &&
                getClassName().equals(that.getClassName()) &&
                getDescription().equals(that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getClassName(), getDescription());
    }

    @Override
    public String toString() {
        return "OperatorDef{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", className='" + className + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
