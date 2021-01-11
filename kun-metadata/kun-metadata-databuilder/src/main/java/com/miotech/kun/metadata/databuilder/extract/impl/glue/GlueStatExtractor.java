package com.miotech.kun.metadata.databuilder.extract.impl.glue;

import com.amazonaws.services.glue.model.Table;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.client.GlueClient;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.template.DataWarehouseStatTemplate;
import com.miotech.kun.metadata.databuilder.extract.stat.StatExtractorTemplate;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import com.miotech.kun.metadata.databuilder.model.DataSource;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class GlueStatExtractor extends StatExtractorTemplate {

    @Override
    public DataWarehouseStatTemplate buildDataWarehouseStatTemplate(Dataset dataset, DataSource dataSource) throws SQLException, ClassNotFoundException {
        AWSDataSource awsDataSource = (AWSDataSource) dataSource;
        return new DataWarehouseStatTemplate(dataset.getDatabaseName(), null,
                dataset.getName(), DatabaseType.ATHENA, awsDataSource);
    }

    @Override
    public LocalDateTime getLastUpdatedTime(Dataset dataset, DataSource dataSource) {
        Table table = GlueClient.searchTable((AWSDataSource) dataSource, dataset.getDatabaseName(), dataset.getName());
        return table == null ? null : table.getUpdateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode) {
        GlueExistenceExtractor glueExistenceExtractor = new GlueExistenceExtractor();
        return glueExistenceExtractor.judgeExistence(dataset, dataSource, DatasetExistenceJudgeMode.DATASET);
    }
}
