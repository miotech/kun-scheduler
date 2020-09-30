package com.miotech.kun.datadashboard.service;

import com.miotech.kun.datadashboard.model.bo.ColumnMetricsRequest;
import com.miotech.kun.datadashboard.model.bo.RowCountChangeRequest;
import com.miotech.kun.datadashboard.model.bo.TestCasesRequest;
import com.miotech.kun.datadashboard.model.entity.*;
import com.miotech.kun.datadashboard.persistence.DataQualityRepository;
import com.miotech.kun.datadashboard.persistence.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Jie Chen
 * @created: 2020/9/15
 */
@Service
public class MetadataService {

    @Autowired
    DataQualityRepository dataQualityRepository;

    @Autowired
    MetadataRepository metadataRepository;

    public MetadataMetrics getMetadataMetrics() {
        MetadataMetrics metrics = new MetadataMetrics();
        metrics.setTotalCaseCount(dataQualityRepository.getTotalCaseCount());
        metrics.setTotalDatasetCount(metadataRepository.getTotalDatasetCount());
        metrics.setDataQualityCoveredCount(dataQualityRepository.getCoveredDatasetCount());
        metrics.setDataQualityLongExistingFailedCount(dataQualityRepository.getLongExistingCount());
        metrics.setDataQualityPassCount(dataQualityRepository.getSuccessCount());
        return metrics;
    }

    public DatasetRowCountChanges getRowCountChange(RowCountChangeRequest rowCountChangeRequest) {
        return metadataRepository.getRowCountChange(rowCountChangeRequest);
    }

    public TestCases getTestCases(TestCasesRequest testCasesRequest) {
        return dataQualityRepository.getTestCases(testCasesRequest);
    }

    public ColumnMetricsList getColumnMetricsList(ColumnMetricsRequest columnMetricsRequest) {
        return metadataRepository.getColumnMetricsList(columnMetricsRequest);
    }
}
