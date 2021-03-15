package com.miotech.kun.datadashboard.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.datadashboard.model.bo.ColumnMetricsRequest;
import com.miotech.kun.datadashboard.model.bo.RowCountChangeRequest;
import com.miotech.kun.datadashboard.model.bo.TestCasesRequest;
import com.miotech.kun.datadashboard.model.entity.ColumnMetricsList;
import com.miotech.kun.datadashboard.model.entity.DataQualityCases;
import com.miotech.kun.datadashboard.model.entity.DatasetRowCountChanges;
import com.miotech.kun.datadashboard.model.entity.MetadataMetrics;
import com.miotech.kun.datadashboard.persistence.DataQualityRepository;
import com.miotech.kun.datadashboard.persistence.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author: Jie Chen
 * @created: 2020/9/15
 */
@Service
public class MetadataService {

    private final static String ROW_COUNT_CHANGES_FORMAT = "row_count_change:page_number=%d:page_size=%d";
    private final static String COLUMN_METRICS_FORMAT = "column_metrics:page_number=%d:page_size=%d";
    private final static String METADATA_METRICS = "metadata_metrics";
    private final static Cache<String, Object> localCache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    @Autowired
    DataQualityRepository dataQualityRepository;

    @Autowired
    MetadataRepository metadataRepository;

    public MetadataMetrics getMetadataMetrics() {
        try {
            return (MetadataMetrics) localCache.get(METADATA_METRICS, () -> {
                MetadataMetrics metrics = new MetadataMetrics();
                metrics.setTotalCaseCount(dataQualityRepository.getTotalCaseCount());
                metrics.setTotalDatasetCount(metadataRepository.getTotalDatasetCount());
                metrics.setDataQualityCoveredCount(dataQualityRepository.getCoveredDatasetCount());
                metrics.setDataQualityLongExistingFailedCount(dataQualityRepository.getLongExistingCount());
                metrics.setDataQualityPassCount(dataQualityRepository.getSuccessCount());

                return metrics;
            });
        } catch (ExecutionException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public DatasetRowCountChanges getRowCountChange(RowCountChangeRequest rowCountChangeRequest) {
        String key = String.format(ROW_COUNT_CHANGES_FORMAT, rowCountChangeRequest.getPageNumber(), rowCountChangeRequest.getPageSize());
        try {
            return (DatasetRowCountChanges) localCache.get(key, () -> metadataRepository.getRowCountChange(rowCountChangeRequest));
        } catch (ExecutionException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public DataQualityCases getTestCases(TestCasesRequest testCasesRequest) {
        return dataQualityRepository.getTestCases(testCasesRequest);
    }

    public ColumnMetricsList getColumnMetricsList(ColumnMetricsRequest columnMetricsRequest) {
        String key = String.format(COLUMN_METRICS_FORMAT, columnMetricsRequest.getPageNumber(), columnMetricsRequest.getPageSize());
        try {
            return (ColumnMetricsList) localCache.get(key, () -> metadataRepository.getColumnMetricsList(columnMetricsRequest));
        } catch (ExecutionException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }
}
