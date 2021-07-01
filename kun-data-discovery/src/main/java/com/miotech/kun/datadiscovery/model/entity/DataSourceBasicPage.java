package com.miotech.kun.datadiscovery.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceBasicPage {

    List<DatasourceBasic> datasources = new ArrayList<>();

    public void add(DatasourceBasic datasourceBasic) {
        datasources.add(datasourceBasic);
    }
}
