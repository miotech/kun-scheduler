package com.miotech.kun.datadashboard.model.entity;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/9/15
 */
@Data
public class MetadataMetrics {

    Long dataQualityCoveredCount;

    Long dataQualityLongExistingFailedCount;

    Long dataQualityPassCount;

    Long totalCaseCount;

    Long totalDatasetCount;

}
