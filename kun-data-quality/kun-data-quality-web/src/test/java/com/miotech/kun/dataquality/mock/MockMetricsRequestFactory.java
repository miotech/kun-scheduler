package com.miotech.kun.dataquality.mock;

import com.google.common.collect.Maps;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.web.model.bo.MetricsRequest;

import java.util.Map;

public class MockMetricsRequestFactory {

    private MockMetricsRequestFactory() {
    }

    public static MetricsRequest create() {
        String sql = "select count(1) c from test";
        String field = "c";
        long datasetGid = IdGenerator.getInstance().nextId();
        return create(sql, field, datasetGid);
    }

    public static MetricsRequest create(Map<String, Object> collectRule) {
        long datasetGid = IdGenerator.getInstance().nextId();
        return create(datasetGid, collectRule);
    }

    public static MetricsRequest create(String sql, String field) {
        long datasetGid = IdGenerator.getInstance().nextId();
        return create(sql, field, datasetGid);
    }

    public static MetricsRequest create(String sql, String field, Long datasetGid) {
        MetricsRequest metricsRequest = new MetricsRequest();

        Map<String, Object> payload = Maps.newHashMap();
        payload.put("sql", sql);
        payload.put("field", field);
        payload.put("collectMethod", "CUSTOM_SQL");
        metricsRequest.setDatasetGid(datasetGid);
        metricsRequest.setPayload(payload);

        return metricsRequest;
    }

    public static MetricsRequest create(Long datasetGid, Map<String, Object> payload) {
        MetricsRequest metricsRequest = new MetricsRequest();

        metricsRequest.setDatasetGid(datasetGid);
        metricsRequest.setPayload(payload);

        return metricsRequest;
    }

}
