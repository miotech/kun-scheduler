package com.miotech.kun.metadata.databuilder.extract.impl.hive;

import com.miotech.kun.metadata.common.cataloger.Cataloger;
import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.databuilder.extract.statistics.StatisticsExtractorTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

public class HiveStatisticsExtractor extends StatisticsExtractorTemplate {

    private static final Logger logger = LoggerFactory.getLogger(HiveStatisticsExtractor.class);

    public HiveStatisticsExtractor(DataSource dataSource, Dataset dataset, Cataloger cataloger) {
        super(dataSource, dataset, cataloger);
    }

    @Override
    public OffsetDateTime getLastUpdatedTime() {
        return cataloger.getLastUpdatedTime(dataset);
    }

    @Override
    public Long getTotalByteSize() {
        return cataloger.getTotalByteSize(dataset);
    }

    @Override
    public boolean judgeExistence(Dataset dataset, DatasetExistenceJudgeMode judgeMode) {
        return cataloger.judgeExistence(dataset, judgeMode);
    }
}
