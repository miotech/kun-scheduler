package com.miotech.kun.dataplatform.facade.model.taskdefinition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.monitor.facade.model.sla.SlaConfig;

import java.util.List;

@JsonDeserialize(builder = ScheduleConfig.Builder.class)
public class ScheduleConfig {

    private final String type;

    private final String cronExpr;

    private final List<Long> inputNodes;

    private final List<TaskDatasetProps> inputDatasets;

    private final List<TaskDatasetProps> outputDatasets;

    private final String timeZone;

    //task retry times limit
    private final Integer retries;

    //task retry delay,unit seconds
    private final Integer retryDelay;

    private final SlaConfig slaConfig;

    private final String blockType;

    private final String executorLabel;

    private ScheduleConfig(String type,
                           String cronExpr,
                           String timeZone,
                           List<Long> inputNodes,
                           List<TaskDatasetProps> inputDatasets,
                           List<TaskDatasetProps> outputDatasets,
                           Integer retries,
                           Integer retryDelay,
                           SlaConfig slaConfig,
                           String blockType,
                           String executorLabel) {
        this.type = type;
        this.cronExpr = cronExpr;
        this.timeZone = timeZone;
        this.inputNodes = inputNodes == null ? ImmutableList.of() : inputNodes;
        this.inputDatasets = inputDatasets == null ? ImmutableList.of() : inputDatasets;
        this.outputDatasets = outputDatasets == null ? ImmutableList.of() : outputDatasets;
        this.retries = retries;
        this.retryDelay = retryDelay;
        this.slaConfig = slaConfig;
        this.blockType = blockType;
        this.executorLabel = executorLabel;
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

    public String getTimeZone() {
        return timeZone;
    }

    public Integer getRetries() {
        return retries;
    }

    public Integer getRetryDelay() {
        return retryDelay;
    }

    public SlaConfig getSlaConfig() {
        return slaConfig;
    }

    public String getBlockType() {
        return blockType;
    }

    public String getExecutorLabel() {
        return executorLabel;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return new Builder()
                .withType(type)
                .withBlockType(blockType)
                .withInputNodes(inputNodes)
                .withInputDatasets(inputDatasets)
                .withOutputDatasets(outputDatasets)
                .withCronExpr(cronExpr)
                .withRetries(retries)
                .withRetryDelay(retryDelay)
                .withTimeZone(timeZone)
                .withSlaConfig(slaConfig)
                .withExecutorLabel(executorLabel);
    }

    public static final class Builder {
        private String type;
        private String cronExpr;
        private List<Long> inputNodes;
        private List<TaskDatasetProps> inputDatasets;
        private List<TaskDatasetProps> outputDatasets;
        private String timeZone;
        private Integer retries;
        private Integer retryDelay;
        private SlaConfig slaConfig;
        private String blockType;
        private String executorLabel;

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

        public Builder withTimeZone(String timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        public Builder withRetries(Integer retries) {
            this.retries = retries;
            return this;
        }

        public Builder withRetryDelay(Integer retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public Builder withSlaConfig(SlaConfig slaConfig) {
            this.slaConfig = slaConfig;
            return this;
        }

        public Builder withBlockType(String blockType) {
            this.blockType = blockType;
            return this;
        }

        public Builder withExecutorLabel(String executorLabel) {
            this.executorLabel = executorLabel;
            return this;
        }

        public ScheduleConfig build() {
            return new ScheduleConfig(type, cronExpr, timeZone, inputNodes, inputDatasets, outputDatasets, retries,
                    retryDelay, slaConfig, blockType, executorLabel);
        }
    }

}
