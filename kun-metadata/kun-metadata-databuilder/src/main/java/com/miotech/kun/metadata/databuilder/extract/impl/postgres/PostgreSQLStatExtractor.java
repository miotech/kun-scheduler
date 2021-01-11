package com.miotech.kun.metadata.databuilder.extract.impl.postgres;

import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.constant.DatasetExistenceJudgeMode;
import com.miotech.kun.metadata.databuilder.extract.template.DataWarehouseStatTemplate;
import com.miotech.kun.metadata.databuilder.extract.stat.StatExtractorTemplate;
import com.miotech.kun.metadata.databuilder.model.DataSource;
import com.miotech.kun.metadata.databuilder.model.PostgresDataSource;

import java.sql.SQLException;

public class PostgreSQLStatExtractor extends StatExtractorTemplate {

    @Override
    public DataWarehouseStatTemplate buildDataWarehouseStatTemplate(Dataset dataset, DataSource dataSource) throws SQLException, ClassNotFoundException {
        PostgresDataSource postgresDataSource = (PostgresDataSource) dataSource;
        String dbName = dataset.getDatabaseName().split("\\.")[0];
        String schemaName = dataset.getDatabaseName().split("\\.")[1];
        return new DataWarehouseStatTemplate(dbName, schemaName,
                dataset.getName(), DatabaseType.POSTGRES, postgresDataSource);
    }

    @Override
    public boolean judgeExistence(Dataset dataset, DataSource dataSource, DatasetExistenceJudgeMode judgeMode) {
        PostgreSQLExistenceExtractor postgreSQLExistenceExtractor = new PostgreSQLExistenceExtractor();
        return postgreSQLExistenceExtractor.judgeExistence(dataset, dataSource, DatasetExistenceJudgeMode.DATASET);
    }
}
