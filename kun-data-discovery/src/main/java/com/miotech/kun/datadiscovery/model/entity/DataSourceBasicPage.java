package com.miotech.kun.datadiscovery.model.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/22
 */
@Data
public class DataSourceBasicPage {

    List<DatasourceBasic> datasources = new ArrayList<>();

    public void add(DatasourceBasic datasourceBasic) {
        datasources.add(datasourceBasic);
    }
}
