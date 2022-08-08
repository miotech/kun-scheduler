package com.miotech.kun.dataquality.web.model.bo;

import lombok.Data;

import java.util.Map;

@Data
public class MetricsRequest {

    private Map<String, Object> payload;

    private Long datasetGid;

}
