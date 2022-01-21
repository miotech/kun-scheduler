package com.miotech.kun.datadiscovery.model.entity;

import com.miotech.kun.common.model.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatasetBasicPage extends PageInfo {

    List<DatasetBasic> datasets = new ArrayList<>();

    public void add(DatasetBasic datasetBasic) {
        datasets.add(datasetBasic);
    }
}
