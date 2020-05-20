package com.miotech.kun.metadata.extract.impl;

import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.PostgresDatabase;
import com.miotech.kun.metadata.model.bo.*;
import com.miotech.kun.metadata.models.Table;

import java.util.ArrayList;
import java.util.List;

public class PostgresExtractor extends JDBCExtractor {

    private PostgresDatabase postgresDatabase;

    private PostgresExtractor() {};

    public PostgresExtractor(PostgresDatabase postgresDatabase) {
        this.postgresDatabase = postgresDatabase;
    }

    @Override
    public List<Dataset> extract() {
        List<Table> tables = new ArrayList<>();
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
