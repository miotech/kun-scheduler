package com.miotech.kun.metadata.databuilder.extract.statistics;

import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.TableStatistics;
import com.miotech.kun.metadata.databuilder.constant.StatisticsMode;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetExistenceExtractor;
import com.miotech.kun.metadata.databuilder.model.DataSource;

public interface DatasetStatisticsExtractor extends DatasetExistenceExtractor, TableStatisticsExtractor, FieldStatisticsExtractor {

    default Dataset extract(Dataset dataset, DataSource dataSource, StatisticsMode statisticsMode) {
        Dataset.Builder resultBuilder = Dataset.newBuilder().withGid(dataset.getGid());

        if (statisticsMode.equals(StatisticsMode.FIELD)) {
            resultBuilder.withFieldStatistics(extractFieldStatistics(dataset, dataSource));
            resultBuilder.withTableStatistics(extractTableStatistics(dataset, dataSource));
        } else if (statisticsMode.equals(StatisticsMode.TABLE)) {
            resultBuilder.withTableStatistics(extractTableStatistics(dataset, dataSource));
        } else {
            throw new IllegalArgumentException("Invalid statisticsMode: " + statisticsMode);
        }

        return resultBuilder.build();
    }

    default TableStatistics extractTableStatistics(Dataset dataset, DataSource dataSource) {
        return TableStatistics.newBuilder()
                .withRowCount(getRowCount(dataset, dataSource))
                .withTotalByteSize(getTotalByteSize(dataset, dataSource))
                .withLastUpdatedTime(getLastUpdatedTime(dataset, dataSource))
                .withStatDate(DateTimeUtils.now())
                .build();
    }

}
