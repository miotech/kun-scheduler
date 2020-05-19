package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.bo.*;
import com.miotech.kun.metadata.models.Table;

import java.util.List;

public class ArrangoExtractor implements Extractor {
    @Override
    public List<Table> extract() {
        return null;
    }

    @Override
    public List<DatasetInfo> extractDataset(DatasetExtractBO extractBO) {
        return null;
    }

    @Override
    public List<DatasetFieldInfo> extractFields(DatasetFieldExtractBO fieldExtractBO) {
        return null;
    }

    @Override
    public DatasetStatisticsInfo extractDatasetStatistics(DatasetStatisticsExtractBO statisticsExtractBO) {
        return null;
    }

    @Override
    public DatasetFieldStatisticsInfo extractDatasetFieldStatistics(DatasetFieldStatisticsExtractBO fieldStatisticsExtractBO) {
        return null;
    }
}
