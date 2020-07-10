package com.miotech.kun.datadiscovery.model.entity;

import com.miotech.kun.datadiscovery.model.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DatasetBasicPage extends PageInfo {

    List<DatasetBasic> datasets = new ArrayList<>();

    public void add(DatasetBasic datasetBasic) {
        datasets.add(datasetBasic);
    }
}
