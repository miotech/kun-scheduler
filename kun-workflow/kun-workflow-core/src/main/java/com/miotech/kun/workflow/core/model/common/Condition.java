package com.miotech.kun.workflow.core.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public class Condition {
    private Map<String, String> content;

    @JsonCreator
    public Condition(@JsonProperty("content") Map<String, String> content) {
        this.content = content;
    }

    public Map<String, String> getContent() {
        return content;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Condition{" +
                "content=" + content.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Condition condition = (Condition) o;
        return content.equals(condition.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
}
