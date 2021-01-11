package com.miotech.kun.metadata.databuilder.extract.impl.glue;

import com.amazonaws.services.glue.model.Table;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.client.GlueClient;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.schema.DatasetExistenceExtractor;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import com.miotech.kun.metadata.databuilder.model.DataSource;

public class GlueExistenceExtractor implements DatasetExistenceExtractor {

    @Override
    public boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode) {
        Table table;
        switch (judgeMode) {
            case DATASET:
                table = GlueClient.searchTable((AWSDataSource) dataSource, dataset.getDatabaseName(), dataset.getName());
                break;
            case DATASOURCE:
                table = GlueClient.searchTableWithSnapshot((AWSDataSource) dataSource, dataset.getDatabaseName(), dataset.getName());
                break;
            default:
                throw new IllegalArgumentException("Invalid judgeMode: " + judgeMode);
        }

        return table != null;
    }

}
