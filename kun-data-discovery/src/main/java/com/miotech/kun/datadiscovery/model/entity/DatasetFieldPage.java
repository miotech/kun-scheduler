package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.datadiscovery.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/28
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DatasetFieldPage extends PageInfo {

    @JsonProperty("columns")
    private List<DatasetField> datasetFields = new ArrayList<>();

    public void add(DatasetField datasetField) {
        datasetFields.add(datasetField);
    }
}
