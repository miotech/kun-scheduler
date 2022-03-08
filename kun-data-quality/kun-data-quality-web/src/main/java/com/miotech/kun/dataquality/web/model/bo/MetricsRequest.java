package com.miotech.kun.dataquality.web.model.bo;

import com.miotech.kun.dataquality.core.expectation.Dataset;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.core.metrics.SQLMetrics;
import lombok.Data;

@Data
public class MetricsRequest {

    private String granularity = Metrics.Granularity.CUSTOM.name();

    private String sql;

    private String field;

    private Long datasetGid;

    public Metrics convertTo(String name, String description, Dataset dataset) {
        return SQLMetrics.newBuilder()
                .withName(name)
                .withDescription(description)
                .withGranularity(Metrics.Granularity.valueOf(this.granularity))
                .withDataset(dataset)
                .withSql(this.sql)
                .withField(this.field)
                .build();
    }

    public static MetricsRequest convertFrom(Metrics metrics) {
        if (!(metrics instanceof SQLMetrics)) {
            throw new IllegalArgumentException("Invalid metrics type: " + metrics.getMetricsType());
        }

        SQLMetrics sqlMetrics = (SQLMetrics) metrics;
        MetricsRequest metricsRequest = new MetricsRequest();
        metricsRequest.setSql(sqlMetrics.getSql());
        metricsRequest.setField(sqlMetrics.getField());
        metricsRequest.setDatasetGid(sqlMetrics.getDataset().getGid());

        return metricsRequest;
    }

}
