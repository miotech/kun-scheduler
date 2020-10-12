package com.miotech.kun.workflow.operator.model;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/14
 */
public class DataQualityCase {

    List<Long> datasetIds;

    TemplateType dimension;

    String executionString;

    List<DataQualityRule> rules;

    public List<Long> getDatasetIds() {
        return datasetIds;
    }

    public void setDatasetIds(List<Long> datasetIds) {
        this.datasetIds = datasetIds;
    }

    public TemplateType getDimension() {
        return dimension;
    }

    public void setDimension(TemplateType dimension) {
        this.dimension = dimension;
    }

    public String getExecutionString() {
        return executionString;
    }

    public void setExecutionString(String executionString) {
        this.executionString = executionString;
    }

    public List<DataQualityRule> getRules() {
        return rules;
    }

    public void setRules(List<DataQualityRule> rules) {
        this.rules = rules;
    }
}
