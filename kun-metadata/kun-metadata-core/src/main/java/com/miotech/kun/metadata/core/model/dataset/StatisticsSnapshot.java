package com.miotech.kun.metadata.core.model.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class StatisticsSnapshot {

    private final TableStatistics tableStatistics;

    private final List<FieldStatistics> fieldStatistics;

    @JsonCreator
    public StatisticsSnapshot(@JsonProperty("tableStatistics") TableStatistics tableStatistics,
                              @JsonProperty("fieldStatistics") List<FieldStatistics> fieldStatistics) {
        this.tableStatistics = tableStatistics;
        this.fieldStatistics = fieldStatistics;
    }

    public TableStatistics getTableStatistics() {
        return tableStatistics;
    }

    public List<FieldStatistics> getFieldStatistics() {
        return fieldStatistics;
    }
}
