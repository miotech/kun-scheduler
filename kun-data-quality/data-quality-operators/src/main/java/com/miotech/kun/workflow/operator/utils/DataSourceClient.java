package com.miotech.kun.workflow.operator.utils;

import com.miotech.kun.commons.utils.HttpApiClient;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.metadata.core.model.datasource.DataSource;

public class DataSourceClient extends HttpApiClient {

    private final String infraBaseUrl;

    public DataSourceClient(String infraBaseUrl) {
        this.infraBaseUrl = infraBaseUrl;
    }

    @Override
    public String getBase() {
        return infraBaseUrl;
    }

    public DataSource getDataSourceById(Long dataSourceId) {
        String body = get(infraBaseUrl + "/datasource/" + dataSourceId);
        return JSONUtils.jsonToObject(body, DataSource.class);
    }
}
