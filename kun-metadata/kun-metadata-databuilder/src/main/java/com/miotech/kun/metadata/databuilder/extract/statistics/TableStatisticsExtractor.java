package com.miotech.kun.metadata.databuilder.extract.statistics;

import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.databuilder.model.DataSource;

import java.time.LocalDateTime;

public interface TableStatisticsExtractor {

    default LocalDateTime getLastUpdatedTime(Dataset dataset, DataSource dataSource) {
        return null;
    }

    default Long getRowCount(Dataset dataset, DataSource dataSource) {
        return null;
    }

    default Long getTotalByteSize(Dataset dataset, DataSource dataSource) {
        return null;
    }

}
