package com.miotech.kun.dataquality.mock;

import com.miotech.kun.dataquality.web.model.bo.MetricsRequest;
import com.miotech.kun.dataquality.web.model.bo.ValidateMetricsRequest;

public class MockValidateMetricsRequestFactory {

    private MockValidateMetricsRequestFactory() {
    }

    public static ValidateMetricsRequest create(Long datasetId, String sqlText, String field) {
        ValidateMetricsRequest vmr  = new ValidateMetricsRequest();
        MetricsRequest metricsRequest = MockMetricsRequestFactory.create(sqlText, field, datasetId);
        vmr.setMetrics(metricsRequest);

        return vmr;
    }

}
