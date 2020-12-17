package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonDeserialize(builder = ExecPlan.ExecPlanBuilder.class)
public class ExecPlan {
    private final List<SplineSource> inputSources;//输入源
    private final SplineSource outputSource;//输出源
    private final String taskName;//任务名


    public ExecPlan(List<SplineSource> inputSources, SplineSource outputSource, String taskName) {
        this.inputSources = inputSources;
        this.outputSource = outputSource;
        this.taskName = taskName;
    }

    public List<SplineSource> getInputSources() {
        return inputSources;
    }

    public SplineSource getOutputSource() {
        return outputSource;
    }

    public String getTaskName() {
        return taskName;
    }

    public static ExecPlanBuilder newBuilder(){
        return new ExecPlanBuilder();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecPlan)) return false;
        ExecPlan execPlan = (ExecPlan) o;
        return Objects.equals(getInputSources(), execPlan.getInputSources()) &&
                Objects.equals(getOutputSource(), execPlan.getOutputSource()) &&
                getTaskName().equals(execPlan.getTaskName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInputSources(), getOutputSource(), getTaskName());
    }

    @Override
    public String toString() {
        return "ExecPlan{" +
                "inputSources=" + inputSources +
                ", outputSource=" + outputSource +
                ", taskName='" + taskName + '\'' +
                '}';
    }

    @JsonPOJOBuilder
    public static final class ExecPlanBuilder {
        private List<SplineSource> inputSources;//输入源
        private SplineSource outputSource;//输出源
        private String taskName;//任务名

        private ExecPlanBuilder() {
        }

        public static ExecPlanBuilder anExecPlan() {
            return new ExecPlanBuilder();
        }

        public ExecPlanBuilder withInputSources(List<SplineSource> inputSources) {
            this.inputSources = inputSources != null ? inputSources : new ArrayList<>();
            return this;
        }

        public ExecPlanBuilder withOutputSource(SplineSource outputSource) {
            this.outputSource = outputSource;
            return this;
        }

        public ExecPlanBuilder withTaskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public ExecPlan build() {
            return new ExecPlan(inputSources, outputSource, taskName);
        }
    }
}
