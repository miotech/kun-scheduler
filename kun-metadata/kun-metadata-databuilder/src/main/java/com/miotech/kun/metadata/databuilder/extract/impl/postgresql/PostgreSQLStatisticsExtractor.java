package com.miotech.kun.metadata.databuilder.extract.impl.postgresql;

import com.miotech.kun.metadata.common.cataloger.Cataloger;
import com.miotech.kun.metadata.core.model.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.databuilder.extract.statistics.StatisticsExtractorTemplate;

public class PostgreSQLStatisticsExtractor extends StatisticsExtractorTemplate {


    public PostgreSQLStatisticsExtractor(DataSource dataSource, Dataset dataset, Cataloger cataloger) {
        super(dataSource, dataset,cataloger);
    }

    @Override
    public Long getTotalByteSize() {
        return cataloger.getTotalByteSize(dataset);
    }

    @Override
    public boolean judgeExistence(Dataset dataset, DatasetExistenceJudgeMode judgeMode) {
        return cataloger.judgeExistence(dataset, DatasetExistenceJudgeMode.DATASET);
    }
}
