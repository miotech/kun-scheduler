package com.miotech.kun.metadata.databuilder.extract.statistics;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetFieldStat;
import com.miotech.kun.metadata.databuilder.model.DataSource;

import java.util.List;

public interface FieldStatisticsExtractor {

    List<DatasetFieldStat> extractFieldStatistics(Dataset dataset, DataSource dataSource);

}
