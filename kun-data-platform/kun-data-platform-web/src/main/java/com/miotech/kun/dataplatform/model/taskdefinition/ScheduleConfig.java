package com.miotech.kun.dataplatform.model.taskdefinition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.TimeZoneEnum;

import java.util.List;

@JsonDeserialize(builder = ScheduleConfig.Builder.class)
public class ScheduleConfig {

    private final String type;

    private final String cronExpr;

    private final List<Long> inputNodes;

    private final List<TaskDatasetProps> inputDatasets;

    private final List<TaskDatasetProps> outputDatasets;

    private final TimeZoneEnum timeZoneEnum;

    private ScheduleConfig(String type,
                           String cronExpr,
                           TimeZoneEnum timeZoneEnum,
                           List<Long> inputNodes,
                           List<TaskDatasetProps> inputDatasets,
                           List<TaskDatasetProps> outputDatasets) {
        this.type = type;
        this.cronExpr = cronExpr;
        this.timeZoneEnum = timeZoneEnum;
        this.inputNodes = inputNodes == null ? ImmutableList.of() : inputNodes;
        this.inputDatasets = inputDatasets == null ? ImmutableList.of() : inputDatasets;
        this.outputDatasets = outputDatasets == null ? ImmutableList.of() : outputDatasets;
    }

    public String getType() {
        return type;
    }

    public String getCronExpr() {
        return cronExpr;
    }

    public List<Long> getInputNodes() {
        return inputNodes;
    }

    public List<TaskDatasetProps> getInputDatasets() {
        return inputDatasets;
    }

    public List<TaskDatasetProps> getOutputDatasets() {
        return outputDatasets;
    }

    public TimeZoneEnum getTimeZoneEnum() {
        return timeZoneEnum;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String type;
        private String cronExpr;
        private List<Long> inputNodes;
        private List<TaskDatasetProps> inputDatasets;
        private List<TaskDatasetProps> outputDatasets;
        private TimeZoneEnum timeZoneEnum;

        private Builder() {
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withCronExpr(String cronExpr) {
            this.cronExpr = cronExpr;
            return this;
        }

        public Builder withInputNodes(List<Long> inputNodes) {
            this.inputNodes = inputNodes;
            return this;
        }

        public Builder withInputDatasets(List<TaskDatasetProps> inputDatasets) {
            this.inputDatasets = inputDatasets;
            return this;
        }

        public Builder withOutputDatasets(List<TaskDatasetProps> outputDatasets) {
            this.outputDatasets = outputDatasets;
            return this;
        }

        public Builder withTimeZoneEnum(TimeZoneEnum timeZoneEnum) {
            this.timeZoneEnum = timeZoneEnum;
            return this;
        }

        public ScheduleConfig build() {
            return new ScheduleConfig(type, cronExpr, timeZoneEnum, inputNodes, inputDatasets, outputDatasets);
        }
    }
}
