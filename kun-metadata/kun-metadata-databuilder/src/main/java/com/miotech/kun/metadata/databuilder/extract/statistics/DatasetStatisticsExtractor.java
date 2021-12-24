package com.miotech.kun.metadata.databuilder.extract.statistics;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.core.model.constant.StatisticsMode;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.TableStatistics;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetExistenceExtractor;

public interface DatasetStatisticsExtractor extends DatasetExistenceExtractor, TableStatisticsExtractor, FieldStatisticsExtractor {


    Dataset extract(StatisticsMode statisticsMode);

    default TableStatistics extractTableStatistics() {
        return TableStatistics.newBuilder()
                .withRowCount(getRowCount())
                .withTotalByteSize(getTotalByteSize())
                .withLastUpdatedTime(getLastUpdatedTime())
                .withStatDate(DateTimeUtils.now())
                .build();
    }

}
