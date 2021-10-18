package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

@JsonDeserialize(builder = SplineSource.SplineSourceBuilder.class)
public class SplineSource {
    private final String sourceName;
    private final String sourceType;
    private final TableInfo tableInfo;

    public SplineSource(String sourceName, String sourceType ,TableInfo tableInfo) {
        this.sourceName = sourceName;
        this.sourceType = sourceType;
        this.tableInfo = tableInfo;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public static SplineSourceBuilder newBuilder(){
        return new SplineSourceBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplineSource)) return false;
        SplineSource that = (SplineSource) o;
        return getSourceName().equals(that.getSourceName()) &&
                getSourceType().equals(that.getSourceType()) &&
                Objects.equals(getTableInfo(), that.getTableInfo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSourceName(), getSourceType(), getTableInfo());
    }

    @Override
    public String toString() {
        return "SplineSource{" +
                "sourceName='" + sourceName + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", tableInfo=" + tableInfo +
                '}';
    }

    @JsonPOJOBuilder
    public static final class SplineSourceBuilder {
        private String sourceName;
        private String sourceType;
        private TableInfo tableInfo;


        public SplineSourceBuilder withSourceName(String sourceName) {
            this.sourceName = sourceName;
            return this;
        }

        public SplineSourceBuilder withSourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public SplineSourceBuilder withTableInfo(TableInfo tableInfo){
            this.tableInfo = tableInfo;
            return this;
        }

        public SplineSource build() {
            return new SplineSource(sourceName, sourceType,tableInfo);
        }
    }
}
