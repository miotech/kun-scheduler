package com.miotech.kun.metadata.databuilder.extract.statistics;

import com.miotech.kun.metadata.common.cataloger.Cataloger;
import com.miotech.kun.metadata.core.model.constant.StatisticsMode;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.FieldStatistics;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.databuilder.extract.template.DataWarehouseStatTemplate;

import java.util.List;
import java.util.stream.Collectors;

public abstract class StatisticsExtractorTemplate implements DatasetStatisticsExtractor {

    protected final DataSource dataSource;
    protected final Dataset dataset;
    protected final Cataloger cataloger;
    private final DataWarehouseStatTemplate statTemplate;

    public StatisticsExtractorTemplate(DataSource dataSource,Dataset dataset,Cataloger cataloger) {
        this.dataSource = dataSource;
        this.dataset = dataset;
        this.statTemplate = buildDataWarehouseStatTemplate(dataSource,dataset);
        this.cataloger = cataloger;
    }

    private DataWarehouseStatTemplate buildDataWarehouseStatTemplate(DataSource dataSource,Dataset dataset){
        ConnectionType connectionType = dataSource.getConnectionConfig().getDataConnection().getConnectionType();
        switch (connectionType){
            case HIVE_SERVER:
            case ATHENA:
                return new DataWarehouseStatTemplate(dataset.getDatabaseName(),null,dataset.getName(),dataSource);
            case POSTGRESQL:
                String dbName = dataset.getDatabaseName().split("\\.")[0];
                String schemaName = dataset.getDatabaseName().split("\\.")[1];
                return new DataWarehouseStatTemplate(dbName, schemaName,
                        dataset.getName(),  dataSource);
            default: throw new IllegalStateException(connectionType + " warehouse is not support yet");

        }
    }

    public Dataset extract(StatisticsMode statisticsMode) {
        Dataset.Builder resultBuilder = Dataset.newBuilder().withGid(dataset.getGid());

        if (statisticsMode.equals(StatisticsMode.FIELD)) {
            resultBuilder.withFieldStatistics(extractFieldStatistics());
            resultBuilder.withTableStatistics(extractTableStatistics());
        } else if (statisticsMode.equals(StatisticsMode.TABLE)) {
            resultBuilder.withTableStatistics(extractTableStatistics());
        } else {
            throw new IllegalArgumentException("Invalid statisticsMode: " + statisticsMode);
        }

        return resultBuilder.build();
    }

    @Override
    public Long getRowCount() {
        return statTemplate.getRowCount(dataSource.getConnectionConfig().getDataConnection().getConnectionType());
    }

    @Override
    public List<FieldStatistics> extractFieldStatistics() {
        return dataset.getFields().stream().map(field ->
                statTemplate.getFieldStats(field)).collect(Collectors.toList());
    }

}
