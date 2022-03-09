package com.miotech.kun.dataquality.mock;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.web.model.bo.MetricsRequest;

public class MockMetricsRequestFactory {

    private MockMetricsRequestFactory() {
    }

    public static MetricsRequest create() {
        String sql = "select count(1) c from test";
        String field = "c";
        long datasetGid = IdGenerator.getInstance().nextId();
        return create(sql, field, datasetGid);
    }

    public static MetricsRequest create(String sql, String field) {
        long datasetGid = IdGenerator.getInstance().nextId();
        return create(sql, field, datasetGid);
    }

    public static MetricsRequest create(String sql, String field, Long datasetGid) {
        MetricsRequest metricsRequest = new MetricsRequest();
        metricsRequest.setSql(sql);
        metricsRequest.setField(field);
        metricsRequest.setDatasetGid(datasetGid);
        metricsRequest.setGranularity(Metrics.Granularity.CUSTOM.name());

        return metricsRequest;
    }

}
