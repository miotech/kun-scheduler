package com.miotech.kun.metadata.web.model.vo;

import com.miotech.kun.common.model.PageInfo;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */

public class DataSourcePage extends PageInfo {

    private List<DataSource> datasources;

    public DataSourcePage() {
    }

    public List<DataSource> getDatasources() {
        return datasources;
    }

    public void setDatasources(List<DataSource> datasources) {
        this.datasources = datasources;
    }
}
