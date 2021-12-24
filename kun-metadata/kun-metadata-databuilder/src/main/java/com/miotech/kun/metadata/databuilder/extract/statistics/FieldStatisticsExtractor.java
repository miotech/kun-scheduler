package com.miotech.kun.metadata.databuilder.extract.statistics;

import com.miotech.kun.metadata.core.model.dataset.FieldStatistics;

import java.util.List;

public interface FieldStatisticsExtractor {

    List<FieldStatistics> extractFieldStatistics();

}
