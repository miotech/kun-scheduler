package com.miotech.kun.workflow.operator.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.HttpApiClient;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.datasource.DataSource;

import static com.miotech.kun.workflow.operator.DataQualityConfiguration.INFRA_BASE_URL;

@Singleton
public class DataSourceClient extends HttpApiClient {

    private final Props props;
    private final String infraBaseUrl;

    @Inject
    public DataSourceClient(Props props) {
        this.props = props;
        this.infraBaseUrl = props.getString(INFRA_BASE_URL);
    }

    @Override
    public String getBase() {
        return infraBaseUrl;
    }

    public DataSource getDataSourceById(Long dataSourceId) {
        String body = get(infraBaseUrl + "/datasource/" + dataSourceId);
        return JSONUtils.jsonToObject(body, DataSource.class);
    }

    public Long getDataSourceIdByGid(Long gid) {
        String body = get(infraBaseUrl + "/dataset/basic/" + gid);
        return JSONUtils.jsonToObject(body, Dataset.class).getDatasourceId();
    }
}
