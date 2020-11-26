package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

@JsonDeserialize(builder = SplineSource.SplineSourceBuilder.class)
public class SplineSource {
    private final String sourceName;
    private final String sourceType;

    public SplineSource(String sourceName, String sourceType) {
        this.sourceName = sourceName;
        this.sourceType = sourceType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public static SplineSourceBuilder newBuilder(){
        return new SplineSourceBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplineSource)) return false;
        SplineSource that = (SplineSource) o;
        return Objects.equals(getSourceName(), that.getSourceName()) &&
                Objects.equals(getSourceType(), that.getSourceType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSourceName(), getSourceType());
    }

    @Override
    public String toString() {
        return "SplineSource{" +
                "sourceName='" + sourceName + '\'' +
                ", sourceType='" + sourceType + '\'' +
                '}';
    }

    @JsonPOJOBuilder
    public static final class SplineSourceBuilder {
        private String sourceName;
        private String sourceType;


        public SplineSourceBuilder withSourceName(String sourceName) {
            this.sourceName = sourceName;
            return this;
        }

        public SplineSourceBuilder withSourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public SplineSource build() {
            return new SplineSource(sourceName, sourceType);
        }
    }
}
